package com.doi.ecosaver.ui.scan

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doi.ecosaver.data.DataRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class ScanFragmentViewModel(private val dataRepository: DataRepository): ViewModel() {

    fun sendImageToGemini(bitmap: Bitmap) = dataRepository.scanOCRGemini(bitmap)

    private val _capturedImage = MutableLiveData<Bitmap>()
    val capturedImage: LiveData<Bitmap> get() = _capturedImage

    fun setCapturedImage(bitmap: Bitmap) {
        _capturedImage.value = bitmap
    }

}