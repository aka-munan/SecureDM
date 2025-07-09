package devoid.secure.dm.domain.model

interface FriendsRepository {
    suspend fun searchUserByUname(uName:String,limit:Int=5):Result<List<UserClass.RemoteUser>>
    suspend fun getUserById(uId:String):Result<UserClass.RemoteUser>
    suspend fun getChatIdFromOfTarget(uid: String): Result<String?>
    suspend fun getFriends(pageNumber : Int=1,pageSize: Int=10):Result<List<UserClass.RemoteUser>>
    suspend fun getFriendShipStatus(targetUid: String):Result<FriendRequestStatus?>
    suspend fun sendFriendRequest(targetUid: String): Result<FriendRequestStatus>
    suspend fun getFriendRequestsCount():Result<Long>
    suspend fun getFriendRequests(pageNo:Int=1,pageSize:Int=10):Result<List<FriendRequest>>
    suspend fun setFriendRequestStatus(chatId:String,targetUid: String, status: FriendRequestStatus):Result<Unit>
    suspend fun cancelRequest(targetUid: String): Result<Boolean>
}