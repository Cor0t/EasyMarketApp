package com.example.easymarketapp.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class LimpiarFireBase {

    private FirebaseFirestore firestore;

    public LimpiarFireBase() {
        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();
    }

    public void eliminarDocumentosDeColecciones() {
        // Definir las colecciones a limpiar
        List<String> colecciones = List.of("Bebidas", "Helados", "Leches", "Pastas_Salsas", "Arroz_Legumbres");

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
                                        // Aquí podrías agregar algún código para notificar que la eliminación fue exitosa, si lo necesitas.
                                    })
                                    .addOnFailureListener(e -> {
                                        // Aquí podrías agregar algún código para manejar el error, si lo necesitas.
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Aquí podrías agregar algún código para manejar el error, si lo necesitas.
                    });
        }
    }
}
