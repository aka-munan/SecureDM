package devoid.secure.dm.data

import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.AttachmentType
import devoid.secure.dm.domain.model.AttachmentsRepository
import devoid.secure.dm.domain.model.MessageAttachment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class AttachmentRepoImpl(private val supabase: SupabaseClient) : AttachmentsRepository {

    override suspend fun uploadFile(
        name: String,
        chatId: String,
        attachmentType: AttachmentType,
        data: ByteArray
    ): Result<Unit> {
        return try {
            val bucket = supabase.storage.from(bucketId = "file-attachments")
            val path = getPath(name, chatId, attachmentType)
//            val resumableUpload =
//                bucket.resumable.createOrContinueUpload(path = path, source = "upload file", data = data) {
//                    upsert = false
//                }
            bucket.upload(path, data) {
                this.upsert = false
            }
//            resumableUpload.startOrResumeUploading()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUrl(name: String, chatId: String, attachmentType: AttachmentType): Result<String> {
        val path = getPath(name, chatId, attachmentType)
        return getUrl(path)
    }

    override suspend fun getUrl(path: String): Result<String> {
        return try {
            val bucket = supabase.storage.from("file-attachments")
            val url = bucket.publicUrl(path)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun delete(name: String, chatId: String, attachmentType: AttachmentType): Result<Unit> {
        return try {
            val path = getPath(name, chatId, attachmentType)
            val bucket = supabase.storage.from(bucketId = "file-attachments")
            bucket.delete(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetch() {
        val bucket = supabase.storage.from(bucketId = "file-attachments")
//        bucket.exists()
    }

    override suspend fun getAttachmentsSharedInChat(
        chatId: String,
        vararg attachmentType: AttachmentType,
        pageNo: Int,
        pageSize: Int,
        order: Order
    ): Result<List<MessageAttachment>> {
        return try {
            val req = supabase.from("message_attachments").select() {
                filter {
                    and {
                        eq("chat_id", chatId)
                        or {
                            attachmentType.forEach {
                                eq("type",it.name)
                            }
                        }
                    }
                }
                range((1).toLong(), (pageNo * pageSize + pageSize).toLong())
                limit(pageSize.toLong())
            }
            Logger.i("attacghments loaded: ${req.decodeList<MessageAttachment>()}")
            Result.success(req.decodeList<MessageAttachment>())
        } catch (e: Exception) {
            Logger.e(e){
                "failed to load attachments"
            }
            Result.failure(e)
        }
    }

    private fun getPath(name: String, chatId: String, attachmentType: AttachmentType): String {
        return when (attachmentType) {
            AttachmentType.AUDIO -> "audio"
            AttachmentType.VIDEO -> "video"
            AttachmentType.IMAGE -> "image"
            AttachmentType.DOCUMENT -> "document"
        } + "/$chatId/$name"
    }
}