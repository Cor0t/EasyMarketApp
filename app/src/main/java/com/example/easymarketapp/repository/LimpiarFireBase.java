package com.example.easymarketapp.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;

public class LimpiarFireBase {

    private FirebaseFirestore firestore;

    public LimpiarFireBase() {
        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();
    }

    public void eliminarDocumentosDeColecciones(final OnCompleteListener<Void> listener) {
        // Definir las colecciones a limpiar, quitamos "Bebidas"
        List<String> colecciones = List.of("Helados", "Leches", "Pastas_Salsas", "Arroz_Legumbres");

        // Crear un TaskCompletionSource para monitorear todas las eliminaciones
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        int[] coleccionesPendientes = {colecciones.size()}; // Usamos un array para tener una referencia mutable a la cantidad de colecciones restantes

        // Eliminar documentos de todas las colecciones
        for (String coleccion : colecciones) {
            CollectionReference collectionRef = firestore.collection(coleccion);
            collectionRef.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            WriteBatch batch = firestore.batch();
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                batch.delete(document.getReference());
                            }
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        // Reducimos la cantidad de colecciones pendientes
                                        coleccionesPendientes[0]--;
                                        if (coleccionesPendientes[0] == 0) {
                                            taskCompletionSource.setResult(null); // Llamar cuando todas las colecciones han sido procesadas
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        coleccionesPendientes[0]--;
                                        if (coleccionesPendientes[0] == 0) {
                                            taskCompletionSource.setResult(null); // Aseguramos que se complete incluso en caso de error
                                        }
                                    });
                        } else {
                            // Si la colección está vacía, reducimos la cantidad de colecciones pendientes
                            coleccionesPendientes[0]--;
                            if (coleccionesPendientes[0] == 0) {
                                taskCompletionSource.setResult(null); // Llamar cuando todas las colecciones han sido procesadas
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        coleccionesPendientes[0]--;
                        if (coleccionesPendientes[0] == 0) {
                            taskCompletionSource.setResult(null); // Aseguramos que se complete incluso en caso de error
                        }
                    });
        }

        // Establecemos el listener cuando todas las colecciones hayan sido procesadas
        taskCompletionSource.getTask().addOnCompleteListener(listener);
    }
}