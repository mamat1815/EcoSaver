package com.doi.ecosaver.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doi.ecosaver.data.AnalisisData
import com.doi.ecosaver.data.DataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AnalysisViewModel(repository: DataRepository) : ViewModel() {

    private val _analisisDataList = MutableLiveData<List<AnalisisData>>()
    val analisisDataList: LiveData<List<AnalisisData>> = _analisisDataList

    private val databaseRef: DatabaseReference by lazy {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("users/$uid/data")
    }

    fun loadAllData() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AnalisisData>()

                for (monthSnapshot in snapshot.children) {
                    val monthYear = monthSnapshot.key ?: continue

                    val bensin = monthSnapshot.child("Bensin/liter").getValue(Double::class.java)?.toFloat() ?: 0f
                    val listrik = monthSnapshot.child("Listrik/liter").getValue(Double::class.java)?.toFloat() ?: 0f
                    val air = monthSnapshot.child("Air/liter").getValue(Double::class.java)?.toFloat() ?: 0f
                    val belanja = monthSnapshot.child("Belanja/totalHarga").getValue(Double::class.java)?.toFloat() ?: 0f

                    list.add(
                        AnalisisData(
                            monthYear = monthYear,
                            bensin = bensin,
                            listrik = listrik,
                            air = air,
                            belanja = belanja
                        )
                    )
                }

                _analisisDataList.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                _analisisDataList.postValue(emptyList())
            }
        })
    }
}
