import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easymarketapp.R

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val comenzarButton: Button = findViewById(R.id.comenzarButton)
        val vegetarianoCheckBox: CheckBox = findViewById(R.id.vegetarianoCheckBox)
        val celiacoCheckBox: CheckBox = findViewById(R.id.celiacoCheckBox)
        val presupuestoEditText: EditText = findViewById(R.id.presupuestoEditText)

        comenzarButton.setOnClickListener {
            val isVegetariano = vegetarianoCheckBox.isChecked
            val isCeliaco = celiacoCheckBox.isChecked
            val presupuesto = presupuestoEditText.text.toString().toDoubleOrNull()

            if (presupuesto == null) {
                Toast.makeText(this, "Por favor, ingrese un presupuesto válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mensaje = buildString {
                append("Opciones seleccionadas:\n")
                if (isVegetariano) append("- Vegetariano\n")
                if (isCeliaco) append("- Celiaco\n")
                append("Presupuesto: $${String.format("%.2f", presupuesto)}")
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

            // Create an Intent to start ListadoActivity
            val intent = Intent(this, ListadoActivity::class.java)

            // Pass data to ListadoActivity if needed
            intent.putExtra("isVegetariano", isVegetariano)
            intent.putExtra("isCeliaco", isCeliaco)
            intent.putExtra("presupuesto", presupuesto)

            // Start ListadoActivity
            startActivity(intent)
        }
    }
}