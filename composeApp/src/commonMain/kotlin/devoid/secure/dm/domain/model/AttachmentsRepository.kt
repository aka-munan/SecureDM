package devoid.secure.dm.domain.model

import io.github.jan.supabase.postgrest.query.Order

interface AttachmentsRepository {
    suspend fun uploadFile(name:String,chatId:String,attachmentType: AttachmentType,data: ByteArray):Result<Unit>
    suspend fun getUrl(name:String,chatId: String,attachmentType: AttachmentType):Result<String>
    suspend fun getUrl(path:String):Result<String>
    suspend fun delete(name: String,chatId: String,attachmentType: AttachmentType):Result<Unit>
    suspend fun fetch()
    suspend fun getAttachmentsSharedInChat(chatId: String, vararg attachmentType: AttachmentType=arrayOf(), pageNo:Int=1, pageSize:Int=10, order: Order= Order.DESCENDING): Result<List<MessageAttachment>>
}