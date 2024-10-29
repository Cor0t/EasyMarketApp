package com.example.easymarketapp
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val comenzarButton: MaterialButton = findViewById(R.id.buttonStart)
        val lactoseIntolerantCheckBox: MaterialCheckBox = findViewById(R.id.checkboxLactoseIntolerant)
        val presupuestoEditText: TextInputEditText = findViewById(R.id.editTextBudget)

        comenzarButton.setOnClickListener {
            val isLactoseIntolerant = lactoseIntolerantCheckBox.isChecked
            val presupuesto = presupuestoEditText.text.toString().toDoubleOrNull()

            if (presupuesto == null) {
                Toast.makeText(this, "Por favor, ingrese un presupuesto válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mensaje = buildString {
                append("Opciones seleccionadas:\n")
                if (isLactoseIntolerant) append("- Intolerante a la lactosa\n")
                append("Presupuesto: $${NumberFormat.getNumberInstance(Locale("CL")).format(presupuesto)}")
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

            // Create an Intent to start ListadoActivity
            val intent = Intent(this, ListadoActivity::class.java)

            // Pass data to ListadoActivity
            intent.putExtra("isLactoseIntolerant", isLactoseIntolerant)
            intent.putExtra("presupuesto", presupuesto)

            // Start ListadoActivity
            startActivity(intent)
        }
    }
}