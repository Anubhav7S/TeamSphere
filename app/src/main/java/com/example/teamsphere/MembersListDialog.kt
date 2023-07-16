package com.example.teamsphere

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class MembersListDialog(context: Context, private var list:ArrayList<User>, private var title:String="")
    : Dialog(context) {
    private var adapter:MembersListItemsAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view= LayoutInflater.from(context).inflate(R.layout.dialog_list,null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View){
        view.findViewById<TextView>(R.id.tv_title_dialogList).text=title
        if (list.size>0){
            view.findViewById<RecyclerView>(R.id.rv_List_dialogList).layoutManager=LinearLayoutManager(context)
            adapter= MembersListItemsAdapter(context,list)
            view.findViewById<RecyclerView>(R.id.rv_List_dialogList).adapter=adapter

            adapter!!.setOnClickListener(object : MembersListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user:User,action:String)

}