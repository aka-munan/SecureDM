package devoid.secure.dm.domain.audio

expect class AudioPlayer(){
    //make sure to initialize audioPLayer before doing anything
    val duration:Int
    val progress:Int
    var autoPlay : Boolean
     fun setCallback(callback: AudioPLayerCallback?)
    suspend fun init(uri: String)
    fun isPlaying():Boolean
    fun play()
    fun pause()
    fun seekTo(milli:Int)
    fun release()
}

interface AudioPLayerCallback{
    fun onLoading()
    fun onReady()
    fun onPlay()
    fun onPause()
    fun onStop(){}
    fun onProgress(fraction:Float)
}