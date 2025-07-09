package devoid.secure.dm.di

import com.example.app.BuildKonfig
import devoid.secure.dm.data.AttachmentRepoImpl
import devoid.secure.dm.data.ChatRepoImpl
import devoid.secure.dm.data.FriendsRepoImpl
import devoid.secure.dm.data.auth.AuthRepoImpl
import devoid.secure.dm.data.auth.UserRepoImpl
import devoid.secure.dm.domain.BackgroundTaskManager
import devoid.secure.dm.domain.model.*
import devoid.secure.dm.ui.viewmodel.*
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val JsNonJsModule: Module
expect val platformModule:Module
expect fun getIOCommonDispatcher(): CoroutineDispatcher
val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL ,
            supabaseKey = BuildKonfig.SUPABASE_KEY
        ) {
            install(io.github.jan.supabase.auth.Auth)
            install(io.github.jan.supabase.postgrest.Postgrest)
            install(Realtime)
            install(io.github.jan.supabase.compose.auth.ComposeAuth) {
                googleNativeLogin(BuildKonfig.WEB_CLIENT_ID) //Use the Web Client ID, not the Android one!
            }
            install(Storage)
        }
    }
    single { AuthRepoImpl(getIOCommonDispatcher(), supabaseClient = get()) }.bind<AuthRepository>()
    single { UserRepoImpl(getIOCommonDispatcher(), supabaseClient = get()) }.bind<UserRepository>()
    single { ChatRepoImpl(supabaseClient = get(), scope = CoroutineScope(getIOCommonDispatcher())) }.bind<ChatRepository>()
    single { FriendsRepoImpl(get()) }.bind<FriendsRepository>()
    single { AttachmentRepoImpl(get()) }.bind<AttachmentsRepository>()
    single {  }
    single{BackgroundTaskManager()}
    viewModelOf(::MainViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::FriendRequestViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AttachmentsViewModel)
//    viewModelOf(::SettingsViewModel)// settings viewmodel provided by android and desktop srcs
}