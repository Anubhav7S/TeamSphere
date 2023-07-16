package com.example.teamsphere

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class LabelColorListDialog(context: Context, private var list:ArrayList<String>, private var title:String="",
                                    private var mSelectedColor:String=""):Dialog(context){ //we won't create direct objects of this class
// but we will inherit it and directly access its elements
    private var adapter:LabelColorListItemsAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list,null)

        setContentView(view)

        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
//        Toast.makeText(context,"working", Toast.LENGTH_SHORT).show()
    }

    private fun setUpRecyclerView(view: View){
        view.findViewById<TextView>(R.id.tv_title_dialogList).text=title
        view.findViewById<RecyclerView>(R.id.rv_List_dialogList).layoutManager = LinearLayoutManager(context)
        adapter= LabelColorListItemsAdapter(context,list, mSelectedColor)
        view.findViewById<RecyclerView>(R.id.rv_List_dialogList).adapter=adapter
        adapter!!.onItemClickListener=object : LabelColorListItemsAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }

        }
    }

    protected abstract fun onItemSelected(color:String) //what should happen once we select a color



}