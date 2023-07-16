package com.example.teamsphere

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.teamsphere.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private var binding:ActivitySplashScreenBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)
       // window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val typeface:Typeface=Typeface.createFromAsset(assets,"berkshire-swash/BerkshireSwash-Regular.ttf")
        binding?.tvSplashScreen?.typeface=typeface
//        Handler().postDelayed({startActivity(Intent(this,Introduction::class.java))
//                              finish()} ,2500)
        Handler(Looper.getMainLooper()).postDelayed({
            var currentUserID=FirestoreClass().getCurrentUserID()
            if (currentUserID.isNotEmpty()){
                startActivity(Intent(this,MainActivity::class.java))
            }
            else{
                startActivity(Intent(this,Introduction::class.java))
            }

           // startActivity(Intent(this,Introduction::class.java))
                              finish()}, 1000)
    }

}