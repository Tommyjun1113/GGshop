package com.example.shopping.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.shopping.utils.UserSession

object FavoriteRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun favoriteRef() =
        db.collection("users")
            .document(UserSession.documentId)
            .collection("favorites")

    fun addFavorite(productId: String) {
        favoriteRef()
            .document(productId)
            .set(
                mapOf("createdAt" to System.currentTimeMillis()),
                SetOptions.merge()
            )
    }

    fun removeFavorite(productId: String) {
        favoriteRef()
            .document(productId)
            .delete()
    }

    fun getFavorites(
        onResult: (Set<String>) -> Unit
    ) {
        favoriteRef()
            .get()
            .addOnSuccessListener { snapshot ->
                val ids = snapshot.documents.map { it.id }.toSet()
                onResult(ids)
            }
    }
}
