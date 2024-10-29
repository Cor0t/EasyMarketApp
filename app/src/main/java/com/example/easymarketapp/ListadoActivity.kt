package com.example.easymarketapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.adapter.ProductAdapter
import com.example.easymarketapp.model.Producto
import com.example.easymarketapp.repository.FirebaseProductRepository
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ListadoActivity : AppCompatActivity() {
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var verOpcionesSimilaresButton: MaterialButton
    private lateinit var aceptarButton: MaterialButton

    // Adapter y Repository
    private lateinit var productAdapter: ProductAdapter
    private val productRepository = FirebaseProductRepository()
    private var currentProducts: List<Producto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadAllProducts() // Cargar todos los productos sin filtros
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        verOpcionesSimilaresButton = findViewById(R.id.verOpcionesSimilaresButton)
        aceptarButton = findViewById(R.id.aceptarButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.products_list)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Configurar el adaptador y el evento de selección de producto
        productAdapter = ProductAdapter(currentProducts) { product ->
            Toast.makeText(
                this,
                "Seleccionado: ${product.nombre} - $${String.format("%.0f", product.precio)}",
                Toast.LENGTH_SHORT
            ).show()
        }

        productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListadoActivity)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadAllProducts() {
        lifecycleScope.launch {
            productRepository.getAllProducts { products ->
                if (products.isEmpty()) {
                    Toast.makeText(this@ListadoActivity, "No se encontraron productos en la base de datos", Toast.LENGTH_LONG).show()
                } else {
                    currentProducts = products
                    productAdapter.updateProducts(products) // Actualizar el adaptador con la lista de productos
                    updateTotal()
                }
            }
        }
    }

    private fun updateTotal() {
        val totalAmount = currentProducts.sumOf { it.precio }
        val formattedTotal = String.format("%.0f", totalAmount)
        totalTextView.text = getString(R.string.total_format, formattedTotal)
    }

    private fun setupListeners() {
        verOpcionesSimilaresButton.setOnClickListener {
            Toast.makeText(this, "Buscando opciones similares...", Toast.LENGTH_SHORT).show()
        }

        aceptarButton.setOnClickListener {
            setResultAndFinish()
        }
    }

    private fun setResultAndFinish() {
        val intent = Intent().apply {
            putExtra("selectedProducts", currentProducts.size)
            putExtra("totalAmount", currentProducts.sumOf { it.precio })
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
