package com.example.teamsphere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teamsphere.databinding.ActivityIntroductionBinding

class Introduction : AppCompatActivity() {
    private var binding:ActivityIntroductionBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.btnSignUp?.setOnClickListener {
            val intent= Intent(this,SignUp::class.java)
            startActivity(intent)
        }
        binding?.btnSignIn?.setOnClickListener {
            val intent= Intent(this,SignIn::class.java)
            startActivity(intent)
        }
    }
}