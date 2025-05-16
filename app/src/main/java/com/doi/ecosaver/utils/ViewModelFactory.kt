package com.doi.ecosaver.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.ui.analysis.AnalysisViewModel
import com.doi.ecosaver.ui.home.HomeFragmentViewModel
import com.doi.ecosaver.ui.home.electric.ElectricActivityViewModel
import com.doi.ecosaver.ui.home.fuel.FuelActivityViewModel
import com.doi.ecosaver.ui.home.shopping.ShoppingActivityViewModel
import com.doi.ecosaver.ui.home.water.WaterActivityViewModel
import com.doi.ecosaver.ui.scan.ScanFragmentViewModel
import com.doi.ecosaver.ui.signin.SignInViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SignInViewModel::class.java) -> SignInViewModel(
                dataRepository
            ) as T
            modelClass.isAssignableFrom(ScanFragmentViewModel::class.java) -> ScanFragmentViewModel(
                dataRepository
            ) as T
            modelClass.isAssignableFrom(HomeFragmentViewModel::class.java) -> HomeFragmentViewModel(
                dataRepository

            )as T
            modelClass.isAssignableFrom(FuelActivityViewModel::class.java) -> FuelActivityViewModel(
                dataRepository
            )as T
            modelClass.isAssignableFrom(ElectricActivityViewModel::class.java) -> ElectricActivityViewModel(
                dataRepository
            )as T
            modelClass.isAssignableFrom(ShoppingActivityViewModel::class.java) -> ShoppingActivityViewModel(
                dataRepository
            )as T
            modelClass.isAssignableFrom(WaterActivityViewModel::class.java) -> WaterActivityViewModel(
                dataRepository
            )as T
            modelClass.isAssignableFrom(AnalysisViewModel::class.java) -> AnalysisViewModel(
                dataRepository
            )as T

//            modelClass.isAssignableFrom(AddPostViewModel::class.java) -> AddPostViewModel(
//                dataRepository
//            ) as T
//
//            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
//                dataRepository
//            ) as T
//
//            modelClass.isAssignableFrom(DetailPostActivityViewModel::class.java) -> DetailPostActivityViewModel(
//                dataRepository
//            ) as T

            else

            -> throw IllegalArgumentException("Unknown ViewModel: " + modelClass.name)
        }

    }
}