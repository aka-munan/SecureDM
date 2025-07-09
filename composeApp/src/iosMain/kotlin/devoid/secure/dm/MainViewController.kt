package devoid.secure.dm

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import devoid.secure.dm.ui.navigation.RootRoute
import devoid.secure.dm.ui.compose.App
import devoid.secure.dm.ui.state.MainAppState
import devoid.secure.dm.ui.viewmodel.MainViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import platform.Foundation.NSDateFormatter

fun MainViewController() = ComposeUIViewController {
    val viewModel = koinViewModel<MainViewModel>()
    val appState by viewModel.appState.collectAsState()
    App()
}