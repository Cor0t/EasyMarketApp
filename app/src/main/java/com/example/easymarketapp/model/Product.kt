package com.example.easymarketapp.model

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val isVegetarian: Boolean = false,
    val isCeliacFriendly: Boolean = false,
    val imageUrl: String? = null
)