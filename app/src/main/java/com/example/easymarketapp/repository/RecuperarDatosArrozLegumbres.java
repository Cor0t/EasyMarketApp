package com.example.easymarketapp.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class RecuperarDatosArrozLegumbres {
    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    public void cargarProductosEnFirebase(int pagina) {
        // URL adaptada a la nueva API (Arroz y Legumbres)
        String url = String.format(
                "https://www.lider.cl/supermercado/category/Despensa/Arroz_y_Legumbres?page=%d",
                pagina
        );

        Request request = new Request.Builder()
                .url(url)
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("accept-language", "es-CL,es;q=0.9")
                .header("cache-control", "max-age=0")
                .header("cookie", "vtc=Z_52Gzfyfh5woE6ZoaZR0w; _pxvid=841428b0-6192-11ef-afd2-11f96365fbf0; ...")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    Document document = Jsoup.parse(html);

                    Elements scriptElements = document.select("script[type=application/ld+json]");
                    if (!scriptElements.isEmpty()) {
                        Element jsonScript = scriptElements.first();
                        String jsonData = jsonScript.html();

                        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                        JsonArray itemList = jsonObject.getAsJsonArray("itemListElement");

                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                        for (int i = 0; i < itemList.size(); i++) {
                            JsonObject item = itemList.get(i).getAsJsonObject()
                                    .getAsJsonObject("item");

                            String nombre = item.get("name").getAsString();
                            String marca = item.getAsJsonObject("brand")
                                    .get("name").getAsString();
                            String precio = item.getAsJsonObject("offers")
                                    .get("price").getAsString();
                            String imagen = item.get("image").getAsString();
                            String sku = item.get("sku").getAsString();

                            HashMap<String, Object> productoData = new HashMap<>();
                            productoData.put("nombre", nombre);
                            productoData.put("marca", marca);
                            productoData.put("precio", precio);
                            productoData.put("imagen", imagen);
                            productoData.put("sku", sku);
                            productoData.put("pagina", pagina); // Agregamos la página como referencia

                            firestore.collection("Arroz_Legumbres")  // Cambié el nombre de la colección aquí
                                    .document(sku)
                                    .set(productoData)
                                    .addOnSuccessListener(unused ->
                                            System.out.println("Producto guardado exitosamente: " + nombre + " (Página " + pagina + ")"))
                                    .addOnFailureListener(e ->
                                            System.err.println("Error al guardar producto: " + nombre + " - " + e.getMessage()));
                        }
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

    // Método para cargar múltiples páginas
    public void cargarMultiplesPaginas(int inicio, int fin) {
        for (int pagina = inicio; pagina <= fin; pagina++) {
            final int paginaActual = pagina;
            // Agregamos un pequeño delay entre páginas para no sobrecargar el servidor
            new Thread(() -> {
                try {
                    Thread.sleep(1000 * (paginaActual - inicio)); // 1 segundo de delay entre páginas
                    cargarProductosEnFirebase(paginaActual);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
