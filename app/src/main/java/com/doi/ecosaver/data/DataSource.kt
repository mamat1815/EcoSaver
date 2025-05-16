package com.doi.ecosaver.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.doi.ecosaver.data.StrukResult.Air
import com.doi.ecosaver.data.StrukResult.BelanjaSimple
import com.doi.ecosaver.data.StrukResult.Bensin
import com.doi.ecosaver.data.StrukResult.Listrik
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface DataSource {

    fun signInGoogle(token:String): LiveData<User>

    fun scanOCRGemini(image: Bitmap): LiveData<StrukResult>

    fun fetchBensin(userId: String, bulanTahun: String): LiveData<StrukResult.Bensin?>
    fun fetchListrik(userId: String, bulanTahun: String): LiveData<StrukResult.Listrik?>
    fun fetchAir(userId: String, bulanTahun: String): LiveData<StrukResult.Air?>
    fun fetchBelanja(userId: String, bulanTahun: String): LiveData<StrukResult.BelanjaSimple?>

    interface FetchCallback<T> {
        fun onResponse(result: T?)
        fun onError(errorMessage: String)
    }

    // Fungsi fetch Bensin
    fun fetchDataBensin(userId: String, bulanTahun: String, callback: FetchCallback<Bensin>) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("data")
            .child(bulanTahun)
            .child("Bensin")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(Bensin::class.java)
                    callback.onResponse(data)
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }

    // Fungsi fetch Listrik
    fun fetchDataListrik(userId: String, bulanTahun: String, callback: FetchCallback<Listrik>) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("data")
            .child(bulanTahun)
            .child("Listrik")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(Listrik::class.java)
                    callback.onResponse(data)
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }

    // Fungsi fetch Air
    fun fetchDataAir(userId: String, bulanTahun: String, callback: FetchCallback<Air>) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("data")
            .child(bulanTahun)
            .child("Air")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(Air::class.java)
                    callback.onResponse(data)
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }

    // Fungsi fetch Belanja
    fun fetchDataBelanja(userId: String, bulanTahun: String, callback: FetchCallback<BelanjaSimple>) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("data")
            .child(bulanTahun)
            .child("Belanja")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(BelanjaSimple::class.java)
                    callback.onResponse(data)
                } else {
                    callback.onResponse(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }
    val onLoading: LiveData<Boolean>

}