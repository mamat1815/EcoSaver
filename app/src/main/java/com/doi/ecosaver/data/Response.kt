package com.doi.ecosaver.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String,
    val displayName: String,
    val profileImg: String,
): Parcelable{
    constructor() : this("", "", "")
}


sealed class StrukResult : Parcelable {

    @Parcelize
    data class Bensin(
        val jenis: String,
        val liter: Double,
        val totalHarga: Int,
        val bulanTahun: String
    ) : StrukResult()

    @Parcelize
    data class Listrik(
        val jenisMeter: String,
        val kwh: Int,
        val totalHarga: Int,
        val bulanTahun: String
    ) : StrukResult()

    @Parcelize
    data class Air(
        val liter: Double,
        val totalHarga: Int,
        val bulanTahun: String
    ) : StrukResult()

    @Parcelize
    data class BelanjaSimple(
        val jenis: String,
        val total: Int,
        val bulanTahun: String
    ) : StrukResult()

    @Parcelize
    data class Item(
        val nama: String,
        val harga: Int
    ) : Parcelable
}

data class BensinData(
    val liter: Double = 0.0,
    val totalHarga: Int = 0,
    val counter: Int = 0
)

data class ListrikData(
    val kWh: Double = 0.0,
    val totalHarga: Int = 0,
    val counter: Int = 0
)

data class AirData(
    val liter: Double = 0.0,
    val totalHarga: Int = 0,
    val counter: Int = 0
)

data class BelanjaData(
    val totalHarga: Int = 0,
    val counter: Int = 0
)

data class ListrikDetail(
    val monthYear: String,
    val kWh: Float
)

data class BensinDetail(
    val monthYear: String,
    val liter: Float = 0f
)

data class AirDetail(
    val monthYear: String,
    val liter: Float
)

data class BelanjaDetail(
    val monthYear: String,
    val totalHarga: Float
)

data class AnalisisData(
    val monthYear: String,
    val bensin: Float = 0f,
    val listrik: Float = 0f,
    val air: Float = 0f,
    val belanja: Float = 0f
)

