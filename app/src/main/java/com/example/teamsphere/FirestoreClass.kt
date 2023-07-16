package com.example.teamsphere

import android.app.Activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    //this class will take care of the firestore database stuff.

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(
        activity: SignUp,
        userInfo: User
    ) { // the user used is the User class that was created
        mFireStore.collection(Constants.USERS) //creating a new collection with the name of USERS
            .document(getCurrentUserID()).set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
            } //create a new document for every single user we have using their unique uid
        // merge whatever user info is passed to us
    }

    fun createBoard(activity:CreateBoard, board:Board){
        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge()) //merge the data if it
            // already exits instead of overwriting it
            .addOnSuccessListener {
                Toast.makeText(activity,"Board created successfully",Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception->
                Toast.makeText(activity,"Error while creating the board",Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity: Activity , readBoardsList:Boolean=false) {
        mFireStore.collection(Constants.USERS) //creating a new collection
            .document(getCurrentUserID()).get().addOnSuccessListener {document->
                val loggedInUser = document.toObject(User::class.java) // when using toObject method we need to mention
            // of which class we want to use the object.
            // Here we make a user object of whatever is being given in the document, user object will be the uid
                when(activity){
                    is SignIn->{
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity->{
                        if (loggedInUser != null) {
                            //Toast.makeText(activity,"Chal raha hai",Toast.LENGTH_LONG).show()
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                        }
                    }
                    is MyProfile->{
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
//                if (loggedInUser!=null)
//                    activity.signInSuccess(loggedInUser)
            }.addOnFailureListener {
                e->
                when(activity){
                    is SignIn->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                    is MyProfile->{
                        activity.hideProgressDialog()

                    }
                }
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS).whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserID()).get()
            .addOnSuccessListener {
                document->
              //  Toast.makeText(activity,"getBoardsList working",Toast.LENGTH_SHORT).show()
                val boardList:ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board = i.toObject(Board::class.java) //create a board object from the java object
                    if (board != null) {
                        board.documentID=i.id
                    }
                    boardList.add(board!!)

                }
                activity.populateBoardsListToUI(boardList)

            }.addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error in getting boards list",Toast.LENGTH_SHORT).show()
            }
    }

    fun updateUserProfileData(activity:Activity, userHashMap:HashMap<String,Any>) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).update(userHashMap).addOnSuccessListener {
            Toast.makeText(activity,"Profile details updated successfully",Toast.LENGTH_SHORT).show()
            when(activity){
                is MainActivity->{
                    activity.tokenUpdateSuccess()
                }
                is MyProfile->{
                    activity.profileUpdateSuccess()
                }
            }
//            activity.profileUpdateSuccess()
        }.addOnFailureListener {
            exception->
            when(activity){
                is MainActivity->{
                    activity.hideProgressDialog()
                }
                is MyProfile->{
                    activity.hideProgressDialog()
                }
            }
            //activity.hideProgressDialog()
            Toast.makeText(activity,"Error when updating the profile",Toast.LENGTH_SHORT).show()
        }
    }


    fun getCurrentUserID():String{
        var currentUser=FirebaseAuth.getInstance().currentUser
        var currentUserID=""
        if (currentUser!=null){
            currentUserID=currentUser.uid
        }
        return currentUserID
        //return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun getBoardDetails(activity: TaskList, documentID: String) {
        mFireStore.collection(Constants.BOARDS).document(documentID).get()
            .addOnSuccessListener {
                    document->
             //   Toast.makeText(activity,"getBoardsList working",Toast.LENGTH_SHORT).show()
                val board = document.toObject(Board::class.java)!!
                board.documentID=document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error in getting boards list",Toast.LENGTH_SHORT).show()
            }
    }

    fun addUpdateTaskList(activity: Activity,board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS).document(board.documentID).update(taskListHashMap).addOnSuccessListener {
            //Toast.makeText(activity,"addUpdateTaskList working",Toast.LENGTH_SHORT).show()
            if (activity is TaskList){
                activity.addUpdateTaskListSuccess()
            }
            else if (activity is CardDetails){
               // FirestoreClass().getBoardDetails(activity as TaskList,board.documentID)
                activity.addUpdateTaskListSuccess()
            }

        }.addOnFailureListener {
            exception->
            if (activity is TaskList){
                activity.hideProgressDialog()
            }
            else if (activity is CardDetails){
                activity.hideProgressDialog()
            }
            Toast.makeText(activity,"Error in updating",Toast.LENGTH_SHORT).show()
        }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo:ArrayList<String>){
        mFireStore.collection(Constants.USERS) // constants.users=id i.e mFireStore.collection(id) is the id
    // which we are accessing in firebase
            .whereIn(Constants.ID,assignedTo) //wherein constants.id=assigned to
            .get().addOnSuccessListener {
                document->
               // Toast.makeText(activity,"getAssignedMembersListDetails working",Toast.LENGTH_SHORT).show()
                val usersList:ArrayList<User> = ArrayList()
                for (i in document.documents){
                    val user=i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if (activity is Members){
                    activity.setUpMembersList(usersList)
                }
                else if (activity is TaskList){
                    activity.boardMembersDetailsList(usersList)
                }

            }.addOnFailureListener {e->
                if (activity is Members){
                    activity.hideProgressDialog()
                }
                else if (activity is TaskList){
                    activity.hideProgressDialog()
                }
//                activity.hideProgressDialog()
                Toast.makeText(activity,"getAssignedMembersListDetails not working",Toast.LENGTH_SHORT).show()
            }
    }

    fun getMemberDetails(activity:Members,email:String){
        mFireStore.collection(Constants.USERS).whereEqualTo(Constants.EMAIL,email).get().addOnSuccessListener {
            document->
            if (document.documents.size>0){ //if size > 0 , i.e if there are any documents present
                val user=document.documents[0].toObject(User::class.java)!! //[0] because each user will have his
                // unique email ID, 1 user won't have more than 1 email ID
                activity.memberDetails(user)
            }
            else{
                activity.hideProgressDialog()
                activity.showErrorSnackBar("No such member found!")
            }
        }
            .addOnFailureListener {e->
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error while getting user details",Toast.LENGTH_SHORT).show()
            }
    }

    fun assignMemberToBoard(activity:Members,board: Board, user: User){
        val assignedToHashMap= HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo //download board from database
        // after we assign a member to a board, we are updating it in the database
        mFireStore.collection(Constants.BOARDS).document(board.documentID) // we want to update the board that we are
    // currently working on or where the user has entered a new entry for the members
            .update(assignedToHashMap).addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error while creating a board",Toast.LENGTH_SHORT).show()
            }
    }
}