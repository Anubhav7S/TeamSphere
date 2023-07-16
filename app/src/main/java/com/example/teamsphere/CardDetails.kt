package com.example.teamsphere

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.teamsphere.databinding.ActivityCardDetailsBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetails : BaseActivity() {
    private var binding:ActivityCardDetailsBinding?=null
    private lateinit var mBoardDetails:Board
    private var mTaskListPosition=-1
    private var mCardPosition=-1
    private var mSelectedColor=""
    private lateinit var mMembersDetailList:ArrayList<User>
    private var mSelectedDueDateInMilliSeconds:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        setupActionBar()
        binding?.etNameCardDetails?.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length) // set the focus
    // directly on ending of the length of the text i.e puts cursor on ending of text
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()){
            setColor()
        }
        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etNameCardDetails?.text.toString().isNotEmpty()){
                updateCardDetails()
            }
            else{
                Toast.makeText(this,"Please enter a name for the card!",Toast.LENGTH_SHORT).show()
            }
        }
        binding?.tvSelectLabelColor?.setOnClickListener {
            labelColorsListDialog()
        }
        binding?.tvSelectMembers?.setOnClickListener {
            membersListDialog()
        }

        setUpSelectedMembersList()

        mSelectedDueDateInMilliSeconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if (mSelectedDueDateInMilliSeconds>0){
            val simpleDateFormat=SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate=simpleDateFormat.format(Date(mSelectedDueDateInMilliSeconds))
            binding?.tvSelectDueDate?.text=selectedDate
        }

        binding?.tvSelectDueDate?.setOnClickListener {
            showDatePicker()
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCardDetails)
        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            // supportActionBar?.title= title
          //  supportActionBar?.title= resources.getString(R.string.members)
            supportActionBar?.title=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
            binding?.toolbarCardDetails?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
                R.id.action_delete_card->{
                    alertDialogForDeletingCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                    return true
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails= IntentCompat.getParcelableExtra(intent,Constants.BOARD_DETAIL,Board::class.java)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition= intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mMembersDetailList=intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST,User::class.java)!!
            }
            else{
                mMembersDetailList=IntentCompat.getParcelableArrayListExtra(intent,Constants.BOARD_MEMBERS_LIST,User::class.java)!!
            }
            // mMembersDetailList=IntentCompat.getParcelableExtra(intent,Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
       // FirestoreClass().getBoardDetails(this@CardDetails, mBoardDetails)
        val intent=Intent(this,TaskList::class.java)
        intent.putExtra(Constants.DOCUMENT_ID,mBoardDetails.documentID)
        //finish()
        startActivity(intent)
        finish()
    }

    private fun updateCardDetails(){
        val card=Card(binding?.etNameCardDetails?.text.toString(), mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].createdBy, mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,mSelectedDueDateInMilliSeconds)

        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size -1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetails,mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList:ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)
        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1) //taskList has no. of elements + add card,
    // so we want to make changes in database hence we don't want add card as an element in the task list
        taskList[mTaskListPosition].cards = cardsList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetails,mBoardDetails)
    }

    private fun alertDialogForDeletingCard(cardName:String){
        val builder=AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card,cardName))
        builder.setPositiveButton(resources.getString(R.string.yes)){ dialogInterface,which->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)){ dialogInterface,which->
            dialogInterface.dismiss()
        }
        val alertDialog:AlertDialog=builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList():ArrayList<String>{
        val colorsList:ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColor(){
        binding?.tvSelectLabelColor?.text=""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
//        Toast.makeText(this,"working",Toast.LENGTH_SHORT).show()
    }

    private fun labelColorsListDialog(){

        val colorsList:ArrayList<String> = colorsList()

        val listDialog=object : LabelColorListDialog(this,colorsList,
            resources.getString(R.string.str_select_label_color),mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun membersListDialog(){
        var cardAssignedMembersList=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo //to know
    // which members are assigned to us
        if (cardAssignedMembersList.size>0){  //do we have any members in the list
            for (i in mMembersDetailList.indices){ //go through all members of the list
                for (j in cardAssignedMembersList){ // for every single member in the list, check if its id is same as the
                    // member that is assigned to the card because we have people assigned to the card and we have people assigned to the board
                    if (mMembersDetailList[i].id==j){ // if someone who is a member of the board is also a member of the card
                        mMembersDetailList[i].selected=true
                    }
                }
            }
        }
        else{
            for (i in mMembersDetailList.indices){ // if no one is in the list of people who are assigned to the card
                // then no one is selected
                mMembersDetailList[i].selected=false
            }
        }

        val listDialog=object : MembersListDialog(this,mMembersDetailList,resources.getString(R.string.select_member)){
            override fun onItemSelected(user: User, action: String) {
                if (action==Constants.SELECT){
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }

                }
                else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)
                    for(i in mMembersDetailList.indices){
                        if (mMembersDetailList[i].id==user.id){
                            mMembersDetailList[i].selected=false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }
        listDialog.show()

    }

    private fun setUpSelectedMembersList(){ // to refresh the list whenever we make changes
        val cardAssignedMemberList=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()
        for (i in mMembersDetailList.indices){
            for (j in cardAssignedMemberList){
                if (mMembersDetailList[i].id==j){
                    val selectedMember=SelectedMembers(mMembersDetailList[i].id,mMembersDetailList[i].image)
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        if (selectedMembersList.size>0){ // to display the blue + add member button in recycler view of card members
            selectedMembersList.add(SelectedMembers("",""))
            binding?.tvSelectMembers?.visibility=View.GONE
            binding?.rvSelectedMembersList?.visibility=View.VISIBLE
            binding?.rvSelectedMembersList?.layoutManager=GridLayoutManager(this,6,) // span count
        // is number of elements which we want to display on teh screen next to each other
            val adapter=CardMemberListItemsAdapter(this,selectedMembersList,true)
            binding?.rvSelectedMembersList?.adapter=adapter
            adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                override fun onClick() {
                    membersListDialog()
                }

            })
        }
        else{
            binding?.tvSelectMembers?.visibility=View.VISIBLE
            binding?.rvSelectedMembersList?.visibility=View.GONE
        }
    }

    private fun showDatePicker(){ // setting up the calendar
        val calendar=Calendar.getInstance()
        val year=calendar.get(Calendar.YEAR)
        val month=calendar.get(Calendar.MONTH)
        val day=calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog=DatePickerDialog(this,DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val sDayOfMonth=if (dayOfMonth<10) "0$dayOfMonth" else "$dayOfMonth"
            val sMonthOfYear=if (monthOfYear+1<10) "0${monthOfYear+1}" else "$monthOfYear"
            val selectedDate="$sDayOfMonth/$sMonthOfYear/$year"
            binding?.tvSelectDueDate?.text=selectedDate

            val sdf=SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val theDate=sdf.parse(selectedDate)
            mSelectedDueDateInMilliSeconds=theDate!!.time
        },year,month,day)
        datePickerDialog.show()

    }
}