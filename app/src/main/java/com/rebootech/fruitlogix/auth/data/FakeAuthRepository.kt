package com.rebootech.fruitlogix.auth.data

import com.rebootech.fruitlogix.auth.domain.model.AuthError
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.Session
import com.rebootech.fruitlogix.auth.domain.model.SignUpRequest
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeAuthRepository : AuthRepository {

    private data class InMemoryUser(
        val username: String,
        val password: String,
        val displayName: String,
        val role: UserRole,
        val email: String
    )

    // DEV ONLY: usuarios de prueba iniciales
    private val users = mutableListOf(
        InMemoryUser(
            username = "alexandra.jimenez",
            password = "Producer123",
            displayName = "Alexandra Jiménez",
            role = UserRole.PRODUCER,
            email = "alexandra.jimenez@santafarms.pe"
        ),
        InMemoryUser(
            username = "carlos.mendoza",
            password = "Distributor123",
            displayName = "Carlos Mendoza",
            role = UserRole.DISTRIBUTOR,
            email = "carlos.mendoza@fruitlogix.pe"
        )
    )

    override suspend fun signIn(username: String, password: String): AuthResult {
        // Retraso simulado de 600–800 ms
        delay(Random.nextLong(600, 800))

        val normalizedUser = username.trim().lowercase()
        val foundUser = synchronized(users) {
            users.find { it.username.lowercase() == normalizedUser }
        }

        return if (foundUser != null && foundUser.password == password) {
            AuthResult.Success(
                Session(
                    username = foundUser.username,
                    displayName = foundUser.displayName,
                    role = foundUser.role
                )
            )
        } else {
            AuthResult.Failure(AuthError.InvalidCredentials)
        }
    }

    override suspend fun signUp(request: SignUpRequest): AuthResult {
        delay(Random.nextLong(600, 800))

        val normalizedUser = request.username.trim().lowercase()
        val normalizedEmail = request.email.trim().lowercase()

        synchronized(users) {
            if (users.any { it.username.lowercase() == normalizedUser }) {
                return AuthResult.Failure(AuthError.UsernameTaken)
            }
            if (users.any { it.email.lowercase() == normalizedEmail }) {
                return AuthResult.Failure(AuthError.EmailTaken)
            }

            val newUser = InMemoryUser(
                username = request.username.trim(),
                password = request.password,
                displayName = request.fullName.trim(),
                role = request.role,
                email = request.email.trim()
            )
            users.add(newUser)

            return AuthResult.Success(
                Session(
                    username = newUser.username,
                    displayName = newUser.displayName,
                    role = newUser.role
                )
            )
        }
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        val normalized = username.trim().lowercase()
        return synchronized(users) {
            users.any { it.username.lowercase() == normalized }
        }
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        val normalized = email.trim().lowercase()
        return synchronized(users) {
            users.any { it.email.lowercase() == normalized }
        }
    }

    companion object {
        val instance: FakeAuthRepository by lazy { FakeAuthRepository() }
    }
}

