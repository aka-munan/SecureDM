package devoid.secure.dm.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import devoid.secure.dm.ui.navigation.AuthRoute
import devoid.secure.dm.ui.theme.backgroundBrush
import devoid.secure.dm.ui.theme.blurColor
import devoid.secure.dm.domain.toSimplifiedError
import devoid.secure.dm.ui.viewmodel.AuthViewModel
import devoid.secure.dm.ui.viewmodel.EditProfileViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.GoogleDialogType
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.error
import secure_dm.composeapp.generated.resources.google_logo
import secure_dm.composeapp.generated.resources.person_edit
import secure_dm.composeapp.generated.resources.verified
import secure_dm.composeapp.generated.resources.visibility
import secure_dm.composeapp.generated.resources.visibility_off

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginLayout(
    modifier: Modifier = Modifier,
    isLogin: Boolean,
    onToggleSignup: () -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onLoginWithGoogle: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.wrapContentSize().padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isLogin) "Welcome Back" else "Signup",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(), label = { Text("Email") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onLogin(email, password)
            }),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    AsyncImage(
                        modifier = Modifier.padding(8.dp),
                        model = if (passwordVisible)Res.drawable.visibility else Res.drawable.visibility_off,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        )
        Text(
            "Forgot Password?",
            modifier = Modifier.align(Alignment.End).padding(8.dp)
                .clickable(onClick = onForgotPassword),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            onLogin(email, password)
        }) {
            Text(if (isLogin) "Log in" else "Signup")
        }
        Row(modifier = Modifier.align(Alignment.Start), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isLogin) "Don't have an account yet!" else "Already have an account!",
                color = MaterialTheme.colorScheme.secondary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            TextButton(onClick = onToggleSignup) {
                Text(if (isLogin) "Signup" else "Log in")
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            HorizontalDivider(Modifier.width(50.dp))
            Text(
                "OR",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            HorizontalDivider(Modifier.width(50.dp))
        }
        ElevatedButton(onClick = onLoginWithGoogle) {
            AsyncImage(
                modifier = Modifier.size(30.dp).padding(4.dp),
                model = Res.drawable.google_logo,
                contentDescription = "Google Login"
            )
            Text("Log in with Google")
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SetupProfileLayout(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    enableBackNavigation: Boolean = true,
    onSuccess: () -> Unit
) {
    val viewModel: EditProfileViewModel = koinViewModel()
    val user by viewModel.user.collectAsState()
    val name by viewModel.fullName.collectAsState()
    val uName by viewModel.userName.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val profileUrl by derivedStateOf { user?.avatarUrl ?: Res.drawable.person_edit }
    val isUsernameAvailable by viewModel.isUserNameAvailable.collectAsState()
    val userNameError by viewModel.userNameError.collectAsState()
    var showLoading by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier.wrapContentSize().padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecureDmSnackBar(snackBarHostState)
        val onDoneClicked:()->Unit={
            showLoading = true
            viewModel.viewModelScope.launch {
                val result = viewModel.updateProfile().single()
                showLoading = false
                result.onSuccess {
                    onSuccess()
                }.onFailure {
                    scope.launch {
                        snackBarHostState.showSnackbar(message = it.toSimplifiedError().toString())
                    }
                }
            }
        }
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = "Setup LocalProfile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )


        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { },
                model = profileUrl,
                colorFilter =
                    if (user?.avatarUrl == null)
                        ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                    else null,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name, onValueChange = { viewModel.updateFullName(it) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                label = { Text("Name") })
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uName, onValueChange = { viewModel.updateUserName(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            singleLine = true,
            isError = userNameError != null,
            label = { Text("Username") },
            supportingText = {
                if (userNameError != null) {
                    Text(text = userNameError.toString(), color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                if (uName == (user?.uName ?: "")) {
                    return@OutlinedTextField
                }
                val icon = if (userNameError==null) {
                    Res.drawable.verified
                } else {
                    Res.drawable.error
                }
                AsyncImage(
                    modifier = Modifier.size(24.dp),
                    model = icon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = if (userNameError==null) Color.Green else MaterialTheme.colorScheme.error)
                )
            })

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = bio, onValueChange = { viewModel.updateBio(it)},
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.clearFocus()
                onDoneClicked()
            }),
            label = { Text("Bio") })

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !showLoading,
            onClick = onDoneClicked) {
            if (showLoading){
                CircularProgressIndicator()
            }else{
                Text("Done")
            }
        }
        if (enableBackNavigation) {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onNavigateBack) {
                Text("Login Other Account!")
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(onNavigate: (AuthRoute) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<AuthViewModel>()
    AuthScreenBase(snackbarHostState = snackbarHostState) {
        Column(
            modifier = Modifier.wrapContentSize().padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var email by remember { mutableStateOf("") }
            val focusManager = LocalFocusManager.current
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "Forgot Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(), label = { Text("Email") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                })
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.sendPasswordReset(email, onResult = { result ->
                        result.onSuccess {
                            scope.launch {
                                snackbarHostState.showSnackbar(message = "Please check your email App to reset your password.")
                                onNavigate(AuthRoute.Login())
                            }
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(message = it.message ?: "Unknown error")
                            }
                        }
                    })
                }) {
                Text("Reset Password")
            }
            TextButton(
                modifier = Modifier.align(Alignment.Start),
                onClick = { onNavigate(AuthRoute.Login()) }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Back to Login")
            }
        }
    }
}

