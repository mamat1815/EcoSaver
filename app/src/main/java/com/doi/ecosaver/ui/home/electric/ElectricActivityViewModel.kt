package com.doi.ecosaver.ui.home.electric

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.ListrikDetail
import com.doi.ecosaver.data.StrukResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ElectricActivityViewModel(private val repository: DataRepository) : ViewModel() {

    private val _listrikDataList = MutableLiveData<List<ListrikDetail>>()
    val listrikDataList: LiveData<List<ListrikDetail>> = _listrikDataList

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
                val list = mutableListOf<ListrikDetail>()
                for (monthSnapshot in snapshot.children) {
                    val monthYear = monthSnapshot.key ?: continue
                    val listrikSnapshot = monthSnapshot.child("Listrik")

                    // Ambil nilai liter sebagai Double, kemudian ubah ke Float
                    val literValue = listrikSnapshot.child("kwh").getValue(Double::class.java)?.toFloat()
                        ?: continue

                    list.add(ListrikDetail(monthYear = monthYear, kWh = literValue))
                }
                _listrikDataList.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                _listrikDataList.postValue(emptyList())
            }
        })
    }
    fun getDetail(userId: String, bulanTahun: String): LiveData<StrukResult.Listrik?> {
        _loading.value = true
        val liveData = repository.fetchListrik(userId, bulanTahun)
        liveData.observeForever {
            _loading.value = false
            if (it == null) {
                _errorMessage.value = "Data listrik tidak ditemukan"
            } else {
                _errorMessage.value = null
            }
        }
        return liveData
    }
}
