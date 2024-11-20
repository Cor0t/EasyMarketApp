package com.example.easymarketapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val sku: String = "",
    val nombre: String = "",
    val marca: String = "",
    val precio: Double = 0.0,
    val imagen: String = "",
    val pagina: Int = 0,
    val categoria: String = "",
    val documentId: String = "",
    var cantidad: Int = 1
) : Parcelable