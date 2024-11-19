package com.example.easymarketapp.repository;

public class RecuperarDatosHelados extends RecuperarDatosBase {
    @Override
    protected String getUrl(int pagina) {
        String url = String.format(
                "https://www.lider.cl/supermercado/category/Congelados/Helados?page=%d",
                pagina
        );
        System.out.println("URL Helados: " + url);
        return url;
    }

    @Override
    protected String getCollectionName() {
        return "Helados";
    }
}