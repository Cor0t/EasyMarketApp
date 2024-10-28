package com.example.easymarketapp.repository

import com.example.easymarketapp.model.Product

class ProductRepository {
    private val products = listOf(
        Product(1, "Ensalada César", 12.300, true, true),
        Product(2, "Hamburguesa Vegana", 15.100, true, true),
        Product(3, "Pasta sin Gluten", 13.200, false, true),
        Product(4, "Pizza Vegetariana", 18.000, true, false),
        Product(5, "Sopa de Verduras", 8.500, true, true),
        Product(6, "Pollo al Horno", 16.100, false, true),
        Product(7, "Arroz con Verduras", 10.400, true, true),
        Product(8, "Pescado a la Plancha", 19.000, false, true)
    )

    fun getFilteredProducts(
        isVegetarian: Boolean,
        isCeliaco: Boolean,
        maxPrice: Double
    ): List<Product> {
        return products.filter { product ->
            (!isVegetarian || product.isVegetarian) &&
                    (!isCeliaco || product.isCeliacFriendly) &&
                    product.price <= maxPrice
        }
    }
}