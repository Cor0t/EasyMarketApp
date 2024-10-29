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

    // Presupuesto
    private var presupuesto: Double = 0.0
    private var currentTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        // Obtener el presupuesto del intent
        presupuesto = intent.getDoubleExtra("presupuesto", 0.0)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadAllProducts()
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
        productAdapter = ProductAdapter(currentProducts) { product ->
            handleProductSelection(product)
        }

        productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListadoActivity)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleProductSelection(product: Producto) {
        val newTotal = currentTotal + product.precio

        if (newTotal <= presupuesto) {
            currentTotal = newTotal
            currentProducts = currentProducts + product
            productAdapter.updateProducts(currentProducts)
            updateTotal()
            Toast.makeText(
                this,
                "Agregado: ${product.nombre} - $${String.format("%.0f", product.precio)}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "No se puede agregar el producto. Excede el presupuesto de $${String.format("%.0f", presupuesto)}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadAllProducts() {
        lifecycleScope.launch {
            productRepository.getAllProducts { products ->
                if (products.isEmpty()) {
                    Toast.makeText(this@ListadoActivity, "No se encontraron productos en la base de datos", Toast.LENGTH_LONG).show()
                } else {
                    // Primero filtramos productos que individualmente no excedan el presupuesto
                    val availableProducts = products.filter { it.precio <= presupuesto }

                    if (availableProducts.isEmpty()) {
                        Toast.makeText(
                            this@ListadoActivity,
                            "No hay productos disponibles dentro del presupuesto establecido",
                            Toast.LENGTH_LONG
                        ).show()
                        return@getAllProducts
                    }

                    // Ordenamos los productos por precio (de menor a mayor)
                    val sortedProducts = availableProducts.sortedBy { it.precio }

                    // Lista para almacenar los productos seleccionados
                    val selectedProducts = mutableListOf<Producto>()
                    var currentSum = 0.0

                    // Iteramos sobre los productos ordenados
                    for (product in sortedProducts) {
                        // Si agregar este producto no excede el presupuesto, lo añadimos
                        if (currentSum + product.precio <= presupuesto) {
                            selectedProducts.add(product)
                            currentSum += product.precio
                        }
                    }

                    if (selectedProducts.isEmpty()) {
                        Toast.makeText(
                            this@ListadoActivity,
                            "No se encontró una combinación de productos dentro del presupuesto",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Actualizar el RecyclerView con los productos seleccionados
                        productAdapter.updateProducts(selectedProducts)

                        // Opcional: Mostrar el total gastado
                        Toast.makeText(
                            this@ListadoActivity,
                            "Total: $${String.format("%.2f", currentSum)} de $$presupuesto",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun updateTotal() {
        totalTextView.text = getString(
            R.string.total_format_with_budget,
            String.format("%.0f", currentTotal),
            String.format("%.0f", presupuesto)
        )
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
            putExtra("totalAmount", currentTotal)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}