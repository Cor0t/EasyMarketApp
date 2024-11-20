package com.example.easymarketapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easymarketapp.repository.LimpiarFireBase
import com.example.easymarketapp.repository.RecuperarTodasApi
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.text.NumberFormat
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    private lateinit var presupuestoEditText: TextInputEditText
    private lateinit var lactoseIntolerantCheckBox: MaterialCheckBox
    private lateinit var celiacCheckBox: MaterialCheckBox
    private lateinit var comenzarButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Inicializar vistas
        presupuestoEditText = findViewById(R.id.editTextBudget)
        lactoseIntolerantCheckBox = findViewById(R.id.checkboxLactoseIntolerant)
        celiacCheckBox = findViewById(R.id.checkboxCeliac)
        comenzarButton = findViewById(R.id.buttonStart)

        comenzarButton.setOnClickListener {
            val presupuestoText = presupuestoEditText.text.toString()

            if (presupuestoText.isBlank()) {
                presupuestoEditText.error = "Por favor, ingrese un presupuesto"
                return@setOnClickListener
            }

            val presupuesto = try {
                presupuestoText.toDouble()
            } catch (e: NumberFormatException) {
                presupuestoEditText.error = "Por favor, ingrese un número válido"
                return@setOnClickListener
            }

            if (presupuesto <= 0) {
                presupuestoEditText.error = "El presupuesto debe ser mayor a 0"
                return@setOnClickListener
            }

            val isLactoseIntolerant = lactoseIntolerantCheckBox.isChecked
            val isCeliac = celiacCheckBox.isChecked

            val mensaje = buildString {
                append("Opciones seleccionadas:\n")
                if (isLactoseIntolerant) append("- Intolerante a la lactosa\n")
                if (isCeliac) append("- Celíaco\n")
                append("Presupuesto: $${String.format("%,.0f", presupuesto)}")
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

            val intent = Intent(this, ListadoActivity::class.java).apply {
                putExtra("isLactoseIntolerant", isLactoseIntolerant)
                putExtra("isCeliac", isCeliac)
                putExtra("presupuesto", presupuesto)
            }

            startActivity(intent)
        }
    }
}