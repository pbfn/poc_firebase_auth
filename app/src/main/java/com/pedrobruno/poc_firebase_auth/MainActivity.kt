package com.pedrobruno.poc_firebase_auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pedrobruno.poc_firebase_auth.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var googleSingInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("730879797264-q8dftpobnsr8p1a55gjmcbqquuf9cblr.apps.googleusercontent.com")
                .requestEmail()
                .build()

        googleSingInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        binding.googleSignInBtnn.setOnClickListener {
            Log.d(TAG, "onCreate: begin Google SignIn")
            val intent = googleSingInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: GoogleSignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                Log.d(TAG, "onActivityResult:${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount:begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener { authResult ->
            Log.d(TAG, "firebaseAuthWithGoogleAccount:LoggedIn")
            val firebaseUser = firebaseAuth.currentUser
            val uid = firebaseUser!!.uid
            val email = firebaseUser.email
            Log.d(TAG, "firebaseAuthWithGoogleAccount:Uid $uid")
            Log.d(TAG, "firebaseAuthWithGoogleAccount:Email $email")

            if (authResult.additionalUserInfo!!.isNewUser) {
                Log.d(TAG, "firebaseAuthWithGoogleAccount:Account created.. \n$email")
                Toast.makeText(this, "Account created.. \n$email", Toast.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "firebaseAuthWithGoogleAccount:Existing user.. \n$email")
                Toast.makeText(this, "LoggedIn.. \n$email", Toast.LENGTH_LONG).show()
            }

            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            finish()
        }
            .addOnFailureListener {
                Log.d(TAG, "firebaseAuthWithGoogleAccount:Loggin failed due to ${it.message}")
                Toast.makeText(this, "Loggin failed due to ${it.message}", Toast.LENGTH_LONG).show()
            }


    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            finish()
        }
    }


}