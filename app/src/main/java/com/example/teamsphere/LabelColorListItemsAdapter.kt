package com.example.teamsphere

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class LabelColorListItemsAdapter(private val context: Context, private var list:ArrayList<String>,
                                      private val mSelectedColor:String):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener:OnItemClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_label_color,parent,false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item=list[position]
        if (holder is MyViewHolder){
            //Toast.makeText(context,"working", Toast.LENGTH_SHORT).show()
           holder.itemView.findViewById<View>(R.id.view_main).setBackgroundColor(Color.parseColor(item))
            if (item==mSelectedColor){
                holder.itemView.findViewById<ImageView>(R.id.iv_selectedColor).visibility=View.VISIBLE
            }
            else{
                holder.itemView.findViewById<ImageView>(R.id.iv_selectedColor).visibility=View.GONE
            }
            holder.itemView.setOnClickListener {
                if (onItemClickListener!=null){
                    onItemClickListener!!.onClick(position,item)
                }
            }
        }
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)

    interface OnItemClickListener{
        fun onClick(position: Int, color:String)
    }
}