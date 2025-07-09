package devoid.secure.dm.ui.state

import androidx.compose.runtime.Stable

@Stable
sealed class AudioPlayerState(open val id: String?){
    data class Loading(override val id: String?):AudioPlayerState(id)
    data class Ready(
        override val id:String?=null,
        val isPlaying: Boolean,
        val duration:Int,
        val playerProgress:Float,
    ):AudioPlayerState(id)
}

