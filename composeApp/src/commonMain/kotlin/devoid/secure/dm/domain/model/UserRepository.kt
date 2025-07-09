package devoid.secure.dm.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: StateFlow<UserClass.LocalUser?>
    fun getUserById(uid: String): Flow<UserClass.LocalUser?>
    fun updateUser(user: UserClass.LocalUser): Flow<Result<Unit>>
    fun validateUserName(uName: String): Flow<Result<Boolean>>
    suspend fun updateFCMToken(token: String): Result<Unit>
}