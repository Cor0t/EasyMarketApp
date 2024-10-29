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
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class ListadoActivity : AppCompatActivity() {
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var verOpcionesSimilaresButton: MaterialButton
    private lateinit var aceptarButton: MaterialButton
    private lateinit var sinLactosaSwitch: SwitchMaterial

    // Adapter y Repository
    private lateinit var productAdapter: ProductAdapter
    private val productRepository = FirebaseProductRepository()
    private var selectedProducts: MutableList<Producto> = mutableListOf()
    private var allProducts: List<Producto> = emptyList()

    // Presupuesto
    private var presupuesto: Double = 0.0
    private var currentTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

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
        sinLactosaSwitch = findViewById(R.id.sinLactosaSwitch)
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
        productAdapter = ProductAdapter(allProducts) { product ->
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
            selectedProducts.add(product)
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
                    allProducts = products
                    filterAndDisplayProducts()
                }
            }
        }
    }

    private fun filterAndDisplayProducts() {
        // Filtrar por presupuesto
        var filteredProducts = allProducts.filter { it.precio <= presupuesto }

        // Aplicar filtro sin lactosa si está activado
        if (sinLactosaSwitch.isChecked) {
            filteredProducts = filteredProducts.filter { producto ->
                producto.nombre.lowercase().contains("sin lactosa") ||
                        producto.nombre.lowercase().contains("sinlactosa") ||
                        producto.nombre.lowercase().contains("deslactosado") ||
                        producto.nombre.lowercase().contains("lactose free")
            }
        }

        if (filteredProducts.isEmpty()) {
            Toast.makeText(
                this@ListadoActivity,
                "No hay productos disponibles con los filtros actuales",
                Toast.LENGTH_LONG
            ).show()
        }

        // Ordenar productos por precio
        val sortedProducts = filteredProducts.sortedBy { it.precio }
        productAdapter.updateProducts(sortedProducts)
        updateTotal()
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

        sinLactosaSwitch.setOnCheckedChangeListener { _, _ ->
            filterAndDisplayProducts()
        }
    }

    private fun setResultAndFinish() {
        val intent = Intent().apply {
            putExtra("selectedProducts", selectedProducts.size)
            putExtra("totalAmount", currentTotal)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}