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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val limpiar = LimpiarFireBase()
        limpiar.eliminarDocumentosDeColecciones(object : OnCompleteListener<Void> {
            override fun onComplete(task: Task<Void>) {
                if (task.isSuccessful) {
                    val api = RecuperarTodasApi()
                    api.llamarCargarMultiplesPaginas()
                } else {
                    Toast.makeText(this@MenuActivity, "Error al eliminar documentos", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Referencia a los componentes de la interfaz
        val comenzarButton: MaterialButton = findViewById(R.id.buttonStart)
        val lactoseIntolerantCheckBox: MaterialCheckBox = findViewById(R.id.checkboxLactoseIntolerant)
        val celiacCheckBox: MaterialCheckBox = findViewById(R.id.checkboxCeliac)
        val presupuestoEditText: TextInputEditText = findViewById(R.id.editTextBudget)

        comenzarButton.setOnClickListener {
            val isLactoseIntolerant = lactoseIntolerantCheckBox.isChecked
            val isCeliac = celiacCheckBox.isChecked
            val presupuesto = presupuestoEditText.text.toString().toDoubleOrNull()

            if (presupuesto == null) {
                Toast.makeText(this, "Por favor, ingrese un presupuesto válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mensaje = buildString {
                append("Opciones seleccionadas:\n")
                if (isLactoseIntolerant) append("- Intolerante a la lactosa\n")
                if (isCeliac) append("- Celíaco\n")
                append("Presupuesto: $${NumberFormat.getNumberInstance(Locale("CL")).format(presupuesto)}")
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
