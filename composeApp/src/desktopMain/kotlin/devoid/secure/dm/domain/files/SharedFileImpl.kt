package devoid.secure.dm.domain.files

import co.touchlab.kermit.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.net.URI
import java.nio.file.Files

class SharedFileImpl(override val uri: String) :SharedFile {
    private val file = File(URI.create(uri).path).apply {
        Logger.i("Shared file:${this.path}")
    }
    override fun toByteArray(): ByteArray? {//scale for large files
        if (file.exists()){
//            val stream = ByteArrayOutputStream()
//            val writer = stream.bufferedWriter()
//            FileReader(file).copyTo(writer,1024)
            return Files.readAllBytes(file.toPath())
        }else
            return null
    }

    override fun getFileSize(): Long {
        return file.length()
    }

    override fun getFileName(): String? {
        return file.name
    }
}