package com.example.easymarketapp.repository;

public class RecuperarDatosPastasSalsas extends RecuperarDatosBase {
    @Override
    protected String getUrl(int pagina) {
        return String.format(
                "https://www.lider.cl/supermercado/category/Despensa/Pastas_y_Salsas?page=%d",
                pagina
        );
    }

    @Override
    protected String getCollectionName() {
        return "Pastas_Salsas";
    }
}