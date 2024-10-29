package com.example.easymarketapp.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.easymarketapp.model.Producto
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("productos")

    // Lista de palabras clave que indican que un producto es sin lactosa
    private val lactoseFreeKeywords = listOf(
        "sin lactosa",
        "deslactosado",
        "lactose free",
        "vegano",
        "soya",
        "almendra",
        "avena",
        "coco",
        "vegetal"
    ).map { it.lowercase() }

    // Lista de marcas que son conocidas por producir productos sin lactosa
    private val lactoseFreeBrands = listOf(
        "alpro",
        "silk",
        "not milk",
        "notco",
        "next level",
        "almond breeze",
        "nature's heart"
    ).map { it.lowercase() }

    private fun isLactoseFree(producto: Producto): Boolean {
        val nombreLower = producto.nombre.lowercase()
        val marcaLower = producto.marca.lowercase()

        // Verifica si el nombre del producto contiene alguna palabra clave
        val hasLactoseFreeKeyword = lactoseFreeKeywords.any { keyword ->
            nombreLower.contains(keyword)
        }

        // Verifica si la marca está en la lista de marcas sin lactosa
        val isLactoseFreeBrand = lactoseFreeBrands.any { brand ->
            marcaLower.contains(brand)
        }

        return hasLactoseFreeKeyword || isLactoseFreeBrand
    }

    suspend fun getFilteredProducts(
        isLactoseIntolerant: Boolean,
        presupuesto: Double,
        callback: (List<Producto>) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val snapshot = productsCollection
                    .whereLessThanOrEqualTo("precio", presupuesto)
                    .get()
                    .await()

                val productos = snapshot.documents.mapNotNull { document ->
                    try {
                        Producto(
                            sku = document.getString("sku") ?: "",
                            nombre = document.getString("nombre") ?: "",
                            marca = document.getString("marca") ?: "",
                            precio = document.getString("precio")?.toDoubleOrNull() ?: 0.0,
                            imagen = document.getString("imagen") ?: "",
                            pagina = document.getLong("pagina")?.toInt() ?: 0
                        ).apply {
                            // Determinamos si es sin lactosa basado en el nombre y marca
                            islactoseIntolerant = isLactoseFree(this)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }.let { allProducts ->
                    // Filtramos los productos según la preferencia del usuario
                    if (isLactoseIntolerant) {
                        allProducts.filter { it.islactoseIntolerant }
                    } else {
                        allProducts
                    }
                }

                withContext(Dispatchers.Main) {
                    callback(productos)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                callback(emptyList())
            }
        }
    }
}