package com.doi.ecosaver.ui.home.fuel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doi.ecosaver.data.BensinDetail
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.StrukResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FuelActivityViewModel(private val repository: DataRepository) : ViewModel() {

    private val _bensinDataList = MutableLiveData<List<BensinDetail>>()
    val bensinDataList: LiveData<List<BensinDetail>> = _bensinDataList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val databaseRef: DatabaseReference by lazy {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("users/$uid/data")
    }

    fun loadBensinData() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BensinDetail>()
                for (monthSnapshot in snapshot.children) {
                    val monthYear = monthSnapshot.key ?: continue
                    val bensinSnapshot = monthSnapshot.child("Bensin")

                    // Ambil nilai liter sebagai Double, kemudian ubah ke Float
                    val literValue = bensinSnapshot.child("liter").getValue(Double::class.java)?.toFloat()
                        ?: continue

                    list.add(BensinDetail(monthYear = monthYear, liter = literValue))
                }
                _bensinDataList.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                _bensinDataList.postValue(emptyList())
            }
        })
    }

    fun getBensinDetail(userId: String, bulanTahun: String): LiveData<StrukResult.Bensin?> {
        _loading.value = true
        val liveData = repository.fetchBensin(userId, bulanTahun)
        liveData.observeForever {
            _loading.value = false
            if (it == null) {
                _errorMessage.value = "Data bensin tidak ditemukan"
            } else {
                _errorMessage.value = null
            }
        }
        return liveData
    }
}
