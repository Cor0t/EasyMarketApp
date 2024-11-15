package com.example.easymarketapp.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecuperarDatosBebidas {
    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    // Método para cargar los productos de una página específica
    public void cargarBebidasEnFirebase(int pagina) {
        String url = String.format(
                "https://www.lider.cl/supermercado/category/Frescos_y_Lácteos/Leche?page=%d",
                pagina
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    Document document = Jsoup.parse(html);

                    // Seleccionamos el script con JSON-LD
                    Elements scriptElements = document.select("script[type=application/ld+json]");
                    if (!scriptElements.isEmpty()) {
                        Element jsonScript = scriptElements.first();
                        String jsonData = jsonScript.html();

                        // Parseamos el JSON-LD
                        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                        JsonArray itemList = jsonObject.getAsJsonArray("itemListElement");

                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                        // Iteramos sobre los productos
                        for (int i = 0; i < itemList.size(); i++) {
                            JsonObject item = itemList.get(i).getAsJsonObject()
                                    .getAsJsonObject("item");

                            // Extraemos datos del producto
                            String nombre = item.get("name").getAsString();
                            String marca = item.has("brand") ?
                                    item.getAsJsonObject("brand").get("name").getAsString() : "Marca desconocida";
                            String precio = item.getAsJsonObject("offers").get("price").getAsString();
                            String imagen = item.has("image") ? item.get("image").getAsString() : "";
                            String sku = item.has("sku") ? item.get("sku").getAsString() : "SKU no disponible";
                            String urlProducto = item.has("url") ? item.get("url").getAsString() : "";

                            // Crear mapa de datos del producto
                            HashMap<String, Object> productoData = new HashMap<>();
                            productoData.put("nombre", nombre);
                            productoData.put("marca", marca);
                            productoData.put("precio", precio);
                            productoData.put("imagen", imagen);
                            productoData.put("sku", sku);
                            productoData.put("urlProducto", urlProducto);
                            productoData.put("pagina", pagina);

                            // Guardar datos en Firebase
                            firestore.collection("Bebidas")
                                    .document(sku)
                                    .set(productoData)
                                    .addOnSuccessListener(unused ->
                                            System.out.println("Producto guardado exitosamente: " + nombre + " (Página " + pagina + ")"))
                                    .addOnFailureListener(e ->
                                            System.err.println("Error al guardar producto: " + nombre + " - " + e.getMessage()));
                        }
                    } else {
                        System.err.println("No se encontró JSON-LD en la página " + pagina);
                    }
                } else {
                    System.err.println("Error en la respuesta para página " + pagina + ": " + response.code());
                }
            } catch (IOException e) {
                System.err.println("Error procesando página " + pagina + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // Método para cargar productos de múltiples páginas
    public void cargarMultiplesPaginas(int inicio, int fin) {
        for (int pagina = inicio; pagina <= fin; pagina++) {
            final int paginaActual = pagina;
            new Thread(() -> {
                try {
                    Thread.sleep(1000 * (paginaActual - inicio)); // Delay para evitar sobrecargar el servidor
                    cargarBebidasEnFirebase(paginaActual);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
