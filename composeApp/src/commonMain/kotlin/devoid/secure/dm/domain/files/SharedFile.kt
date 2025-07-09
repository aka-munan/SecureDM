package devoid.secure.dm.domain.files

interface SharedFile {
    val uri :String
    fun toByteArray():ByteArray?
    fun getFileSize():Long
    fun getFileName():String?
}

fun getExtentionFromName(name:String):String{
    val index = name.indexOfLast { it == '.' }
    if (index== -1)
        return ""
    return name.takeLast(name.length - index)
}

