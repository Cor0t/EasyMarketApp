package com.example.easymarketapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.adapter.ProductAdapter
import com.example.easymarketapp.model.Producto
import com.google.android.material.button.MaterialButton

class ResumenCompraActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var restrictionsTextView: TextView
    private lateinit var budgetTextView: TextView
    private lateinit var selectedProductsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var savingsTextView: TextView
    private lateinit var shareButton: MaterialButton
    private lateinit var finishButton: MaterialButton
    private lateinit var productAdapter: ProductAdapter
    private var selectedProducts: List<Producto> = emptyList()
    private var presupuesto: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen_compra)

        initializeViews()
        setupToolbar()
        loadData()
        setupRecyclerView()
        setupButtons()
        updateUI()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        restrictionsTextView = findViewById(R.id.restrictionsTextView)
        budgetTextView = findViewById(R.id.budgetTextView)
        selectedProductsRecyclerView = findViewById(R.id.selectedProductsRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        savingsTextView = findViewById(R.id.savingsTextView)
        shareButton = findViewById(R.id.shareButton)
        finishButton = findViewById(R.id.finishButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Resumen de Compra"
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadData() {
        val extras = intent.extras
        if (extras != null) {
            presupuesto = extras.getDouble("presupuesto", 0.0)
            val isLactoseIntolerant = extras.getBoolean("isLactoseIntolerant", false)
            val isCeliac = extras.getBoolean("isCeliac", false)
            selectedProducts = extras.getParcelableArrayList("selectedProducts") ?: emptyList()

            // Mostrar restricciones
            val restrictions = mutableListOf<String>()
            if (isLactoseIntolerant) restrictions.add("Sin Lactosa")
            if (isCeliac) restrictions.add("Sin Gluten")
            restrictionsTextView.text = "Restricciones: ${if (restrictions.isEmpty()) "Ninguna" else restrictions.joinToString(", ")}"

            // Mostrar presupuesto
            budgetTextView.text = "Presupuesto: $${String.format("%,.0f", presupuesto)}"
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = selectedProducts,
            onProductClick = { /* No action needed */ },
            onDeleteClick = { /* No action needed */ }
        )
        selectedProductsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ResumenCompraActivity)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupButtons() {
        shareButton.setOnClickListener {
            shareResumen()
        }

        finishButton.setOnClickListener {
            // Volver al listado
            val intent = Intent(this, ListadoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)  // Esto limpiará el stack de actividades
            startActivity(intent)
            finish()
        }
    }

    private fun updateUI() {
        // Calcular el total sumando el precio de cada producto multiplicado por su cantidad
        val total = selectedProducts.sumOf { it.precio * it.cantidad }
        totalTextView.text = "Total: $${String.format("%,.0f", total)} / $${String.format("%,.0f", presupuesto)}"

        val savings = presupuesto - total
        if (savings > 0) {
            savingsTextView.text = "Ahorro: $${String.format("%,.0f", savings)}"
            savingsTextView.setTextColor(getColor(R.color.primaryColor))
        } else {
            savingsTextView.text = "Exceso: $${String.format("%,.0f", -savings)}"
            savingsTextView.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    private fun shareResumen() {
        val total = selectedProducts.sumOf { it.precio * it.cantidad }
        val message = buildString {
            appendLine("🛒 Mi lista de compras:")
            appendLine()
            selectedProducts.forEach { producto ->
                appendLine("- ${producto.nombre} x${producto.cantidad}")
                appendLine("  $${String.format("%,.0f", producto.precio * producto.cantidad)}")
            }
            appendLine()
            appendLine("💰 Total: $${String.format("%,.0f", total)}")
            appendLine("💵 Presupuesto: $${String.format("%,.0f", presupuesto)}")
            val savings = presupuesto - total
            if (savings > 0) {
                appendLine("✨ Ahorro: $${String.format("%,.0f", savings)}")
            } else {
                appendLine("⚠️ Exceso: $${String.format("%,.0f", -savings)}")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Compartir lista de compras"))
    }
}