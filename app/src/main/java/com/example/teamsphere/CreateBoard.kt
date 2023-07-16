package com.example.teamsphere

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.teamsphere.databinding.ActivityCreateBoardBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoard : BaseActivity() {
    private var binding:ActivityCreateBoardBinding?=null
    private var mSelectedImageFileURI: Uri?=null
    private lateinit var mUserName:String
    private var mBoardImageURL:String=""
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data!=null) {
            val intent = result.data
            if (intent != null) {
                mSelectedImageFileURI=intent.data
                try {
                    binding?.ivBoardImage?.let {
                        Glide.with(this@CreateBoard).load(mSelectedImageFileURI).centerCrop().placeholder(R.drawable.ic_board_place_holder).into(
                            it)
                    }
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        if (intent.hasExtra(Constants.NAME)){
            mUserName= intent.getStringExtra(Constants.NAME).toString()
        }
        binding?.ivBoardImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                showImagePicker()
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding?.btnCreateCreateBoard?.setOnClickListener {
            if (mSelectedImageFileURI!=null){
                uploadBoardImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }


    private fun showImagePicker(){
        val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
        startForResult.launch(galleryIntent)
    }



    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCreateBoard)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            supportActionBar?.title=resources.getString(R.string.create_board_title)
            binding?.toolbarCreateBoard?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun createBoard(){
        val assignedUsers : ArrayList<String> = ArrayList()
        assignedUsers.add(getCurrentUserID())

        var board=Board(binding?.etBoardName?.text.toString(),mBoardImageURL,mUserName,assignedUsers)
        FirestoreClass().createBoard(this,board)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
       // showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileURI!=null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child("BOARD_IMAGE" +
                    System.currentTimeMillis() + "." + Constants.getFileExtension(this,mSelectedImageFileURI))
            sRef.putFile(mSelectedImageFileURI!!).addOnSuccessListener { // if putting file into storage is successful, we get a snapshot of it
                    taskSnapshot->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                    mBoardImageURL=uri.toString()
                   // hideProgressDialog()
                    createBoard()
                }
            }.addOnFailureListener{
                    exception->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }


}