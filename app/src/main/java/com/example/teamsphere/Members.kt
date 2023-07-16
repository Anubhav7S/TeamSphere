package com.example.teamsphere

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.icu.util.Output
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teamsphere.databinding.ActivityMembersBinding
import com.example.teamsphere.databinding.DialogSearchMemberBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Members : BaseActivity(){
    private var binding:ActivityMembersBinding?=null
    private lateinit var mBoardDetails:Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    private var anyChangesMade:Boolean=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails= IntentCompat.getParcelableExtra<Board>(intent, Constants.BOARD_DETAIL,Board::class.java)!!
        }
        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
//        if (anyChangesMade){
//            showProgressDialog(resources.getString(R.string.please_wait))
//            //setResult(Activity.RESULT_OK)
//            startActivity(Intent(this,TaskList::class.java))
//            hideProgressDialog()
//            //super.getOnBackPressedDispatcher().onBackPressed()
//        }



    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMembers)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            // supportActionBar?.title= title
            supportActionBar?.title= resources.getString(R.string.members)
            binding?.toolbarMembers?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_add_members,menu)
//        return super.onCreateOptionsMenu(menu)
//    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_add_members, menu)
        return super.onCreateOptionsMenu(menu)
}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member->{
                dialogMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun setUpMembersList(list:ArrayList<User>){
        mAssignedMembersList=list

        hideProgressDialog()
        binding?.rvMembersList?.layoutManager=LinearLayoutManager(this)
        binding?.rvMembersList?.setHasFixedSize(true)
        val adapter=MembersListItemsAdapter(this,list)
        binding?.rvMembersList?.adapter=adapter
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this,mBoardDetails,user)
    }

    private fun dialogMember(){
        val customDialog=Dialog(this)
        val dialogBinding=DialogSearchMemberBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.tvAddDialog.setOnClickListener {
            val email = customDialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()){
                customDialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this,email)
            }
            else{
                Toast.makeText(this,"Please enter the email address of the member",Toast.LENGTH_SHORT).show()
            }

           // customDialog.dismiss()
        }
        dialogBinding.tvCancelDialog.setOnClickListener{
            customDialog.dismiss()
        }
        customDialog.show()
    }

//    override fun onBackPressed() {
//        if (anyChangesMade){
//            setResult(Activity.RESULT_OK)
//            super.getOnBackPressedDispatcher().onBackPressed()
//        }
//
//    }

    fun memberAssignSuccess(user: User){ // if we have assigned a new member and that is successful
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesMade=true
        setUpMembersList(mAssignedMembersList)
        sendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken)
        hideProgressDialog()
    }

    private fun sendNotificationToUserAsyncTask(boardName:String, token:String){
        Thread(Runnable {
            var result:String
            var connection:HttpURLConnection?=null
            try {
                val url=URL(Constants.FCM_BASE_URL)
                connection=url.openConnection() as HttpURLConnection
                connection.doOutput=true
                connection.doInput=true
                connection.instanceFollowRedirects=false
                connection.requestMethod="POST"
                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")

                connection.setRequestProperty(Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches=false

                val wr=DataOutputStream(connection.outputStream)
                val jsonRequest=JSONObject()
                val dataObject=JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,"You have been assigned to the board by ${mAssignedMembersList[0].name}")
                jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO,token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult:Int=connection.responseCode
                if (httpResult==HttpURLConnection.HTTP_OK){
                    val inputStream=connection.inputStream
                    val reader=BufferedReader(InputStreamReader(inputStream))
                    val sb=StringBuilder()
                    var line:String
                    try {
                        while (reader.readLine().also{line=it}!=null){
                            sb.append(line+"\n")
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result=sb.toString()
                }
                else{
                    result=connection.responseMessage
                }
            }catch (e:SocketTimeoutException){
                result="Connection Timeout"
            }catch (e:Exception){
                result="Error : " + e.message
            }finally {
                connection?.disconnect()
            }
            runOnUiThread {
                showProgressDialog(resources.getString(R.string.please_wait))
            }
        }).start()

    }

//    private fun dialogMember(){
//        val dialog=Dialog(this)
//        val dialogBinding=DialogSearchMemberBinding.inflate(layoutInflater)
//        dialog.setContentView(dialogBinding.root)
//        dialog.setCanceledOnTouchOutside(false)
////        val tvAdd: TextView = findViewById(R.id.tv_add_dialog)
////        val tvCancel: TextView = findViewById(R.id.tv_cancel_dialog)
//        dialogBinding.tvAddDialog.setOnClickListener {
//
//            val email=dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
//            if (email.isNotEmpty()){
//                dialog.dismiss()
//                //TODO implement logic to add member
//            }
//            else{
//                Toast.makeText(this,"Please enter the email address of the member",Toast.LENGTH_SHORT).show()
//            }
//        }
//        dialogBinding.tvCancelDialog.setOnClickListener {
//            dialog.dismiss()
//        }
//        dialog.show()
//
//    }

}