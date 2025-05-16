package com.doi.ecosaver.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.StrukResult

class HomeFragmentViewModel(private val repo: DataRepository): ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun getBensinDetail(userId: String, bulanTahun: String): LiveData<StrukResult.Bensin?> {
        _loading.value = true
        val liveData = repo.fetchBensin(userId, bulanTahun)
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

    fun getListrikDetail(userId: String, bulanTahun: String): LiveData<StrukResult.Listrik?> {
        _loading.value = true
        val liveData = repo.fetchListrik(userId, bulanTahun)
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

    fun getAirDetail(userId: String, bulanTahun: String): LiveData<StrukResult.Air?> {
        _loading.value = true
        val liveData = repo.fetchAir(userId, bulanTahun)
        Log.d("HomeFragmentViewModel", liveData.value?.totalHarga.toString())
        liveData.observeForever {
            _loading.value = false
            Log.d("HomeFragmentViewModelDDDD", liveData.value?.totalHarga.toString())
            if (it == null) {
                _errorMessage.value = "Data air tidak ditemukan"
            } else {
                _errorMessage.value = null
            }
        }
        return liveData
    }

    fun getBelanjaDetail(userId: String, bulanTahun: String): LiveData<StrukResult.BelanjaSimple?> {
        _loading.value = true
        val liveData = repo.fetchBelanja(userId, bulanTahun)
        Log.d("HomeFragmentViewModel", liveData.value?.total.toString())
        liveData.observeForever {
            _loading.value = false
            Log.d("HomeFragmentViewModelBELANJA", liveData.value?.total.toString())
            if (it == null) {
                _errorMessage.value = "Data belanja tidak ditemukan"
            } else {
                _errorMessage.value = null
            }
        }
        return liveData
    }
}
