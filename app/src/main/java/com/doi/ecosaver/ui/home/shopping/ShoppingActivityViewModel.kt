package com.doi.ecosaver.ui.home.shopping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.BelanjaDetail
import com.doi.ecosaver.data.StrukResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShoppingActivityViewModel(private val repository: DataRepository) : ViewModel() {

    private val _belanjaDataList = MutableLiveData<List<BelanjaDetail>>()
    val belanjaDataList: LiveData<List<BelanjaDetail>> = _belanjaDataList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val databaseRef: DatabaseReference by lazy {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("users/$uid/data")
    }

    fun loadData() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BelanjaDetail>()
                for (monthSnapshot in snapshot.children) {
                    val monthYear = monthSnapshot.key ?: continue
                    val belanjaSnapshot = monthSnapshot.child("Belanja")

                    // Ambil nilai liter sebagai Double, kemudian ubah ke Float
                    val literValue = belanjaSnapshot.child("totalHarga").getValue(Double::class.java)?.toFloat()
                        ?: continue

                    list.add(BelanjaDetail(monthYear = monthYear, totalHarga = literValue))
                }
                _belanjaDataList.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                _belanjaDataList.postValue(emptyList())
            }
        })
    }
    fun getDetail(userId: String, bulanTahun: String): LiveData<StrukResult.BelanjaSimple?> {
        _loading.value = true
        val liveData = repository.fetchBelanja(userId, bulanTahun)
        liveData.observeForever {
            _loading.value = false
            if (it == null) {
                _errorMessage.value = "Data belanja tidak ditemukan"
            } else {
                _errorMessage.value = null
            }
        }
        return liveData
    }
}
