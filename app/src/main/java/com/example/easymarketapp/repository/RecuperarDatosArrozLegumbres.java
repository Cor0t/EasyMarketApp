package com.example.easymarketapp.repository;

public class RecuperarDatosArrozLegumbres extends RecuperarDatosBase {
    @Override
    protected String getUrl(int pagina) {
        return String.format(
                "https://www.lider.cl/supermercado/category/Despensa/Arroz_y_Legumbres?page=%d",
                pagina
        );
    }

    @Override
    protected String getCollectionName() {
        return "Arroz_Legumbres";
    }
}