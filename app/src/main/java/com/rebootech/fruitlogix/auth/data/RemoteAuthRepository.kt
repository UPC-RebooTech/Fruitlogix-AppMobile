package com.rebootech.fruitlogix.auth.data

import android.content.Context
import android.util.Log
import com.rebootech.fruitlogix.auth.domain.model.AccountProfile
import com.rebootech.fruitlogix.auth.domain.model.AuthError
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.Session
import com.rebootech.fruitlogix.auth.domain.model.SignUpRequest
import com.rebootech.fruitlogix.auth.domain.repository.AccountProfileStore
import com.rebootech.fruitlogix.auth.domain.repository.AuthRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.FruitLogixApiService
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.SignInRequestDto
import com.rebootech.fruitlogix.shared.core.network.SignUpRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Sign in and sign up against the FruitLogix server (IAM).
 * The server stores username and password only; role, name and email stay on the phone
 * in [AccountProfileStore].
 */
class RemoteAuthRepository(
    private val api: FruitLogixApiService,
    private val accounts: AccountProfileStore
) : AuthRepository {

    override suspend fun signIn(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val user = username.trim().lowercase()
        val response = try {
            api.signIn(SignInRequestDto(user, password))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return@withContext AuthResult.Failure(e.toAuthError())
        }
        val token = response.token?.takeIf { it.isNotBlank() }
            ?: return@withContext AuthResult.Failure(AuthError.Unknown)
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)

        val name = response.username?.takeIf { it.isNotBlank() }?.lowercase() ?: user
        val profile = accounts.get(name) ?: return@withContext AuthResult.NeedsRole(name, token)
        AuthResult.Success(Session(username = name, displayName = profile.displayName, role = profile.role, token = token))
    }

    override suspend fun signUp(request: SignUpRequest): AuthResult {
        val user = request.username.trim().lowercase()
        val created = withContext(Dispatchers.IO) {
            try {
                val response = api.signUp(SignUpRequestDto(user, request.password))
                response.errorBody()?.close()
                response.body()?.close()
                if (response.isSuccessful) null else statusToError(response.code())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                e.toAuthError()
            }
        }
        if (created != null) return AuthResult.Failure(created)

        accounts.save(
            AccountProfile(
                username = user,
                displayName = request.fullName.trim(),
                role = request.role,
                email = request.email.trim(),
                organizationName = request.organizationName.trim()
            )
        )
        // The account exists now: sign in to get the token
        return signIn(user, request.password)
    }

    /** Used after [AuthResult.NeedsRole]: remembers the role on this phone and opens the session. */
    fun completeWithRole(username: String, token: String, profile: AccountProfile): Session {
        accounts.save(profile)
        return Session(username = username, displayName = profile.displayName, role = profile.role, token = token)
    }

    private fun Throwable.toAuthError(): AuthError {
        // Only the exception type and status code: no usernames, passwords or tokens in logcat
        Log.w(TAG, "Auth call failed: ${javaClass.simpleName} ${(this as? HttpException)?.code() ?: ""}")
        return when (this) {
            is HttpException -> statusToError(code())
            is IOException -> AuthError.Network
            else -> AuthError.Unknown
        }
    }

    private fun statusToError(code: Int): AuthError = when (code) {
        400, 401 -> AuthError.InvalidCredentials
        409 -> AuthError.UsernameTaken
        // Render answers these while the server is waking up
        502, 503, 504 -> AuthError.Network
        else -> AuthError.Unknown
    }.also { if (it == AuthError.Unknown) Log.w(TAG, "Unexpected status from the server: $code") }

    companion object {
        private const val TAG = "FruitLogix-Auth"

        @Volatile private var instance: RemoteAuthRepository? = null

        fun getInstance(context: Context): RemoteAuthRepository =
            instance ?: synchronized(this) {
                instance ?: RemoteAuthRepository(RetrofitClient.api, PrefsAccountProfileStore(context)).also { instance = it }
            }
    }
}
