package com.example.easymarketapp.repository;

import com.example.easymarketapp.repository.RecuperarDatosBebidas; // Asegúrate de importar la clase
import com.example.easymarketapp.repository.RecuperarDatosHelados;
import com.example.easymarketapp.repository.RecuperarDatosArrozLegumbres;
import com.example.easymarketapp.repository.RecuperarDatosLeches;
import com.example.easymarketapp.repository.RecuperarDatosPastasSalsas;
public class RecuperarTodasApi {

    public void llamarCargarMultiplesPaginas() {
        // Crear una instancia de la clase RecuperarDatos
        RecuperarDatosBebidas RecuperarDatosBebidas = new RecuperarDatosBebidas();
        RecuperarDatosHelados RecuperarDatosHelados = new RecuperarDatosHelados();
        RecuperarDatosArrozLegumbres RecuperarDatosArrozLegumbres = new RecuperarDatosArrozLegumbres();
        RecuperarDatosPastasSalsas RecuperarDatosPastasSalsas = new RecuperarDatosPastasSalsas();
        RecuperarDatosLeches RecuperarDatosLeches = new RecuperarDatosLeches();
        // Llamar al método cargarMultiplesPaginas desde la instancia de RecuperarDatos
        RecuperarDatosBebidas.cargarMultiplesPaginas(1,3);
        RecuperarDatosLeches.cargarMultiplesPaginas(1,3);
        RecuperarDatosArrozLegumbres.cargarMultiplesPaginas(1,3);
        RecuperarDatosHelados.cargarMultiplesPaginas(1,3);
        RecuperarDatosPastasSalsas.cargarMultiplesPaginas(1,3);
    }
}
