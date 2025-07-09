package devoid.secure.dm.ui.state

import androidx.compose.runtime.Stable
import io.github.jan.supabase.auth.status.SessionStatus

@Stable
sealed interface MainAppState{
    data object Loading:MainAppState
    data class Success(val sessionStatus: SessionStatus):MainAppState
}