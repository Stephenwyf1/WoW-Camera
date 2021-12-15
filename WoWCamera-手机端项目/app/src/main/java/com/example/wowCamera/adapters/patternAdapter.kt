package com.example.wowCamera.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wowCamera.R

class patternAdapter(val patternList: List<pattern>):
        RecyclerView.Adapter<patternAdapter.ViewHolder>(){
            inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
                val patternView = view
                val patternImage : ImageView = view.findViewById(R.id.patternImage)
                val patternName : TextView = view.findViewById(R.id.patternName)
            }
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onItemLongClick(view: View?, position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pattern_item, parent, false)
        val viewHolder = ViewHolder(view)
//        viewHolder.patternImage.setOnClickListener {}

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pattern = patternList[position]
        holder.patternImage.setImageBitmap(pattern.imgBitmap)
        holder.patternName.text = pattern.name

        if (onItemClickListener != null) {
            holder.patternView.setOnClickListener {
                val layoutPos = holder.layoutPosition
                onItemClickListener!!.onItemClick(holder.patternView, layoutPos)
            }
            holder.patternView.setOnLongClickListener {
                val layoutPos = holder.layoutPosition
                onItemClickListener!!.onItemLongClick(holder.patternView, layoutPos)
                false
            }
        }
    }

    override fun getItemCount() = patternList.size
}