@Composable
fun SetupProfileScreen(
    modifier: Modifier = Modifier,
    onNavigate: (AuthRoute) -> Unit,
    onAuthSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    AuthScreenBase(modifier = modifier, snackbarHostState = snackbarHostState) {
        SetupProfileLayout(
            onSuccess = {
                onAuthSuccess()
            }, onNavigateBack = { onNavigate(AuthRoute.Login()) }
        )
    }
}

@Composable
fun AuthScreenBase(
    modifier: Modifier = Modifier.fillMaxSize(),
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.backgroundBrush()),
        contentAlignment = Alignment.Center
    ) {
        SecureDmSnackBar(snackbarHostState,Modifier.align(Alignment.TopCenter))
        Box(
            Modifier.widthIn(max = 600.dp)
                .fillMaxWidth(fraction = 0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.blurColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            content()
        }
    }
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    isLogin: Boolean,
    onAuthSuccess: () -> Unit,
    onNavigate: (AuthRoute) -> Unit,
) {
    val viewModel: AuthViewModel = koinViewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val supabaseClient = koinInject<SupabaseClient>()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val validateLogin: (Result<Unit>) -> Unit = { result ->
        result.onFailure { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error.toSimplifiedError().message)
            }
        }.onSuccess {
            currentUser?.let { user ->
                if (user.uName != null) {
                    onAuthSuccess()
                } else {
                    onNavigate(AuthRoute.SetupProfile)
                }
            }
        }
    }

    val authState = supabaseClient.composeAuth.rememberSignInWithGoogle(
        type = GoogleDialogType.BOTTOM_SHEET,
        onResult = {
//            supabaseClient.auth.exchangeCodeForSession()
            when (it) { //handle errors
                NativeSignInResult.ClosedByUser -> {}
                is NativeSignInResult.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(it.message)
                    }
                }

                is NativeSignInResult.NetworkError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(it.message)
                    }
                }

                NativeSignInResult.Success -> {
                    validateLogin(Result.success(Unit))
                }
            }
        }
    )
    AuthScreenBase(modifier = modifier, snackbarHostState = snackbarHostState) {
        LoginLayout(
            isLogin = isLogin,
            onToggleSignup = {
                onNavigate(if (isLogin) AuthRoute.SignUp() else AuthRoute.Login())
            },
            onForgotPassword = { onNavigate(AuthRoute.ForgotPassword) },
            onLogin = { email, password ->
                if (isLogin) {
                    viewModel.signIn(email, password) {
                        validateLogin(it)
                    }
                } else {
                    viewModel.signUp(email, password) {
                        validateLogin(it)
                    }
                }
            }, onLoginWithGoogle = {
                authState.startFlow()
            })
    }
}