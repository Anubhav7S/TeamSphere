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
import java.sql.SQLTransactionRollbackException

object Constants{
    const val USERS:String="users"

    const val BOARDS:String="boards"

    const val IMAGE:String="image"
    const val NAME:String="name"
    const val MOBILE:String="mobile"
    const val ASSIGNED_TO:String="assignedTo"
    const val DOCUMENT_ID:String="documentID"
    const val TASK_LIST:String="taskList"
    const val BOARD_DETAIL:String="board_detail"
    const val ID:String="id"
    const val EMAIL:String="email"
    const val BOARD_MEMBERS_LIST:String="board_members_list"
    const val SELECT:String="Select"
    const val UN_SELECT:String="UnSelect"
    const val TRANSFER:String="transfer"
    const val TEAMSPHERE_PREFERENCES="TeamSphere_preferences"


    const val FCM_TOKEN_UPDATED="fcmTokenUpdated"
    const val FCM_TOKEN="fcmToken"


    const val FCM_BASE_URL:String="https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String="authorization"
    const val FCM_KEY:String="key"
    const val FCM_SERVER_KEY:String="AAAAPvjIkkk:APA91bGQxNKDDFIYIQEiTMjBxChB9luN732nf9oFDqsIBdFq9Zq0YeXQ5XWfbxWATNdie7X2ouIR9wDJXYvYlEQhDH01epzIVWJ9H9BX91Poo32ncF4OhohufKlcDBvHQ3PqzrokRwuR"
    const val FCM_KEY_TITLE:String="title"
    const val FCM_KEY_MESSAGE:String="message"
    const val FCM_KEY_DATA:String="data"
    const val FCM_KEY_TO:String="to"


    const val TASK_LIST_ITEM_POSITION:String="task_list_item_position"
    const val CARD_LIST_ITEM_POSITION:String="card_list_item_position"

    const val READ_STORAGE_PERMISSION_CODE=1
    //private const val PICK_IMAGE_REQUEST_CODE=2

    fun getFileExtension(activity: Activity, uri: Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }






//    fun showImagePicker(){
//        val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        // startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
//        startForResult.launch(galleryIntent)
//    }

}