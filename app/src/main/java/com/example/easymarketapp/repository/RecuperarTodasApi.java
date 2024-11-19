package com.example.easymarketapp.repository;

public class RecuperarTodasApi {
    public void llamarCargarMultiplesPaginas() {
        RecuperarDatosHelados RecuperarDatosHelados = new RecuperarDatosHelados();
        RecuperarDatosArrozLegumbres RecuperarDatosArrozLegumbres = new RecuperarDatosArrozLegumbres();
        RecuperarDatosPastasSalsas RecuperarDatosPastasSalsas = new RecuperarDatosPastasSalsas();
        RecuperarDatosLeches RecuperarDatosLeches = new RecuperarDatosLeches();

        RecuperarDatosLeches.cargarMultiplesPaginas(1,4);
        RecuperarDatosArrozLegumbres.cargarMultiplesPaginas(1,4);
        RecuperarDatosHelados.cargarMultiplesPaginas(1,4);
        RecuperarDatosPastasSalsas.cargarMultiplesPaginas(1,4);
    }
}