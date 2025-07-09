package devoid.secure.dm.domain.model

import io.github.jan.supabase.auth.user.UserInfo


interface AuthRepository {
    suspend fun signUpEmail(email:String, password:String):Result<Unit>
    suspend fun signInEmail(email:String, password:String):Result<Unit>
    suspend fun resetPassword(email: String):Result<Unit>
    suspend fun getCurrentUser(): UserInfo?
    fun getProvider(): String?
    suspend fun signOut():Result<Unit>
}