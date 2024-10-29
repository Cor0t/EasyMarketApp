package com.example.easymarketapp.model

data class Product(
    val sku: String = "",
    val nombre: String = "",
    val marca: String = "",
    val precio: Double = 0.0,
    val imagen: String = "",
    val pagina: Int = 0,
    var islactoseIntolerant: Boolean = false,

    )