package com.example.teamsphere

import android.app.Dialog
import android.content.ContentQueryMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.teamsphere.databinding.ActivityBaseBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
//import kotlinx.android.synthetic.main.dialog_progress.*


open class BaseActivity : AppCompatActivity() {
    private var binding:ActivityBaseBinding?=null
    private var doubleBackToExitPressedOnce=false
    private lateinit var mProgressDialog:Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }

    fun showProgressDialog(text:String){
        mProgressDialog= Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        //mProgressDialog.tv_progress_text.text=text
        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text=text
        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserID():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doublePressBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.getOnBackPressedDispatcher().onBackPressed()
            return;
        }
        this.doubleBackToExitPressedOnce=true
        Toast.makeText(this,resources.getString(R.string.please_click_again_to_exit),Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce=false },2000)
    }

    fun showErrorSnackBar(message:String){
        val snackBar=Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackBarView=snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_error_color))
        snackBar.show()
    }
}