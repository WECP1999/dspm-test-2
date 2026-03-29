package com.udb.examenparcial2.data.repository

import android.net.Uri
import com.udb.examenparcial2.data.model.Destination
import com.udb.examenparcial2.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firestore CRUD operations for [Destination] documents.
 *
 * [getDestinations] returns a real-time [Flow] backed by a Firestore snapshot listener,
 * so the UI automatically reacts to remote changes.
 * All write operations are suspending functions returning a [Resource].
 */
class DestinationRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val destinationsCollection = firestore.collection(COLLECTION_DESTINATIONS)

    // ─────────────────────────────── READ ────────────────────────────────────

    /**
     * Emits the full list of destinations in real time, ordered alphabetically by name.
     * Emits [Resource.Loading] once at start, then [Resource.Success] or [Resource.Error]
     * on every Firestore update.
     *
     * The Firestore listener is automatically removed when the collector's scope is cancelled
     * (e.g. when the ViewModel is cleared).
     */
    fun getDestinations(): Flow<Resource<List<Destination>>> = callbackFlow {
        trySend(Resource.Loading())

        val listenerRegistration = destinationsCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching destinations."))
                    return@addSnapshotListener
                }
                val destinations = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Destination::class.java)
                } ?: emptyList()
                trySend(Resource.Success(destinations))
            }

        awaitClose { listenerRegistration.remove() }
    }

    // ─────────────────────────────── CREATE ──────────────────────────────────

    /**
     * Creates a new Firestore document with the provided data.
     * If [imageUri] is provided, its string representation is used as the imageUrl.
     */
    suspend fun createDestination(
        destination: Destination,
        imageUri: String
    ): Resource<Unit> {
        return try {
            val imageUrl = imageUri.toString()
            val newDestination = destination.copy(imageUrl = imageUrl)
            destinationsCollection.add(newDestination.toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create destination.", e)
        }
    }

    // ─────────────────────────────── UPDATE ──────────────────────────────────

    /**
     * Updates an existing Firestore document identified by [destination.id].
     * If [newImageUri] is provided, its string representation replaces the old URL.
     */
    suspend fun updateDestination(
        destination: Destination,
        newImageUri: String? = null
    ): Resource<Unit> {
        return try {
            val imageUrl = newImageUri?.toString() ?: destination.imageUrl
            val updatedData = destination.copy(imageUrl = imageUrl).toMap()
            destinationsCollection.document(destination.id).set(updatedData).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update destination.", e)
        }
    }

    // ─────────────────────────────── DELETE ──────────────────────────────────

    /**
     * Deletes the Firestore document with the given [id].
     */
    suspend fun deleteDestination(id: String): Resource<Unit> {
        return try {
            destinationsCollection.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete destination.", e)
        }
    }

    // ─────────────────────────────── CONSTANTS ───────────────────────────────

    companion object {
        private const val COLLECTION_DESTINATIONS = "destinations"
    }
}
