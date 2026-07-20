package com.example.agriguard.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Test helper for Firebase Authentication state management in Espresso tests.
 *
 * Usage:
 *   FirebaseTestAuthHelper.signOut()
 *   FirebaseTestAuthHelper.currentUser()
 */
object FirebaseTestAuthHelper {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /** Signs out the currently authenticated user. Safe to call if not signed in. */
    fun signOut() {
        auth.signOut()
    }

    /** Returns the currently authenticated [FirebaseUser], or null if signed out. */
    fun currentUser(): FirebaseUser? = auth.currentUser

    /** Returns true if a user is currently authenticated. */
    fun isSignedIn(): Boolean = auth.currentUser != null

    /**
     * Signs in with email/password synchronously using a CountDownLatch.
     * Use only in test setup — never in production code.
     *
     * @param email    Test account email address
     * @param password Test account password
     * @param timeout  Max wait time in milliseconds (default 10_000)
     */
    fun signInSync(email: String, password: String, timeout: Long = 10_000L) {
        val latch = java.util.concurrent.CountDownLatch(1)
        var error: Exception? = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { latch.countDown() }
            .addOnFailureListener { e ->
                error = e
                latch.countDown()
            }

        latch.await(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
        error?.let { throw it }
    }

    /**
     * Creates a new test user account synchronously.
     * Clean up after tests by calling [deleteCurrentUserSync].
     */
    fun createUserSync(email: String, password: String, timeout: Long = 10_000L) {
        val latch = java.util.concurrent.CountDownLatch(1)
        var error: Exception? = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { latch.countDown() }
            .addOnFailureListener { e ->
                error = e
                latch.countDown()
            }

        latch.await(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
        error?.let { throw it }
    }

    /**
     * Deletes the currently signed-in test user from Firebase Auth.
     * Call in @After to keep the test environment clean.
     */
    fun deleteCurrentUserSync(timeout: Long = 10_000L) {
        val user = auth.currentUser ?: return
        val latch = java.util.concurrent.CountDownLatch(1)

        user.delete()
            .addOnCompleteListener { latch.countDown() }

        latch.await(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
}
