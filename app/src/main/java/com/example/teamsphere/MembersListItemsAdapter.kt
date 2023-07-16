package com.example.teamsphere

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MembersListItemsAdapter(private val context: Context, private var list:ArrayList<User>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener:OnClickListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_member,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if (holder is MyViewHolder){
            Glide.with(context).load(model.image).centerCrop().placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_memberImage_member))
            holder.itemView.findViewById<TextView>(R.id.tv_memberName_member).text=model.name
            holder.itemView.findViewById<TextView>(R.id.tv_memberEmail_member).text=model.email

            if (model.selected){
                holder.itemView.findViewById<ImageView>(R.id.iv_selectedMember).visibility=View.VISIBLE
            }
            else{
                holder.itemView.findViewById<ImageView>(R.id.iv_selectedMember).visibility=View.GONE
            }
            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    if (model.selected){
                        onClickListener!!.onClick(position,model,Constants.UN_SELECT)
                    }
                    else{
                        onClickListener!!.onClick(position,model,Constants.SELECT)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view)

    interface OnClickListener{
        fun onClick(position:Int,user:User, action:String)
    }

}