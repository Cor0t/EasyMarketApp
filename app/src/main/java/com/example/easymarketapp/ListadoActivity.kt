package com.example.easymarketapp
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.R
import com.example.easymarketapp.adapter.ProductAdapter
import com.example.easymarketapp.model.Product
import com.example.easymarketapp.repository.ProductRepository
import android.content.Intent

class ListadoActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var verOpcionesSimilaresButton: Button
    private lateinit var aceptarButton: Button

    private lateinit var productAdapter: ProductAdapter
    private val productRepository = ProductRepository()
    private var currentProducts: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        initializeViews()
        setupRecyclerView()
        setupListeners()

        val isVegetariano = intent.getBooleanExtra("isVegetariano", false)
        val isCeliaco = intent.getBooleanExtra("isCeliaco", false)
        val presupuesto = intent.getDoubleExtra("presupuesto", 0.0)

        loadProducts(isVegetariano, isCeliaco, presupuesto)
        updateUI(isVegetariano, isCeliaco, presupuesto)
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        titleTextView = findViewById(R.id.titleTextView)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        verOpcionesSimilaresButton = findViewById(R.id.verOpcionesSimilaresButton)
        aceptarButton = findViewById(R.id.aceptarButton)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { product ->
            // Handle product click
            Toast.makeText(this, "Seleccionado: ${product.name}", Toast.LENGTH_SHORT).show()
        }

        productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListadoActivity)
            adapter = productAdapter
        }
    }

    private fun loadProducts(isVegetariano: Boolean, isCeliaco: Boolean, presupuesto: Double) {
        currentProducts = productRepository.getFilteredProducts(isVegetariano, isCeliaco, presupuesto)
        productAdapter.updateProducts(currentProducts)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        verOpcionesSimilaresButton.setOnClickListener {
            // Aquí podrías implementar la lógica para mostrar productos similares
            Toast.makeText(this, "Buscando opciones similares...", Toast.LENGTH_SHORT).show()
        }

        aceptarButton.setOnClickListener {
            // Aquí podrías implementar la lógica para finalizar la selección
            val intent = Intent().apply {
                putExtra("selectedProducts", currentProducts.size)
                putExtra("totalAmount", currentProducts.sumOf { it.price })
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun updateUI(isVegetariano: Boolean, isCeliaco: Boolean, presupuesto: Double) {
        titleTextView.text = buildString {
            append("Lista de Productos")
            if (isVegetariano) append(" (Vegetariano)")
            if (isCeliaco) append(" (Celiaco)")
        }

        val totalAmount = currentProducts.sumOf { it.price }
        totalTextView.text = "Total: $${String.format("%.3f", totalAmount)}"
    }
}