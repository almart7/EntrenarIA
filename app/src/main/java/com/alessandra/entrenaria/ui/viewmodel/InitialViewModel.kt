import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InitialViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    // Lógica de login con Google y creación o verificación de usuario en Firestore
    fun loginWithGoogle(idToken: String) {
        _loginState.value = LoginState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        viewModelScope.launch {
                            checkOrCreateUser(it.uid, it.email ?: "")
                        }
                    }
                } else {
                    _loginState.value = LoginState.Error("Error en inicio de sesión con Google")
                }
            }
    }

    private suspend fun checkOrCreateUser(uid: String, email: String) {
        val userDocRef = db.collection("users").document(uid)
        val doc = try {
            userDocRef.get().await()
        } catch (e: Exception) {
            _loginState.value = LoginState.Error("Error al obtener datos de usuario")
            return
        }
        if (doc.exists()) {
            _loginState.value = LoginState.Success
        } else {
            val newUser = hashMapOf(
                "uid" to uid,
                "email" to email,
                "name" to "",
                "age" to 0,
                "gender" to ""
            )
            try {
                userDocRef.set(newUser, SetOptions.merge()).await()
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error creando usuario")
                auth.signOut()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _loginState.value = LoginState.Idle
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
