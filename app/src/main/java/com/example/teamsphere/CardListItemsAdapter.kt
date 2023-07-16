package com.example.teamsphere

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class CardListItemsAdapter(private val context : Context, private var list:ArrayList<Card>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener : OnClickListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
//        if (model.labelColor.isNotEmpty()){
//            holder.itemView.findViewById<View>(R.id.view_label_colour).visibility=View.VISIBLE
//            holder.itemView.findViewById<View>(R.id.view_label_colour).setBackgroundColor(Color.parseColor(model.labelColor))
//        }
//        else{
//            holder.itemView.findViewById<View>(R.id.view_label_colour).visibility=View.GONE
//        }
        if (holder is MyViewHolder){
            if (model.labelColor.isNotEmpty()){
                holder.itemView.findViewById<View>(R.id.view_label_colour).visibility=View.VISIBLE
                holder.itemView.findViewById<View>(R.id.view_label_colour).setBackgroundColor(Color.parseColor(model.labelColor))
            }
            else{
                holder.itemView.findViewById<View>(R.id.view_label_colour).visibility=View.GONE
            }
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text= model.name

            if ((context as TaskList).mAssignedMemberDetailList.size>0){
                val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()
                for (i in context.mAssignedMemberDetailList.indices){
                    for (j in model.assignedTo){
                        if (context.mAssignedMemberDetailList[i].id==j){
                            val selectedMembers = SelectedMembers(context.mAssignedMemberDetailList[i].id,context.mAssignedMemberDetailList[i].image)
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if (selectedMembersList.size>0){
                    if (selectedMembersList.size==1 && selectedMembersList[0].id==model.createdBy){ //if you are the creator of the board then you need not see your image on the card
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selectedCardMemberList).visibility=View.GONE
                    }
                    else{
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selectedCardMemberList).visibility=View.VISIBLE
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selectedCardMemberList).layoutManager=GridLayoutManager(context,4)
                        val adapter=CardMemberListItemsAdapter(context,selectedMembersList,false)
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selectedCardMemberList).adapter=adapter
                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener!=null){
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })
                    }
                }
                else{
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selectedCardMemberList).visibility=View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    interface OnClickListener {
        //fun onClick(position: Int, card:Card)
        fun onClick(position: Int)
    }

    class MyViewHolder(view:View):RecyclerView.ViewHolder(view)
}