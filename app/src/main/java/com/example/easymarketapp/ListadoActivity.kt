package com.example.easymarketapp


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
    private lateinit var productAdapter: ProductAdapter
    private var filteredProducts: List<Producto> = emptyList()
    private var presupuesto: Double = 0.0
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

        Log.d("ListadoActivity", "Iniciando carga de productos de ${collections.size} colecciones")

        collections.forEach { collectionName ->
            Log.d("ListadoActivity", "Intentando cargar colección: $collectionName")

            firestore.collection(collectionName)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    val productsInCollection = querySnapshot.documents.size
                    Log.d("ListadoActivity", "Productos encontrados en $collectionName: $productsInCollection")

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
                    Log.d("ListadoActivity", "Colección $collectionName cargada. Completadas: $completedCollections/${collections.size}")

                    if (completedCollections == collections.size) {
                        Log.d("ListadoActivity", "Todas las colecciones cargadas. Total productos: ${allProducts.size}")

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

                            Toast.makeText(
                                this@ListadoActivity,
                                "Se encontraron ${filteredProducts.size} productos dentro de tu presupuesto",
                                Toast.LENGTH_SHORT
                            ).show()
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

    private fun setupListeners() {
        // Establecer estados iniciales según los valores recibidos
        sinLactosaSwitch.isChecked = intent.getBooleanExtra("isLactoseIntolerant", false)
        sinGlutenSwitch.isChecked = intent.getBooleanExtra("isCeliac", false)

        // Configurar listeners para ambos switches
        sinLactosaSwitch.setOnCheckedChangeListener { _, isChecked ->
            loadAndFilterProducts(isChecked, sinGlutenSwitch.isChecked)
        }

        sinGlutenSwitch.setOnCheckedChangeListener { _, isChecked ->
            loadAndFilterProducts(sinLactosaSwitch.isChecked, isChecked)
        }
    }
}