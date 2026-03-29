package com.udb.examenparcial2.data.repository

import com.udb.examenparcial2.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository responsible for all Firebase Authentication operations.
 *
 * Every suspending function wraps Firebase Tasks with [kotlinx.coroutines.tasks.await]
 * so they integrate naturally with ViewModels and coroutine scopes.
 */
class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Signs in an existing user with email and password.
     * @return [Resource.Success] with the signed-in [FirebaseUser], or [Resource.Error].
     */
    suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed. Please try again.", e)
        }
    }

    /**
     * Creates a new user account with email and password.
     * @return [Resource.Success] with the created [FirebaseUser], or [Resource.Error].
     */
    suspend fun register(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed. Please try again.", e)
        }
    }

    /** Signs out the currently authenticated user. */
    fun logout() {
        firebaseAuth.signOut()
    }

    /** Returns true if a user is currently signed in. */
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
}
