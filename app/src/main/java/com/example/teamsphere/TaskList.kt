package com.example.teamsphere

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamsphere.databinding.ActivityTaskListBinding
import java.io.IOException

class TaskList : BaseActivity() {
    private var binding: ActivityTaskListBinding? =null
    private lateinit var mBoardDetails:Board
    private lateinit var mBoardDocumentID:String
    //private lateinit var mAssignedMemberDetailList:ArrayList<User>
    lateinit var mAssignedMemberDetailList:ArrayList<User>

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data!=null) {
            val intent = result.data
            if (intent != null) {
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getBoardDetails(this@TaskList , mBoardDocumentID)

            }
            else{
                Toast.makeText(this,"Error in refreshing",Toast.LENGTH_SHORT).show()
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)
      //  var boardDocumentID=""
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentID= intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
//        if (intent.hasExtra(Constants.TRANSFER)){
//            mBoardDocumentID= intent.getStringExtra(Constants.DOCUMENT_ID)!!
//        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskList , mBoardDocumentID)


    }

//    override fun onResume() {
//      //  showProgressDialog(resources.getString(R.string.please_wait))
//      //  FirestoreClass().getBoardDetails(this@TaskList , mBoardDocumentID)
//        super.onResume()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members->{
                val intent=Intent(this, Members::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails) // we are able to pass the whole object
                // because we are using a parcelable in the form of Board, here mBoardDetails is of type board
                startForResult.launch(intent)
                //startActivity(intent)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarTaskList)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
           // supportActionBar?.title= title
            supportActionBar?.title= mBoardDetails.name
            binding?.toolbarTaskList?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun boardDetails(board: Board){
        mBoardDetails=board
        hideProgressDialog() //to remove extra 'please wait' dialog box 1
       // setupActionBar(board.name)
        setupActionBar()
//        val addTaskList=Task(resources.getString(R.string.addList))
//        //board.taskList.add(addTaskList)
//        mBoardDetails.taskList.add(addTaskList)
//        val rvTaskList: RecyclerView =findViewById(R.id.rv_taskList)
//        rvTaskList.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        rvTaskList.setHasFixedSize(true)
//        //val adapter=TaskListItemsAdapter(this,board.taskList)
//        val adapter=TaskListItemsAdapter(this@TaskList,mBoardDetails.taskList)
//        rvTaskList.adapter=adapter

       // showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskList, mBoardDetails.documentID)
    }

    fun createTaskList(taskListName:String){
        val task=Task(taskListName,FirestoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun updateTaskList(position:Int, listName:String, model:Task){
        val task=Task(listName,model.createdBy)
        mBoardDetails.taskList[position]=task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskList,mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        val cardAssignedUsersList:ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID()) // get the assigned users
        val card=Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)
        val cardsList=mBoardDetails.taskList[position].cards // gives the task list position in which we currently are at
    // (which list) and .cards gives the cards associated with that particular list
        cardsList.add(card)

        val task= Task(mBoardDetails.taskList[position].title, mBoardDetails.taskList[position].createdBy,cardsList)
    //replace the old task with the updated task which also contains the new cards list
        mBoardDetails.taskList[position]=task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun cardDetails(taskListPosition:Int, cardPosition:Int){
        val intent = Intent(this,CardDetails::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailList)
        startActivity(intent)
        finish()
    }

    fun boardMembersDetailsList(list: ArrayList<User>){
        mAssignedMemberDetailList = list
       // hideProgressDialog()
        val addTaskList=Task(resources.getString(R.string.addList))
        //board.taskList.add(addTaskList)
        mBoardDetails.taskList.add(addTaskList)
        val rvTaskList: RecyclerView =findViewById(R.id.rv_taskList)
        rvTaskList.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTaskList.setHasFixedSize(true)
        //val adapter=TaskListItemsAdapter(this,board.taskList)
        val adapter=TaskListItemsAdapter(this@TaskList,mBoardDetails.taskList)
        rvTaskList.adapter=adapter
    }

    fun updateCardsPositionInTaskList(taskListPosition: Int, cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[taskListPosition].cards=cards
        //showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
}