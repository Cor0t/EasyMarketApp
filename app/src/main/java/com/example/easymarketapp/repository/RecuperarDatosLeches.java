package com.example.easymarketapp.repository;


public class RecuperarDatosLeches extends RecuperarDatosBase {
    @Override
    protected String getUrl(int pagina) {
        return String.format(
                "https://www.lider.cl/supermercado/category/Frescos_y_Lácteos/Leche?page=%d",
                pagina
        );
    }

    @Override
    protected String getCollectionName() {
        return "Leches";
    }
}