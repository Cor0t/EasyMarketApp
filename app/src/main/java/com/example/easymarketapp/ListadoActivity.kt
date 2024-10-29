package com.example.easymarketapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.adapter.ProductAdapter
import com.example.easymarketapp.model.Product
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
    private var currentProducts: List<Product> = emptyList()

    // Filtros
    private var isLactoseIntolerant = false
    private var presupuesto = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        // Obtener los filtros del intent
        isLactoseIntolerant = intent.getBooleanExtra("isLactoseIntolerant", false)
        presupuesto = intent.getDoubleExtra("presupuesto", 0.0)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadFilteredProducts()
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

            // Actualizar título según los filtros
            title = if (isLactoseIntolerant) {
                getString(R.string.products_list_lactose_free)
            } else {
                getString(R.string.products_list)
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { product ->
            // Mostrar detalles del producto seleccionado
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

    private fun loadFilteredProducts() {
        lifecycleScope.launch {
            productRepository.getFilteredProducts(
                isLactoseIntolerant = isLactoseIntolerant,
                presupuesto = presupuesto
            ) { products ->
                if (products.isEmpty()) {
                    Toast.makeText(
                        this@ListadoActivity,
                        "No se encontraron productos que cumplan con los filtros",
                        Toast.LENGTH_LONG
                    ).show()
                }

                currentProducts = products
                productAdapter.updateProducts(products)
                updateTotal()
            }
        }
    }

    private fun updateTotal() {
        val totalAmount = currentProducts.sumOf { it.precio }
        val formattedTotal = String.format("%.0f", totalAmount)
        totalTextView.text = getString(R.string.total_format, formattedTotal)

        // Deshabilitar el botón aceptar si el total excede el presupuesto
        aceptarButton.isEnabled = totalAmount <= presupuesto
    }

    private fun setupListeners() {
        verOpcionesSimilaresButton.setOnClickListener {
            // Aquí podrías implementar la lógica para buscar productos similares
            // por ejemplo, productos con precios similares o características similares
            Toast.makeText(this, "Buscando opciones similares...", Toast.LENGTH_SHORT).show()
        }

        aceptarButton.setOnClickListener {
            val totalAmount = currentProducts.sumOf { it.precio }
            if (totalAmount <= presupuesto) {
                setResultAndFinish()
            } else {
                Toast.makeText(
                    this,
                    "El total excede el presupuesto disponible",
                    Toast.LENGTH_SHORT
                ).show()
            }
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