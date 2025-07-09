package devoid.secure.dm.data.auth

import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.AuthRepository
import devoid.secure.dm.domain.model.UserClass
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthRepoImpl(private val dispatcher: CoroutineDispatcher, private val supabaseClient: SupabaseClient) :
    AuthRepository {
    override suspend fun signUpEmail(email: String, password: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override suspend fun signInEmail(email: String, password: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override suspend fun resetPassword(email: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                supabaseClient.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override suspend fun signOut(): Result<Unit> =
    withContext(dispatcher)
    {
        try {
            supabaseClient.realtime.removeAllChannels()
            supabaseClient.auth.signOut()
            supabaseClient.auth.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): UserInfo?{
       return supabaseClient.auth.currentUserOrNull()
    }

    override  fun getProvider(): String?{
       val appMetaData =  supabaseClient.auth.currentUserOrNull()?.appMetadata
        return appMetaData?.get("provider")?.toString()
    }
}

