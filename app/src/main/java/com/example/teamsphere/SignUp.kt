package com.example.teamsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.teamsphere.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUp : BaseActivity() {
    private var binding:ActivitySignUpBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        binding?.toolbarSignup?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            //onBackPressed()
        }
        binding?.btnSignUpSignUp?.setOnClickListener {
            registerUser()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignup)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
           // supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black)
           // actionBar.setHomeAsUpIndicator(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        }
    }

    private fun validateForm(name:String, email:String, password:String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter your name!")
                false
            }
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

    private fun registerUser(){
        val name:String=binding?.etNameSignup?.text.toString().trim(){it<=' '} //trim entry space that the user has entered
        val email:String=binding?.etEmailSignup?.text.toString().trim(){it<=' '}
        val password:String=binding?.etPasswordSignup?.text.toString().trim(){it<=' '} //empty space in password won't work in this
        if (validateForm(name, email, password)){
            //Toast.makeText(this,"User is registered",Toast.LENGTH_SHORT).show()
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
               // hideProgressDialog()
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user=User(firebaseUser.uid, name, registeredEmail)
                    FirestoreClass().registerUser(this,user) //to register user in database, previously he was added to authentication portal on app console
//                    Toast.makeText(
//                        this,
//                        "$name you have successfully registered the email address $registeredEmail",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    FirebaseAuth.getInstance().signOut()
//                    finish()
                } else {
                    Toast.makeText(this, "Sorry, registration has failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "you have been successfully registered in database",Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

}