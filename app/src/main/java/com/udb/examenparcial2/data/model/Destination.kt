package com.udb.examenparcial2.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a tourist destination managed by the travel agency.
 *
 * All fields have default values so Firestore can deserialize
 * documents via [DocumentSnapshot.toObject] without a custom constructor.
 * The [id] field is populated automatically by Firestore via [@DocumentId].
 */
data class Destination(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = ""
) {
    /**
     * Converts the model to a plain [Map] suitable for Firestore write operations.
     * The document [id] is intentionally excluded — it is the Firestore document key,
     * not a stored field.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "country" to country,
        "price" to price,
        "description" to description,
        "imageUrl" to imageUrl
    )
}
