package com.babyygurll.admin.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirebaseAuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentEmail: String?
        get() = auth.currentUser?.email

    val currentUid: String?
        get() = auth.currentUser?.uid

    /**
     * Signs in with email and password via Firebase Auth.
     */
    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("Authentication succeeded but user profile was null")))
                    }
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }

    /**
     * Creates a new administrator account in Firebase Auth.
     */
    suspend fun createAccount(email: String, pass: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("Registration succeeded but user profile was null")))
                    }
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }

    /**
     * Sends a password reset email to the given address via Firebase.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }

    /**
     * Signs out of Firebase Auth.
     */
    fun signOut() {
        auth.signOut()
    }
}

