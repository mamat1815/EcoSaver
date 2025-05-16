package com.doi.ecosaver.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataRepository(private val repository: RemoteDataSource) :DataSource{

    val signIn = MutableLiveData<User>()
    val ocr = MutableLiveData<StrukResult>()


    private val _onLoading = MutableLiveData<Boolean>()
    override val onLoading: LiveData<Boolean> get() = _onLoading

    override fun signInGoogle(token: String): LiveData<User> {
        repository.signInWithGoogle(token, object : RemoteDataSource.SignInWithGoogleCallback{
            override fun onResponse(response: User) {
                signIn.value = response
            }

            override fun onFailure(errorMessage: String) {

            }
        })
        return signIn
    }

    override fun scanOCRGemini(image: Bitmap): LiveData<StrukResult> {
        _onLoading.postValue(true)
        repository.scanOCRGeminiRemote(image, object : RemoteDataSource.OcrCallback{
            override fun onResponse(response: StrukResult) {
                ocr.value = response
                _onLoading.postValue(false)
            }
        })
        return ocr
    }
    override fun fetchBensin(userId: String, bulanTahun: String): LiveData<StrukResult.Bensin?> {
        val liveData = MutableLiveData<StrukResult.Bensin?>()
        repository.fetchDataBensin(userId, bulanTahun, object : RemoteDataSource.FetchCallback<StrukResult.Bensin> {
            override fun onResponse(result: StrukResult.Bensin?) {
                liveData.postValue(result)
            }

            override fun onError(errorMessage: String) {
                liveData.postValue(null)
            }
        })
        return liveData
    }

    override fun fetchListrik(userId: String, bulanTahun: String): LiveData<StrukResult.Listrik?> {
        val liveData = MutableLiveData<StrukResult.Listrik?>()
        repository.fetchDataListrik(userId, bulanTahun, object : RemoteDataSource.FetchCallback<StrukResult.Listrik> {
            override fun onResponse(result: StrukResult.Listrik?) {
                liveData.postValue(result)
            }

            override fun onError(errorMessage: String) {
                liveData.postValue(null)
            }
        })
        return liveData
    }

    override fun fetchAir(userId: String, bulanTahun: String): LiveData<StrukResult.Air?> {
        val liveData = MutableLiveData<StrukResult.Air?>()
        repository.fetchDataAir(userId, bulanTahun, object : RemoteDataSource.FetchCallback<StrukResult.Air> {
            override fun onResponse(result: StrukResult.Air?) {
                liveData.postValue(result)
            }

            override fun onError(errorMessage: String) {
                liveData.postValue(null)
            }
        })
        return liveData
    }

    override fun fetchBelanja(userId: String, bulanTahun: String): LiveData<StrukResult.BelanjaSimple?> {
        val liveData = MutableLiveData<StrukResult.BelanjaSimple?>()
        repository.fetchDataBelanja(userId, bulanTahun, object : RemoteDataSource.FetchCallback<StrukResult.BelanjaSimple> {
            override fun onResponse(result: StrukResult.BelanjaSimple?) {
                liveData.postValue(result)
            }

            override fun onError(errorMessage: String) {
                liveData.postValue(null)
            }
        })
        return liveData
    }

}