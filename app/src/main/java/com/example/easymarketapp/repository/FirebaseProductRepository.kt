package com.example.easymarketapp.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.easymarketapp.model.Producto
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("productos")

    suspend fun getAllProducts(callback: (List<Producto>) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                val snapshot = productsCollection.get().await()

                val products = snapshot.documents.mapNotNull { document ->
                    try {
                        Producto(
                            sku = document.getString("sku") ?: "",
                            nombre = document.getString("nombre") ?: "",
                            marca = document.getString("marca") ?: "",
                            precio = document.getString("precio")?.toDoubleOrNull() ?: 0.0,
                            imagen = document.getString("imagen") ?: "",
                            pagina = document.getLong("pagina")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    callback(products)
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
