package com.example.teamsphere

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.teamsphere.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignIn : BaseActivity() {
    private lateinit var auth:FirebaseAuth
    private var binding: ActivitySignInBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.btnSignInSignIn?.setOnClickListener {
            signInUser()
         }
        setupActionBar()
        auth = FirebaseAuth.getInstance()
        //auth = Firebase.auth
        binding?.toolbarSignIn?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignIn)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title="SIGN IN"
        }
    }

    private fun signInUser(){
        val email:String=binding?.etEmailSignin?.text.toString().trim(){it<=' '}
//        if(errorCode == "auth/email-already-in-use"){
//            alert("Email already in use")
//        }
        val password:String=binding?.etPasswordSignin?.text.toString().trim(){it<=' '}
        if (validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        Toast.makeText(baseContext, "Welcome, you are now signed in!.", Toast.LENGTH_SHORT,).show()
                        val user = auth.currentUser
                        FirestoreClass().loadUserData(this@SignIn)
                       // startActivity(Intent(this,MainActivity::class.java))
                       // updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                       // updateUI(null)
                    }
                }
        }
    }

    private fun validateForm(email:String, password:String):Boolean{
        return when{
//            TextUtils.isEmpty(name)->{
//                showErrorSnackBar("Please enter your name!")
//                false
//            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter your email address!")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter your password")
                return false
            }
            else->{
                true
            }

        }
    }

    fun signInSuccess(loggedInUser: User) {
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()

    }
}