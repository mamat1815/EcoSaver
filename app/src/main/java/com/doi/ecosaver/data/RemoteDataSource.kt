package com.doi.ecosaver.data

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.doi.ecosaver.data.StrukResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class RemoteDataSource {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    interface SignInWithGoogleCallback {
        fun onResponse(response: User)
        fun onFailure(errorMessage: String)
    }

fun signInWithGoogle(idToken: String, callback: SignInWithGoogleCallback) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    auth.signInWithCredential(credential).addOnCompleteListener {
        val users = auth.currentUser
        if (users != null) {
            val uid = users.uid
            val userRef = db.reference.child("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    val userData = User(
                        uid,
                        users.displayName ?: "",
                        users.photoUrl?.toString() ?: ""
                    )
                    userRef.setValue(userData)
                    callback.onResponse(userData)
                } else {
                    val existingUser = snapshot.getValue(User::class.java)
                    callback.onResponse(existingUser!!)
                }
            }.addOnFailureListener { error ->
                callback.onFailure(error.message ?: "Unknown error")
            }
        } else {
            callback.onFailure("User is null")
        }
    }
}

    interface OcrCallback {
        fun onResponse(response: StrukResult)
    }

    fun scanOCRGeminiRemote(image1: Bitmap, callback: OcrCallback) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "GEMINIAPIKEY"
        )

        val userId = auth.currentUser?.uid.toString()

        val content = content {
            image(image1)
            text("""
            Scan struk ini dan berikan informasi dalam format berikut (tanpa tambahan kata lain):
            
            Kategori: [Bensin/Listrik/Air/Belanja]
            
            Jika Bensin:
            SPBU: [nama]
            Jenis: [Premium/Pertalite/Pertamax/Solar]
            Liter: [xx.xx L]
            Total: Rp.xxx.xxx
            
            Jika Listrik:
            Jenis: [Prabayar/Pasca Bayar]
            KWh: [xx KWh]
            Total: Rp.xxx.xxx
            
            Jika Air:
            Liter: [xx Liter] (jika tidak ada tulis "Asumsi Liter")
            Total: Rp.xxx.xxx
            
            Jika Belanja:
            Jenis Belanja: [Harian/Bulanan/Pasar/Makanan/Pergi]  
            Total: Rp.xxx.xxx  
            
            
            Tulis sesuai format dan jangan menambahkan kata lain.
            """.trimIndent())

        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = generativeModel.generateContent(content)
                val text = result.text ?: ""
                val resultObj = parseGeminiResponse(text)
                withContext(Dispatchers.Main) {
                    callback.onResponse(resultObj)
                    val basePath = "users/$userId/data"
                    when (resultObj) {

                        is StrukResult.Bensin -> {
                            val path = "$basePath/${resultObj.bulanTahun}/Bensin"
                            val ref = db.reference.child(path)
                            ref.get().addOnSuccessListener { snapshot ->
                                val exsitingCounter = snapshot.child("counter").getValue(Int::class.java) ?: 0
                                val existingLiter =
                                    snapshot.child("liter").getValue(Double::class.java) ?: 0.0
                                val existingTotal =
                                    snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                                ref.setValue(
                                    mapOf(
                                        "counter" to exsitingCounter + 1,
                                        "liter" to existingLiter + resultObj.liter,
                                        "totalHarga" to existingTotal + resultObj.totalHarga
                                    )
                                )
                            }
                        }

                        is StrukResult.Air -> {
                            val path = "$basePath/${resultObj.bulanTahun}/Air"
                            val ref = db.reference.child(path)

                            ref.get().addOnSuccessListener { snapshot ->
                                val exsitingCounter =
                                    snapshot.child("counter").getValue(Int::class.java) ?: 0
                                val existingLiter =
                                    snapshot.child("liter").getValue(Double::class.java) ?: 0.0
                                val existingTotal =
                                    snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                                ref.setValue(
                                    mapOf(
                                        "counter" to exsitingCounter + 1,
                                        "liter" to existingLiter + resultObj.liter,
                                        "totalHarga" to existingTotal + resultObj.totalHarga
                                    )
                                )
                            }
                        }
                        is StrukResult.BelanjaSimple -> {
                            val path = "$basePath/${resultObj.bulanTahun}/Belanja"
                            val ref = db.reference.child(path)
                            ref.get().addOnSuccessListener { snapshot ->
                                val exsitingCounter =
                                    snapshot.child("counter").getValue(Int::class.java) ?: 0
                                val existingTotal =
                                    snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                                ref.setValue(
                                    mapOf(
                                        "counter" to exsitingCounter + 1,
                                        "totalHarga" to existingTotal + resultObj.total
                                    )
                                )
                            }
                        }
                        is StrukResult.Listrik -> {
                            val path = "$basePath/${resultObj.bulanTahun}/Listrik"
                            val ref = db.reference.child(path)
                            ref.get().addOnSuccessListener { snapshot ->
                                val exsitingCounter =
                                    snapshot.child("counter").getValue(Int::class.java) ?: 0
                                val existingKwh =
                                    snapshot.child("kwh").getValue(Int::class.java) ?: 0
                                val existingTotal =
                                    snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                                ref.setValue(
                                    mapOf(
                                        "counter" to exsitingCounter + 1,
                                        "kwh" to existingKwh + resultObj.kwh,
                                        "totalHarga" to existingTotal + resultObj.totalHarga
                                    )
                                )
                            }


                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun parseGeminiResponse(text: String): StrukResult {
        val date = Regex("""\d{4}-\d{2}-\d{2}""").find(text)?.value ?: "2025-05-01"
        val bulanTahun = date.substring(0, 7)

        return when {
            text.contains("spbu", true) -> {
                val jenis = Regex("Premium|Pertalite|Pertamax|Solar", RegexOption.IGNORE_CASE).find(text)?.value ?: "Lainnya"
                val liter = Regex("""Liter:\s*(\d+(?:[.,]\d+)?)""", RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                val total = Regex("""Total:\s*Rp[\s.]?(\d+(?:\.\d+)*)""").find(text)?.groupValues?.get(1)?.replace(".", "")?.toIntOrNull() ?: 0

                StrukResult.Bensin(jenis, liter, total, bulanTahun)
            }

            text.contains("listrik", true) -> {
                val jenis = Regex("""Jenis:\s*(Prabayar|Pasca Bayar)""", RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1) ?: "Tidak diketahui"
                val kwh = Regex("""KWh:\s*(\d+)""", RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val total = Regex("""Total:\s*Rp[\s.]?(\d+(?:\.\d+)*)""").find(text)?.groupValues?.get(1)?.replace(".", "")?.toIntOrNull() ?: 0

                StrukResult.Listrik(jenis, kwh, total, bulanTahun)
            }

            text.contains("air", true) || text.contains("pdam", true) -> {
                val liter = Regex("""(\d+(?:[.,]\d+)?)\s*[lL]""")
                    .find(text)?.groupValues?.get(1)
                    ?.replace(",", ".")?.toDoubleOrNull()

                val bayar = Regex("""Rp[\s.]?(\d+(?:\.\d+)*)""")
                    .find(text)?.groupValues?.get(1)
                    ?.replace(".", "")?.toIntOrNull() ?: 0

                val hasilLiter = liter ?: ((bayar / 7500.0) * 1000)


                StrukResult.Air(hasilLiter, bayar, bulanTahun)
            }

            else -> {
                val jenisBelanja = Regex("""Jenis Belanja:\s*(\w+)""", RegexOption.IGNORE_CASE)
                    .find(text)?.groupValues?.get(1) ?: "Tidak diketahui"

                val total = Regex("""Total:\s*Rp[\s.]?(\d+(?:\.\d+)*)""")
                    .find(text)?.groupValues?.get(1)?.replace(".", "")?.toIntOrNull() ?: 0

                StrukResult.BelanjaSimple(jenisBelanja, total, bulanTahun)
            }

        }
    }

    interface FetchCallback<T> {
        fun onResponse(result: T?)
        fun onError(errorMessage: String)
    }
    fun fetchDataBensin(userId: String, bulanTahun: String, callback: FetchCallback<StrukResult.Bensin>) {
        val path = "users/$userId/data/$bulanTahun/Bensin"
        val ref = db.reference.child(path)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val liter = snapshot.child("liter").getValue(Double::class.java) ?: 0.0
                val total = snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                val jenis = "Campuran"

                callback.onResponse(StrukResult.Bensin(jenis, liter, total, bulanTahun))
            } else {
                callback.onResponse(null)
            }
        }.addOnFailureListener {
            callback.onError(it.message ?: "Gagal mengambil data bensin")
        }
    }

    fun fetchDataListrik(userId: String, bulanTahun: String, callback: FetchCallback<StrukResult.Listrik>) {
        val path = "users/$userId/data/$bulanTahun/Listrik"
        val ref = db.reference.child(path)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val kwh = snapshot.child("kwh").getValue(Int::class.java) ?: 0
                val total = snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                val jenis = "Campuran"

                callback.onResponse(StrukResult.Listrik(jenis, kwh, total, bulanTahun))
            } else {
                callback.onResponse(null)
            }
        }.addOnFailureListener {
            callback.onError(it.message ?: "Gagal mengambil data listrik")
        }
    }

    fun fetchDataAir(userId: String, bulanTahun: String, callback: FetchCallback<StrukResult.Air>) {
        val path = "users/$userId/data/$bulanTahun/Air"
        val ref = db.reference.child(path)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val liter = snapshot.child("liter").getValue(Double::class.java) ?: 0.0
                val total = snapshot.child("totalHarga").getValue(Int::class.java) ?: 0

                callback.onResponse(StrukResult.Air(liter, total, bulanTahun))
            } else {
                callback.onResponse(null)
            }
        }.addOnFailureListener {
            callback.onError(it.message ?: "Gagal mengambil data air")
        }
    }

    fun fetchDataBelanja(userId: String, bulanTahun: String, callback: FetchCallback<StrukResult.BelanjaSimple>) {
        val path = "users/$userId/data/$bulanTahun/Belanja"
        val ref = db.reference.child(path)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val total = snapshot.child("totalHarga").getValue(Int::class.java) ?: 0
                val jenis = "Campuran" 

                callback.onResponse(StrukResult.BelanjaSimple(jenis, total, bulanTahun))
            } else {
                callback.onResponse(null)
            }
        }.addOnFailureListener {
            callback.onError(it.message ?: "Gagal mengambil data belanja")
        }
    }

}
