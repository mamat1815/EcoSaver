package com.doi.ecosaver.ui.signin

import androidx.lifecycle.ViewModel
import com.doi.ecosaver.data.DataRepository

class SignInViewModel(private val dataRepository: DataRepository): ViewModel() {
    fun signInWithGoogle(token: String) = dataRepository.signInGoogle(token)
}