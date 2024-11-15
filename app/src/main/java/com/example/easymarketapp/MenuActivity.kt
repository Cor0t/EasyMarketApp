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
import java.text.NumberFormat
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //se establece diseño con interfaz activity_menu
        setContentView(R.layout.activity_menu)

        //Referencia a los componentes de la interfaz
        val comenzarButton: MaterialButton = findViewById(R.id.buttonStart)
        val lactoseIntolerantCheckBox: MaterialCheckBox = findViewById(R.id.checkboxLactoseIntolerant)
        val presupuestoEditText: TextInputEditText = findViewById(R.id.editTextBudget)

        //cuando se haga click, se ejecuta comenzarButton
        comenzarButton.setOnClickListener {
            val limpiar = LimpiarFireBase()
            limpiar.eliminarDocumentosDeColecciones()

            val api = RecuperarTodasApi()
            api.llamarCargarMultiplesPaginas()
            


            //Verifica checkbox y convierte el texto a double
            val isLactoseIntolerant = lactoseIntolerantCheckBox.isChecked
            val presupuesto = presupuestoEditText.text.toString().toDoubleOrNull()

            //Si no es valido, pide ingresar nuevamente
            if (presupuesto == null) {
                Toast.makeText(this, "Por favor, ingrese un presupuesto válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Si es valido crea un mensaje
            val mensaje = buildString {
                append("Opciones seleccionadas:\n")
                if (isLactoseIntolerant) append("- Intolerante a la lactosa\n")
                append("Presupuesto: $${NumberFormat.getNumberInstance(Locale("CL")).format(presupuesto)}")
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

            // Crea un intent Para iniciar ListadoActivity (this=MenuActivity, destino)
            val intent = Intent(this, ListadoActivity::class.java)

            //  Pasa los valores a ListadoActivity (Pasa los datos y se recuperan en ListadoActivity con get)
            intent.putExtra("isLactoseIntolerant", isLactoseIntolerant)
            intent.putExtra("presupuesto", presupuesto)

            // Inicia ListadoActivity
            startActivity(intent)
        }
    }
}