package com.example.easymarketapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.adapter.ProductAdapter
import com.example.easymarketapp.model.Producto
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot

class ListadoActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var sinLactosaSwitch: SwitchMaterial
    private lateinit var sinGlutenSwitch: SwitchMaterial
    private lateinit var verOpcionesSimilaresButton: MaterialButton
    private lateinit var aceptarButton: MaterialButton
    private lateinit var productAdapter: ProductAdapter
    private var filteredProducts: List<Producto> = emptyList()
    private var allLoadedProducts: List<Producto> = emptyList()
    private var presupuesto: Double = 0.0
    private var dialogoMostrado = false
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        presupuesto = intent.getDoubleExtra("presupuesto", 0.0)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadAndFilterProducts(
            isLactoseIntolerant = intent.getBooleanExtra("isLactoseIntolerant", false),
            isCeliac = intent.getBooleanExtra("isCeliac", false)
        )
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        sinLactosaSwitch = findViewById(R.id.sinLactosaSwitch)
        sinGlutenSwitch = findViewById(R.id.sinGlutenSwitch)
        verOpcionesSimilaresButton = findViewById(R.id.verOpcionesSimilaresButton)
        aceptarButton = findViewById(R.id.aceptarButton)
    }

    private fun setupListeners() {
        sinLactosaSwitch.isChecked = intent.getBooleanExtra("isLactoseIntolerant", false)
        sinGlutenSwitch.isChecked = intent.getBooleanExtra("isCeliac", false)

        sinLactosaSwitch.setOnCheckedChangeListener { _, isChecked ->
            loadAndFilterProducts(isChecked, sinGlutenSwitch.isChecked)
        }

        sinGlutenSwitch.setOnCheckedChangeListener { _, isChecked ->
            loadAndFilterProducts(sinLactosaSwitch.isChecked, isChecked)
        }

        verOpcionesSimilaresButton.setOnClickListener {
            if (!dialogoMostrado) {
                mostrarDialogoConfirmacion()
            } else {
                mostrarOpcionesSimilares()
            }
        }

        aceptarButton.setOnClickListener {
            val productosSeleccionados = filteredProducts.filter { it.cantidad > 0 }

            if (productosSeleccionados.isEmpty()) {
                Toast.makeText(this, "No hay productos seleccionados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ResumenCompraActivity::class.java).apply {
                putExtra("presupuesto", presupuesto)
                putExtra("isLactoseIntolerant", sinLactosaSwitch.isChecked)
                putExtra("isCeliac", sinGlutenSwitch.isChecked)
                putParcelableArrayListExtra("selectedProducts", ArrayList(productosSeleccionados))
            }
            startActivity(intent)
        }
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
        productAdapter = ProductAdapter(
            products = filteredProducts,
            onProductClick = { producto ->
                // Puedes agregar una acción al hacer clic en el producto si lo deseas
            },
            onDeleteClick = { producto ->
                eliminarUnidad(producto)
            }
        )
        productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListadoActivity)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun eliminarUnidad(producto: Producto) {
        val currentProducts = filteredProducts.toMutableList()
        val index = currentProducts.indexOfFirst { it.sku == producto.sku }

        if (index != -1) {
            val productoActual = currentProducts[index]
            if (productoActual.cantidad > 1) {
                // Si hay más de una unidad, reducir la cantidad
                productoActual.cantidad--
                currentProducts[index] = productoActual
            } else {
                // Si solo hay una unidad, eliminar el producto
                currentProducts.removeAt(index)
            }

            filteredProducts = currentProducts
            productAdapter.updateProducts(currentProducts)
            updateTotal(currentProducts)
        }
    }

    private fun loadAndFilterProducts(isLactoseIntolerant: Boolean, isCeliac: Boolean) {
        val allProducts = mutableListOf<Producto>()
        val collections = listOf(
            "Leches",
            "Arroz_Legumbres",
            "Helados",
            "Pastas_Salsas"
        )
        var completedCollections = 0

        collections.forEach { collectionName ->
            firestore.collection(collectionName)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    for (document: DocumentSnapshot in querySnapshot.documents) {
                        try {
                            val producto = Producto(
                                sku = document.getString("sku") ?: "",
                                nombre = document.getString("nombre") ?: "",
                                marca = document.getString("marca") ?: "",
                                precio = document.getString("precio")?.toDoubleOrNull() ?: 0.0,
                                imagen = document.getString("imagen") ?: "",
                                pagina = document.getLong("pagina")?.toInt() ?: 0,
                                categoria = collectionName,
                                documentId = document.id
                            )
                            allProducts.add(producto)
                        } catch (e: Exception) {
                            Log.e("ListadoActivity", "Error al procesar producto en $collectionName", e)
                        }
                    }

                    completedCollections++

                    if (completedCollections == collections.size) {
                        allLoadedProducts = allProducts
                        val filteredProducts = filterProductsByBudgetAndRestrictions(
                            allProducts,
                            presupuesto,
                            isLactoseIntolerant,
                            isCeliac
                        )

                        runOnUiThread {
                            this@ListadoActivity.filteredProducts = filteredProducts
                            productAdapter.updateProducts(filteredProducts)
                            updateTotal(filteredProducts)
                        }
                    }
                }
                .addOnFailureListener { e: Exception ->
                    completedCollections++
                    Log.e("ListadoActivity", "Error cargando colección $collectionName", e)
                }
        }
    }

    private fun filterProductsByBudgetAndRestrictions(
        products: List<Producto>,
        budget: Double,
        isLactoseIntolerant: Boolean,
        isCeliac: Boolean
    ): List<Producto> {
        val productsByCategory = mutableMapOf<String, MutableList<Producto>>()
        var currentTotal = 0.0

        // Inicializar listas por categoría
        products.forEach { producto ->
            if (!productsByCategory.containsKey(producto.categoria)) {
                productsByCategory[producto.categoria] = mutableListOf()
            }
        }

        // Ordenar productos por precio en cada categoría
        val sortedProducts = products.sortedBy { it.precio }

        // Primero agregar al menos un producto de cada categoría
        for (producto in sortedProducts) {
            val categoryProducts = productsByCategory[producto.categoria]
            if (categoryProducts?.isEmpty() == true &&
                currentTotal + producto.precio <= budget &&
                isProductoPermitido(producto, isLactoseIntolerant, isCeliac)) {
                categoryProducts.add(producto)
                currentTotal += producto.precio
            }
        }

        // Luego agregar más productos manteniendo balance y respetando el presupuesto
        var continueAdding = true
        val productCounts = mutableMapOf<String, Int>()

        while (continueAdding) {
            var addedAny = false

            for (producto in sortedProducts) {
                val count = productCounts.getOrDefault(producto.nombre, 0)
                if (count < 3 && // Máximo 3 del mismo producto
                    currentTotal + producto.precio <= budget &&
                    isProductoPermitido(producto, isLactoseIntolerant, isCeliac)) {

                    productsByCategory[producto.categoria]?.add(producto)
                    currentTotal += producto.precio
                    productCounts[producto.nombre] = count + 1
                    addedAny = true
                }
            }

            if (!addedAny) continueAdding = false
        }

        // Agrupar productos repetidos
        val groupedProducts = productsByCategory.values.flatten()
            .groupBy { it.nombre }
            .map { (_, productos) ->
                productos.first().copy(cantidad = productos.size)
            }

        return groupedProducts
    }

    private fun isProductoPermitido(
        producto: Producto,
        isLactoseIntolerant: Boolean,
        isCeliac: Boolean
    ): Boolean {
        var isPermitido = true

        // Verificar restricción de lactosa
        if (isLactoseIntolerant && producto.categoria == "Leches") {
            isPermitido = producto.nombre.lowercase().contains("sin lactosa") ||
                    producto.nombre.lowercase().contains("sinlactosa") ||
                    producto.nombre.lowercase().contains("deslactosado") ||
                    producto.nombre.lowercase().contains("lactose free")
        }

        // Verificar restricción de gluten
        if (isCeliac && isPermitido) {
            when (producto.categoria) {
                "Pastas_Salsas" -> {
                    if (producto.nombre.lowercase().contains("pasta") ||
                        producto.nombre.lowercase().contains("fideos") ||
                        producto.nombre.lowercase().contains("lasaña") ||
                        producto.nombre.lowercase().contains("lasagna") ||
                        producto.nombre.lowercase().contains("ravioles") ||
                        producto.nombre.lowercase().contains("tallarines")
                    ) {
                        isPermitido = producto.nombre.lowercase().contains("sin gluten") ||
                                producto.nombre.lowercase().contains("singluten") ||
                                producto.nombre.lowercase().contains("gluten free")
                    } else if (producto.nombre.lowercase().contains("salsa") ||
                        producto.nombre.lowercase().contains("tomate") ||
                        producto.nombre.lowercase().contains("ketchup")
                    ) {
                        isPermitido = true
                    }
                }
                "Arroz_Legumbres" -> {
                    isPermitido = producto.nombre.lowercase().contains("arroz") ||
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
            }
        }

        return isPermitido
    }

    private fun updateTotal(products: List<Producto>) {
        val total = products.sumOf { it.precio * it.cantidad }
        totalTextView.text = getString(
            R.string.total_format_with_budget,
            String.format("%.0f", total),
            String.format("%.0f", presupuesto)
        )
    }

    private fun mostrarDialogoConfirmacion() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Advertencia")
        builder.setMessage("Al agregar productos similares, el total podría superar el presupuesto establecido. ¿Desea continuar?")

        builder.setPositiveButton("Aceptar") { _, _ ->
            dialogoMostrado = true
            mostrarOpcionesSimilares()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun mostrarOpcionesSimilares() {
        if (filteredProducts.isEmpty()) {
            Toast.makeText(
                this,
                "Primero debes tener productos en tu lista",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val currentTotal = filteredProducts.sumOf { it.precio * it.cantidad }
        val targetPricePerProduct = currentTotal / filteredProducts.size

        val minPrice = targetPricePerProduct * 0.8
        val maxPrice = targetPricePerProduct * 1.2

        val opcionesSimilares = allLoadedProducts.filter { producto ->
            val precioEnRango = producto.precio in minPrice..maxPrice
            val cumpleRestricciones = when {
                // Para productos lácteos cuando hay restricción de lactosa
                sinLactosaSwitch.isChecked && producto.categoria == "Leches" -> {
                    producto.nombre.lowercase().contains("sin lactosa") ||
                            producto.nombre.lowercase().contains("sinlactosa") ||
                            producto.nombre.lowercase().contains("deslactosado") ||
                            producto.nombre.lowercase().contains("lactose free")
                }
                // Para helados, solo mostrar si no hay restricción de lactosa
                producto.categoria == "Helados" -> !sinLactosaSwitch.isChecked
                // Para productos con gluten
                sinGlutenSwitch.isChecked && (producto.categoria == "Pastas_Salsas" ||
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
                else -> true // Si no hay restricciones
            }

            precioEnRango && cumpleRestricciones
        }

        val opcionesOrdenadas = opcionesSimilares.sortedBy {
            Math.abs(it.precio - targetPricePerProduct)
        }.take(10)

        if (opcionesOrdenadas.isEmpty()) {
            Toast.makeText(
                this,
                "No se encontraron opciones similares que cumplan con las restricciones dietéticas",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Crear un mapa con los productos existentes, usando el nombre como clave
        val productosAcumulados = filteredProducts.groupBy {
            it.nombre
        }.mapValues { (_, productos) ->
            productos.first().copy(cantidad = productos.sumOf { it.cantidad })
        }.toMutableMap()

        // Agregar o actualizar con las nuevas opciones
        var productosAgregados = 0
        opcionesOrdenadas.forEach { nuevoProducto ->
            val productoExistente = productosAcumulados[nuevoProducto.nombre]
            if (productoExistente != null) {
                // Si el producto ya existe, aumentar su cantidad
                productosAcumulados[nuevoProducto.nombre] = productoExistente.copy(
                    cantidad = productoExistente.cantidad + 1
                )
            } else {
                // Si es un producto nuevo, agregarlo al mapa
                productosAcumulados[nuevoProducto.nombre] = nuevoProducto.copy(cantidad = 1)
            }
            productosAgregados++
        }

        // Convertir el mapa actualizado a lista
        val nuevaLista = productosAcumulados.values.toList()

        // Actualizar la lista filtrada y la vista
        filteredProducts = nuevaLista
        productAdapter.updateProducts(nuevaLista)
        updateTotal(nuevaLista)

        Toast.makeText(
            this,
            "Se agregaron $productosAgregados productos similares a tu lista",
            Toast.LENGTH_SHORT
        ).show()
    }
}
