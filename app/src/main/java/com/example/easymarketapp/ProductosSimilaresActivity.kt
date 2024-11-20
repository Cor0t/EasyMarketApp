package com.example.easymarketapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.adapter.ProductosSimilaresAdapter
import com.example.easymarketapp.model.Producto
import com.google.android.material.button.MaterialButton

class ProductosSimilaresActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var addSelectedButton: MaterialButton
    private lateinit var adapter: ProductosSimilaresAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_similares)

        setupViews()
        setupToolbar()
        mostrarProductosSimilares()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.similarProductsRecyclerView)
        addSelectedButton = findViewById(R.id.addSelectedButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductosSimilaresAdapter()
        recyclerView.adapter = adapter

        addSelectedButton.setOnClickListener {
            val productosSeleccionados = adapter.getSelectedProducts()
            if (productosSeleccionados.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ningún producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent().apply {
                putParcelableArrayListExtra("productos_seleccionados", ArrayList(productosSeleccionados))
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Productos Similares"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun mostrarProductosSimilares() {
        val allProducts = intent.getParcelableArrayListExtra<Producto>("productos_originales") ?: return
        val productosActuales = intent.getParcelableArrayListExtra<Producto>("productos_actuales") ?: emptyList()
        val precioReferencia = intent.getDoubleExtra("precio_referencia", 0.0)
        val isLactoseIntolerant = intent.getBooleanExtra("isLactoseIntolerant", false)
        val isCeliac = intent.getBooleanExtra("isCeliac", false)

        val minPrice = precioReferencia * 0.8
        val maxPrice = precioReferencia * 1.2

        // Filtrar productos por precio y restricciones dietéticas
        val productosSimilares = allProducts.filter { producto ->
            val precioEnRango = producto.precio in minPrice..maxPrice
            val noEstaEnListaActual = !productosActuales.any { it.sku == producto.sku }
            val cumpleRestricciones = when {
                // Para productos lácteos cuando hay restricción de lactosa
                isLactoseIntolerant && producto.categoria == "Leches" -> {
                    producto.nombre.lowercase().contains("sin lactosa") ||
                            producto.nombre.lowercase().contains("sinlactosa") ||
                            producto.nombre.lowercase().contains("deslactosado") ||
                            producto.nombre.lowercase().contains("lactose free")
                }
                // Para helados, solo mostrar si no hay restricción de lactosa
                producto.categoria == "Helados" -> !isLactoseIntolerant
                // Para productos con gluten
                isCeliac && (producto.categoria == "Pastas_Salsas" ||
                        producto.categoria == "Arroz_Legumbres") -> {
                    when (producto.categoria) {
                        "Pastas_Salsas" -> {
                            if (producto.nombre.lowercase().contains("pasta") ||
                                producto.nombre.lowercase().contains("fideos") ||
                                producto.nombre.lowercase().contains("lasaña") ||
                                producto.nombre.lowercase().contains("lasagna") ||
                                producto.nombre.lowercase().contains("ravioles") ||
                                producto.nombre.lowercase().contains("tallarines")
                            ) {
                                producto.nombre.lowercase().contains("sin gluten") ||
                                        producto.nombre.lowercase().contains("singluten") ||
                                        producto.nombre.lowercase().contains("gluten free")
                            } else {
                                true // Para salsas y otros productos
                            }
                        }
                        "Arroz_Legumbres" -> {
                            producto.nombre.lowercase().contains("arroz") ||
                                    producto.nombre.lowercase().contains("quinoa") ||
                                    producto.nombre.lowercase().contains("quinua") ||
                                    producto.nombre.lowercase().contains("garbanzo") ||
                                    producto.nombre.lowercase().contains("lenteja") ||
                                    producto.nombre.lowercase().contains("poroto") ||
                                    producto.nombre.lowercase().contains("frijol") ||
                                    producto.nombre.lowercase().contains("arveja") ||
                                    producto.nombre.lowercase().contains("sin gluten") ||
                                    producto.nombre.lowercase().contains("singluten") ||
                                    producto.nombre.lowercase().contains("gluten free")
                        }
                        else -> true
                    }
                }
                else -> true
            }

            precioEnRango && noEstaEnListaActual && cumpleRestricciones
        }.distinctBy { it.sku } // Asegurarse de que no haya duplicados

        val opcionesOrdenadas = productosSimilares
            .sortedBy { Math.abs(it.precio - precioReferencia) }
            .take(10)

        adapter.submitList(opcionesOrdenadas)
    }
}