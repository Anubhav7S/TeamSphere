package com.example.teamsphere

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamsphere.databinding.ActivityMainBinding
import com.example.teamsphere.databinding.NavHeaderMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.w3c.dom.Text
import java.io.IOException

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object{
        const val MY_PROFILE_REQUEST_CODE:Int=11
    }
    private lateinit var binding:ActivityMainBinding
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        binding.navView.setNavigationItemSelectedListener(this)
        mSharedPreferences=this.getSharedPreferences(Constants.TEAMSPHERE_PREFERENCES,Context.MODE_PRIVATE) //shared
    // preferences should be available only inside our application

        val tokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false) // to get the value whether the token is updated in the database or not
        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this,true)
        }
        else{
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) {
                updateFCMToken(it)
            }
        }
     //   FirestoreClass().loadUserData(this,true)
        binding.mainAppBarLayout.fabAddBoardMain.setOnClickListener {
            val intent=Intent(this, CreateBoard::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivity(intent)
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(binding.mainAppBarLayout.toolbarMainActivity)
        binding.mainAppBarLayout.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        binding.mainAppBarLayout.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
//        binding.mainAppBarLayout.fabAddBoardMain.setOnClickListener {
//            val intent=Intent(this, CreateBoard::class.java)
//            startActivity(intent)
//        }

//        if (supportActionBar!=null){
//            supportActionBar?.setDisplayHomeAsUpEnabled(true)
//            supportActionBar?.title="SIGN IN"
//        }
    }

    private fun toggleDrawer(){
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    @Deprecated("onBackPressed")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            doublePressBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile->{
                //val pIntent=Intent(this,MyProfile::class.java)
                startActivity(Intent(this,MyProfile::class.java))
                //Toast.makeText(this,"pIntent wala step",Toast.LENGTH_SHORT).show()
               // startForResult.launch(pIntent)
            }//Toast.makeText(this,"My profile",Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this,MyProfile::class.java))

            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply() //reset shared preferences and set that back to empty
                val intent=Intent(this,Introduction::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) //all other running activities will be closed and this content will be delivered to them
                startActivity(intent)
                finish()
            }

        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList:Boolean) {
        hideProgressDialog()
        mUserName=user.name
       // Toast.makeText(this,"Reached drawer",Toast.LENGTH_LONG).show()
//        val headerView = binding.navView.getHeaderView(0)
//        val headerBinding = headerView.findViewById<ImageView>(R.id.iv_user_image_myProfile)

// Profile Image
//        Glide
//            .with(this)
//            .load(user.image)
//            .centerCrop()
//            .placeholder(R.drawable.ic_user_place_holder)
//            .into(headerBinding)

// Username
      //  headerView.findViewById<TextView>(R.id.tv_userName).text = user.name
        val viewHeader = binding.navView.getHeaderView(0)
        val headerBinding = viewHeader?.let { NavHeaderMainBinding.bind(it) }
        //Glide.with(myFragment).load(url).centerCrop().placeholder(R.drawable.loading_spinner).into(myImageView)
        headerBinding?.navUserImage?.let {
        Glide.with(this).load(user.image).centerCrop().placeholder(R.drawable.ic_user_place_holder).into(it)} //circleCrop
        headerBinding?.tvUserName?.text = user.name
        if (readBoardsList){
          // showProgressDialog(resources.getString(R.string.please_wait)) removed
            FirestoreClass().getBoardsList(this)
        }
    }

    fun populateBoardsListToUI(boardsList:ArrayList<Board>){
      // hideProgressDialog() //removed
        val rvBoardsList:RecyclerView=findViewById(R.id.rv_BoardsList)
        val tvNoBoardsAvailable:TextView=findViewById(R.id.tv_noBoardsAvailable)
      // hideProgressDialog()
        if (boardsList.size>0){
            rvBoardsList.visibility=View.VISIBLE
            tvNoBoardsAvailable.visibility=View.GONE
            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)
            val adapter=BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter=adapter
            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent=Intent(this@MainActivity,TaskList::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentID)
                    startActivity(intent)
                }
            })
        }
        else{

            rvBoardsList.visibility=View.GONE
            tvNoBoardsAvailable.visibility=View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor:SharedPreferences.Editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this,true) // it should reload the screen
    }

    private fun updateFCMToken(token:String){ //updating the token inside of our database
        val userHashMap=HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token
      //  showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this,userHashMap)

    }

}