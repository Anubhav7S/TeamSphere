package com.example.teamsphere

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.teamsphere.databinding.ActivityMyProfileBinding
import com.example.teamsphere.databinding.NavHeaderMainBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfile : BaseActivity() {
//    companion object{
//        private const val READ_STORAGE_PERMISSION_CODE=1
//        private const val PICK_IMAGE_REQUEST_CODE=2
//    }
    private var mSelectedImageFileURI:Uri?=null
    private var mProfileImageURL:String=""
    private lateinit var mUserDetails:User
    private var binding:ActivityMyProfileBinding?=null

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data!=null) {
            val intent = result.data
            if (intent != null) {
                mSelectedImageFileURI=intent.data
                try {
                    binding?.ivUserImageMyProfile?.let {
                        Glide.with(this@MyProfile).load(mSelectedImageFileURI).centerCrop().placeholder(R.drawable.ic_user_place_holder).into(
                            it)
                    }
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }

        }
    }
//    private val startForResultMain = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//            result: ActivityResult ->
//        Toast.makeText(this,"MAI YAHA HOON",Toast.LENGTH_SHORT).show()
//        if (result.resultCode == Activity.RESULT_OK ) {
//            Toast.makeText(this,"MAI YAHA HOON",Toast.LENGTH_SHORT).show()
//            val intent = result.data
//            if (intent != null) {
//                try {
//                    Toast.makeText(this,"Working",Toast.LENGTH_LONG).show()
//                   // FirestoreClass().loadUserData(this)
//                }catch (e:Exception){
//                    Toast.makeText(this,"Gadbad",Toast.LENGTH_SHORT).show()
//                }
//
//            }
//            else{
//                Toast.makeText(this,"Not loaded",Toast.LENGTH_SHORT).show()
//            }
//
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()

        FirestoreClass().loadUserData(this)
        binding?.ivUserImageMyProfile?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                showImagePicker()
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }
        binding?.btnUpdateMyProfile?.setOnClickListener {
            if (mSelectedImageFileURI!=null){
                uploadUserImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showImagePicker()
            }
            else{
                Toast.makeText(this,"Permission to access storage has been denied. Please grant them from settings.",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showImagePicker(){
        val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
        startForResult.launch(galleryIntent)
//        val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//                result: ActivityResult ->
//            if (result.resultCode == Activity.RESULT_OK && result.data!=null) {
//                val intent = result.data
//                if (intent != null) {
//                    mSelectedImageFileURI=intent.data
//                    try {
//                        binding?.ivUserImageMyProfile?.let {
//                            Glide.with(this@MyProfile).load(mSelectedImageFileURI).centerCrop().placeholder(R.drawable.ic_user_place_holder).into(
//                                it)
//                        }
//                    }catch (e:IOException){
//                        e.printStackTrace()
//                    }
//                }
//
//            }
//        }
//        val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//       // startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
//        startForResult.launch(galleryIntent)

    }
    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMyProfile)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            supportActionBar?.title=resources.getString(R.string.my_profile)
            binding?.toolbarMyProfile?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

     fun setUserDataInUI(user: User){
        mUserDetails=user
        //Toast.makeText(this@MyProfile ,"Reached",Toast.LENGTH_LONG).show()
        binding?.ivUserImageMyProfile?.let {
            Glide.with(this@MyProfile).load(user.image).centerCrop().placeholder(R.drawable.ic_user_place_holder).into(
                it)
        }
    //    Glide.with(this@MyProfile).load(user.image).centerCrop().placeholder(R.drawable.ic_user_place_holder).into(binding?.ivUserImageMyProfile)
        binding?.etNameMyProfile?.setText(user.name)
        binding?.etEmailMyProfile?.setText(user.email)
        if (user.mobile!=0L){
            binding?.etMobileMyProfile?.setText(user.mobile.toString())
        }
     }

    private fun updateUserProfileData(){
        val userHashMap=HashMap<String,Any>()
      //  var changesMade=false
        if (mProfileImageURL.isNotEmpty() && mProfileImageURL!=mUserDetails.image){
            userHashMap[Constants.IMAGE]=mProfileImageURL
           // changesMade=true
        }
        if (binding?.etNameMyProfile?.text.toString()!=mUserDetails.name){
            userHashMap[Constants.NAME]=binding?.etNameMyProfile?.text.toString()
            //changesMade=true
        }
        if (binding?.etMobileMyProfile?.text.toString()!=mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE]=binding?.etMobileMyProfile?.text.toString().toLong()
           // changesMade=true
        }
        //if (changesMade)
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileURI!=null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE" +
                    System.currentTimeMillis() + "." + Constants.getFileExtension(this,mSelectedImageFileURI))
            sRef.putFile(mSelectedImageFileURI!!).addOnSuccessListener { // if putting file into storage is successful, we get a snapshot of it
                taskSnapshot->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                        mProfileImageURL=uri.toString()
                        hideProgressDialog()
                        updateUserProfileData()
                }
            }.addOnFailureListener{
                exception->
                Toast.makeText(this, exception.message,Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

//    private fun getFileExtension(uri: Uri?):String?{
//        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
//    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        startActivity(Intent(this,MainActivity::class.java))
      //  startForResultMain.launch(Intent(this,MainActivity::class.java))
        finish()
    }
}