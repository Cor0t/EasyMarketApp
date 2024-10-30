package com.example.easymarketapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.adapter.ProductAdapter
import com.example.easymarketapp.model.Producto
import com.example.easymarketapp.repository.FirebaseProductRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class ListadoActivity : AppCompatActivity() {
    //Se establecen los elementos de activity_listado
    private lateinit var toolbar: Toolbar
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var sinLactosaSwitch: SwitchMaterial
    private lateinit var productAdapter: ProductAdapter
    private val productRepository = FirebaseProductRepository()
    private var filteredProducts: List<Producto> = emptyList()
    private var presupuesto: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        presupuesto = intent.getDoubleExtra("presupuesto", 0.0)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadAndFilterProducts(intent.getBooleanExtra("isLactoseIntolerant", false))
    }
    //Inicia los elementos de la interfaz
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        sinLactosaSwitch = findViewById(R.id.sinLactosaSwitch)
    }

    //barra de herramientas
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
    //Listado de productos
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(filteredProducts) { }
        productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListadoActivity)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }
    //Se obtienen los productos de la base de datos
    private fun loadAndFilterProducts(isLactoseIntolerant: Boolean) {
        lifecycleScope.launch {
            productRepository.getAllProducts { products ->
                if (products.isEmpty()) {
                    Toast.makeText(this@ListadoActivity, "No se encontraron productos en la base de datos", Toast.LENGTH_LONG).show()
                } else {
                    // Filtrar por presupuesto y preferencia de intolerancia a la lactosa
                    val filteredProducts = filterProductsByBudgetAndLactose(products, presupuesto, isLactoseIntolerant)

                    // Ordenar productos por precio
                    val sortedProducts = filteredProducts.sortedBy { it.precio }
                    this@ListadoActivity.filteredProducts = sortedProducts
                    productAdapter.updateProducts(sortedProducts)
                    updateTotal(sortedProducts)
                }
            }
        }
    }
    //recibe los productos, el presupuesto y el booleano si es intolerante o no
    private fun filterProductsByBudgetAndLactose(products: List<Producto>, budget: Double, isLactoseIntolerant: Boolean): List<Producto> {
        val filteredProducts = mutableListOf<Producto>()
        var currentTotal = 0.0

        for (product in products) {
            if (currentTotal + product.precio <= budget) {
                if (isLactoseIntolerant) {
                    if (product.nombre.lowercase().contains("sin lactosa") ||
                        product.nombre.lowercase().contains("sinlactosa") ||
                        product.nombre.lowercase().contains("deslactosado") ||
                        product.nombre.lowercase().contains("lactose free")
                    ) {
                        filteredProducts.add(product)
                        currentTotal += product.precio
                    }
                } else {
                    filteredProducts.add(product)
                    currentTotal += product.precio
                }
            } else {
                // Se ha alcanzado el presupuesto, no se agregan más productos
                break
            }
        }

        return filteredProducts
    }
    //recibe la lista de productos filtrados y muestra el total
    private fun updateTotal(products: List<Producto>) {
        val total = products.sumOf { it.precio }
        totalTextView.text = getString(
            R.string.total_format_with_budget,
            String.format("%.0f", total),
            String.format("%.0f", presupuesto)
        )
    }

    //al cambiar de estado el switch, vuelve a cargar los productos filtrados
    private fun setupListeners() {
        sinLactosaSwitch.setOnCheckedChangeListener { _, _ ->
            loadAndFilterProducts(sinLactosaSwitch.isChecked)
        }
    }
}