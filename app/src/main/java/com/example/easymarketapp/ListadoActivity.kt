import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.R

class ListadoActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var verOpcionesSimilaresButton: Button
    private lateinit var aceptarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        initializeViews()
        setupRecyclerView()
        setupListeners()

        // Retrieve passed data if needed
        val isVegetariano = intent.getBooleanExtra("isVegetariano", false)
        val isCeliaco = intent.getBooleanExtra("isCeliaco", false)
        val presupuesto = intent.getDoubleExtra("presupuesto", 0.0)

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
        productsRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Set up your RecyclerView adapter here
        // Example: productsRecyclerView.adapter = ProductAdapter(productList)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        verOpcionesSimilaresButton.setOnClickListener {
            // TODO: Implement logic to view similar options
        }

        aceptarButton.setOnClickListener {
            // TODO: Implement logic to accept the selection
        }
    }

    private fun updateUI(isVegetariano: Boolean, isCeliaco: Boolean, presupuesto: Double) {
        titleTextView.text = buildString {
            append("Lista de Productos")
            if (isVegetariano) append(" (Vegetariano)")
            if (isCeliaco) append(" (Celiaco)")
        }
        totalTextView.text = "Presupuesto: $${String.format("%.2f", presupuesto)}"
    }
}