package com.synapse.social.studioasinc.feature.shared.main

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synapse.social.studioasinc.data.repository.AuthRepository
import com.synapse.social.studioasinc.data.repository.UserRepository
import com.synapse.social.studioasinc.ui.navigation.AppDestination
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private val gson = Gson()
    }

    private val _updateState = MutableLiveData<UpdateState>()
    val updateState: LiveData<UpdateState> = _updateState

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth = _isCheckingAuth.asStateFlow()

    private val _startDestination = MutableStateFlow(AppDestination.Auth.route)
    val startDestination = _startDestination.asStateFlow()

    init {

        checkUserAuthentication()
    }

    fun checkForUpdates() {
        if (!isNetworkAvailable()) {
            _updateState.value = UpdateState.NoUpdate
            return
        }

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val url = URL("https://pastebin.com/raw/sQuaciVv")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        throw IOException("HTTP error code: ${connection.responseCode}")
                    }
                }

                val updateMap: HashMap<String, Any> = gson.fromJson(
                    response,
                    object : TypeToken<HashMap<String, Any>>() {}.type
                )

                val currentVersionCode = try {
                    application.packageManager.getPackageInfo(application.packageName, 0).versionCode
                } catch (e: PackageManager.NameNotFoundException) {
                    _updateState.value = UpdateState.Error("Version check failed: ${e.message}")
                    return@launch
                }

                val latestVersionCode = (updateMap["versionCode"] as? Double)?.toInt() ?: 0

                if (latestVersionCode > currentVersionCode) {
                    val title = updateMap["title"] as? String ?: ""
                    val versionName = updateMap["versionName"] as? String ?: ""
                    val changelog = updateMap["whatsNew"] as? String ?: ""
                    val updateLink = updateMap["updateLink"] as? String ?: ""
                    val isCancelable = updateMap["isCancelable"] as? Boolean ?: false

                    _updateState.value = UpdateState.UpdateAvailable(title, versionName, changelog, updateLink, isCancelable)
                } else {
                    _updateState.value = UpdateState.NoUpdate
                }
            } catch (e: IOException) {
                _updateState.value = UpdateState.Error("Network error: ${e.message}")
            } catch (e: JsonSyntaxException) {
                _updateState.value = UpdateState.Error("Update parsing error: ${e.message}")
            }
        }
    }

    fun checkUserAuthentication() {
        viewModelScope.launch {
            try {

                var isAuthenticated = authRepository.restoreSession()


                if (!isAuthenticated) {

                    for (attempt in 1..2) {
                        delay((500 * attempt).toLong())
                        isAuthenticated = authRepository.restoreSession()
                        if (isAuthenticated) {
                            break
                        }
                    }
                }

                if (isAuthenticated) {
                    val userId = authRepository.getCurrentUserId()
                    val userEmail = authRepository.getCurrentUserEmail()

                    if (userId != null && !userEmail.isNullOrBlank()) {
                         userRepository.getUserById(userId)
                            .onSuccess { user ->
                                if (user != null) {
                                    if (!user.banned) {
                                        _authState.value = AuthState.Authenticated
                                        _startDestination.value = AppDestination.Home.route
                                    } else {
                                        _authState.value = AuthState.Banned


                                        _startDestination.value = AppDestination.Auth.route
                                    }
                                } else {

                                    _authState.value = AuthState.Authenticated
                                    _startDestination.value = AppDestination.Home.route
                                }
                            }
                            .onFailure {



                                _authState.value = AuthState.Authenticated
                                _startDestination.value = AppDestination.Home.route
                            }
                    } else {

                         _authState.value = AuthState.Unauthenticated
                         _startDestination.value = AppDestination.Auth.route
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                    _startDestination.value = AppDestination.Auth.route
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Database error: ${e.message}")
                _startDestination.value = AppDestination.Auth.route
            } finally {
                _isCheckingAuth.value = false
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

sealed class UpdateState {
    data class UpdateAvailable(val title: String, val versionName: String, val changelog: String, val updateLink: String, val isCancelable: Boolean) : UpdateState()
    object NoUpdate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Banned : AuthState()
    data class Error(val message: String) : AuthState()
}
