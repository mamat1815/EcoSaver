package com.doi.ecosaver.ui.home.water

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doi.ecosaver.data.AirDetail
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.StrukResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WaterActivityViewModel(private val repository: DataRepository) : ViewModel() {

    private val _airDataList = MutableLiveData<List<AirDetail>>()
    val airDataList: LiveData<List<AirDetail>> = _airDataList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val databaseRef: DatabaseReference by lazy {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("users/$uid/data")
    }

    fun loadAirData() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AirDetail>()
                for (monthSnapshot in snapshot.children) {
                    val monthYear = monthSnapshot.key ?: continue
                    val airSnapshot = monthSnapshot.child("Air")

                    // Ambil nilai liter sebagai Double, kemudian ubah ke Float
                    val literValue = airSnapshot.child("liter").getValue(Double::class.java)?.toFloat()
                        ?: continue

                    list.add(AirDetail(monthYear = monthYear, liter = literValue))
                }
                _airDataList.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                _airDataList.postValue(emptyList())
            }
        })
    }

    fun getAirDetail(userId: String, bulanTahun: String): LiveData<StrukResult.Air?> {
        _loading.value = true
        val liveData = repository.fetchAir(userId, bulanTahun)
        liveData.observeForever {
            _loading.value = false
            if (it == null) {
                _errorMessage.value = "Data air tidak ditemukan"
            } else {
                _errorMessage.value = null
            }
        }
        return liveData
    }
}
