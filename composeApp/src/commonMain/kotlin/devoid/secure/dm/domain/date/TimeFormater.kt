package devoid.secure.dm.domain.date

object TimeFormater {
    fun formatToString(millis:Long):String{
        val seconds = millis / 1000
        return buildString {
            if (seconds<60){
                append("00:$seconds")
            }else{
                append((seconds/60).toInt())
                append(":")
                append((seconds.mod(60)).toString())
            }
        }
    }
}