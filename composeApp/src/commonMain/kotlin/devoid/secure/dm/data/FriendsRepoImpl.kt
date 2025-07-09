package devoid.secure.dm.data

import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.FriendRequest
import devoid.secure.dm.domain.model.FriendRequestStatus
import devoid.secure.dm.domain.model.FriendsRepository
import devoid.secure.dm.domain.model.UserClass
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FriendsRepoImpl(private val supabaseClient: SupabaseClient) : FriendsRepository {


    override suspend fun searchUserByUname(uName: String, limit: Int): Result<List<UserClass.RemoteUser>> {
        return try {
            val result = supabaseClient.from("profiles")
                .select(columns = Columns.list("id", "username", "full_name", "avatar_url")) {
                    filter {
                        ilike("username", "%$uName%")
                    }
                    order("username", Order.ASCENDING)
                    limit(limit.toLong())
                }.decodeList<UserClass.RemoteUser>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(uId: String): Result<UserClass.RemoteUser> {
        return try {
            val remoteUser = supabaseClient.from("profiles")
                .select(columns = Columns.list("id", "username", "full_name", "avatar_url", "bio")) {
                    filter {
                        eq("id", uId)
                    }
                    order("username", Order.ASCENDING)
                }.decodeSingle<UserClass.RemoteUser>()
            Result.success(remoteUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatIdFromOfTarget(uid: String): Result<String?> {
         return try {
            val result = supabaseClient.postgrest.rpc("get_chat_id_of_target",buildJsonObject {
                put("p_target_id",uid)
            })
             val chatId = result.decodeSingleOrNull<Map<String, String>>()?.get("chat_id")
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriendShipStatus(targetUid: String): Result<FriendRequestStatus?> {
        Logger.i("getting friendship status")
        return try {
            val result = supabaseClient.postgrest.rpc("get_request_status", parameters = buildJsonObject {
                put("p_target_id", targetUid)
            }).decodeSingleOrNull<Map<String, String>>()
            Logger.i("friend req status: ${result}")
            Result.success(result?.get("request_status")?.let { FriendRequestStatus.valueOf(it)})
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendFriendRequest(targetUid: String): Result<FriendRequestStatus> {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return try {
            currentUser?.let {
                supabaseClient.postgrest.rpc("send_request_individual", parameters = buildJsonObject {
                    put("p_target_id", targetUid)
                })
                Result.success(FriendRequestStatus.PENDING)
            } ?: Result.failure(IllegalStateException("current user not found"))
        } catch (e: Exception) {
            println(e)
            Result.failure(e)
        }
    }

    override suspend fun getFriendRequestsCount(): Result<Long> {
        val currentUser = supabaseClient.auth.currentUserOrNull()

        return try {
            currentUser?.let { user ->
                val count = supabaseClient.from("friends").select {
                    filter {
                        eq("friend_id", user.id)
                        eq("status", FriendRequestStatus.PENDING.name)
                    }
                    count(Count.ESTIMATED)
                }.countOrNull()
                Result.success(count ?: 0L)
            } ?: Result.failure(IllegalStateException("Current user Not Found"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun getFriendRequests(pageNo: Int, pageSize: Int): Result<List<FriendRequest>> {
        return try {
            val postgrestResult =
                supabaseClient.postgrest.rpc("get_pending_requests", parameters = buildJsonObject {
                    put("p_page_size", pageSize)
                    put("p_page_no", pageNo)
                })
            val requests = postgrestResult.decodeList<FriendRequest>()
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setFriendRequestStatus(
        chatId: String,
        targetUid: String,
        status: FriendRequestStatus
    ): Result<Unit> {
        if (status == FriendRequestStatus.PENDING) {
            return Result.failure(IllegalArgumentException("Cant set $status as Status"))
        }
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return try {
            currentUser?.let {
                supabaseClient.postgrest.rpc("set_request_status", parameters = buildJsonObject {
                    put("p_chat_id", chatId)
                    put("p_target_id", targetUid)
                    put("p_status", status.name)
                })
                Result.success(Unit)
            } ?: Result.failure(IllegalStateException("Current user Not Found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriends(pageNumber: Int, pageSize: Int): Result<List<UserClass.RemoteUser>> {
        return try {
            val postgrestResult =
                supabaseClient.postgrest.rpc("get_friends", parameters = buildJsonObject {
                    put("p_page_no", pageNumber)
                    put("p_page_size", pageSize)
                })
            val friends = postgrestResult.decodeList<UserClass.RemoteUser>()
            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRequest(targetUid: String): Result<Boolean> {
        return Result.failure(Exception())
    }
}