package com.doi.ecosaver.ui.signin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.doi.ecosaver.R
import com.doi.ecosaver.data.DataRepository
import com.doi.ecosaver.data.RemoteDataSource
import com.doi.ecosaver.data.User
import com.doi.ecosaver.databinding.ActivitySignInBinding
import com.doi.ecosaver.ui.main.MainActivity
import com.doi.ecosaver.utils.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp


@Suppress("DEPRECATION")
class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FirebaseApp.initializeApp(this)
        FirebaseApp.initializeApp(this)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val dataRepository = DataRepository(RemoteDataSource())
        val viewModelFactory = ViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this,viewModelFactory)[SignInViewModel::class.java]


        binding.apply {
            btnSignIn.setOnClickListener {
                val signInIntent = configureGoogleSignIn().signInIntent
                startActivityForResult(signInIntent,RC_SIGN_IN)
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }

    private fun successSignIn(user: User) {
        showToast("Welcome ${user.displayName}")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun configureGoogleSignIn(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("CLIENTID")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let {
                    viewModel.signInWithGoogle(it).observe(this@SignInActivity) {response ->
                        val user = User(uid = response.uid, displayName = response.displayName, profileImg = response.profileImg)
                        successSignIn(user)
                    }

                }
            } catch (e: ApiException) {
                showToast("Google Sign-In failed: ${e.statusCode}")
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

}
