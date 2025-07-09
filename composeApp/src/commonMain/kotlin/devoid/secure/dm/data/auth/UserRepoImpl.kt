package devoid.secure.dm.data.auth

import devoid.secure.dm.domain.model.User
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.domain.model.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserRepoImpl(private val dispatcher: CoroutineDispatcher, private val supabaseClient: SupabaseClient) :
    UserRepository {
    private val _user = MutableStateFlow<UserClass.LocalUser?>(null)
    override val currentUser: StateFlow<UserClass.LocalUser?> = _user.asStateFlow()
    private var sessionStatus: SessionStatus = SessionStatus.Initializing


    init {
        CoroutineScope(Dispatchers.Main).launch {
            val sessionStatusFlow = supabaseClient.auth.sessionStatus
            sessionStatusFlow.collect {
                if (it is SessionStatus.Authenticated) {
                    if (sessionStatus is SessionStatus.NotAuthenticated) {

                    }
                    getCurrentUser().collect { user ->
                        _user.value = user
                    }
                } else if (it is SessionStatus.NotAuthenticated) {
                    _user.value = null
                }
                sessionStatus = it
            }
        }
    }

    override suspend fun updateFCMToken(token: String): Result<Unit> {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return try {
            currentUser?.let { user ->
                withContext(dispatcher) {
                    supabaseClient.from("profiles").update(update = {
                        set("fcm_token", token)
                    }) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                }
            } ?: throw IllegalStateException("Current user is null")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserById(uid: String): Flow<UserClass.LocalUser?> = flow {
        try {
            val user = supabaseClient.from("profiles")
                .select(columns = Columns.list("id", "username", "full_name", "avatar_url", "email", "bio")) {
                    filter {
                        eq("id", uid)
                    }
                }.decodeSingle<UserClass.LocalUser>()
            emit(user)
        } catch (e: Exception) {
            println(e)
            emit(null)
        }
    }.flowOn(dispatcher).conflate()


    private fun getCurrentUser(): Flow<UserClass.LocalUser?> =
        try {
            supabaseClient.auth.currentUserOrNull()?.id?.let { userId ->
                getUserById(userId)
            } ?: flowOf(null)
        } catch (e: Exception) {
            flowOf(null)
        }


    override fun updateUser(user: UserClass.LocalUser): Flow<Result<Unit>> {
        return flow {
            try {
                val updatedUser = supabaseClient.from("profiles").update(user) {
                    filter {
                        eq("id", user.id)
                    }
                    select()
                }.decodeSingle<UserClass.LocalUser>()
                _user.value = updatedUser
                emit(Result.success(Unit))
            } catch (e: Exception) {
                println(e)
                emit(Result.failure(e))
            }
        }.flowOn(dispatcher)
    }

    override fun validateUserName(uName: String): Flow<Result<Boolean>> {
        return flow {
            try {
                val user = supabaseClient.from("profiles").select {
                    filter {
                        eq("username", uName)
                    }
                }.decodeSingleOrNull<User>()
                emit(Result.success(user == null))
            } catch (e: Exception) {
                print(e)
                emit(Result.failure(Throwable(e)))
            }
        }.flowOn(dispatcher)
    }
}