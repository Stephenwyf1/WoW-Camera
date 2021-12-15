package com.example.wowCamera.adapters

import android.app.ActionBar
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wowCamera.R
import com.google.android.material.button.MaterialButton

class configAdapter(private val configList: List<ConfigStyle>, var context: Context) :
    RecyclerView.Adapter<configAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val configView = view
        val configName: TextView = view.findViewById(R.id.tv)
        val rename_config: MaterialButton = view.findViewById(R.id.rename_config)
        val delete_config: MaterialButton = view.findViewById(R.id.delete_config)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.configitem, parent, false)
        val viewHolder = ViewHolder(view)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val config = configList[position]
        holder.configName.text = config.name

        if (onItemClickListener != null) {
            holder.configView.setOnClickListener {
                val layoutPos = holder.layoutPosition
                onItemClickListener!!.onItemClick(holder.configView, layoutPos)
            }
            holder.configView.setOnLongClickListener {
                val layoutPos = holder.layoutPosition
                onItemClickListener!!.onItemLongClick(holder.configView, layoutPos)
                false
            }
            holder.rename_config.setOnClickListener {
                val popupWindow: PopupWindow
                val pview = LayoutInflater.from(context).inflate(R.layout.popup_config, null)
                popupWindow = PopupWindow(
                    pview,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.contentView = pview
                popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                val inputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(
                    0,
                    InputMethodManager.HIDE_NOT_ALWAYS
                ) //这里给它设置了弹出的时间
                val editText = pview.findViewById<EditText>(R.id.editText)
                editText.requestFocus()
                editText.setText(configList!![position].name)
                editText.selectAll()
                val confirm = pview.findViewById<Button>(R.id.confirm)
                val cancel = pview.findViewById<Button>(R.id.cancle)
                val delete_config = pview.findViewById<Button>(R.id.delete_config)

                //显示PopupWindow
                val rootview = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
                popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)
                cancel.setOnClickListener {
                    popupWindow.dismiss() //让PopupWindow消失
                }
                confirm.setOnClickListener {
                    val inputString = editText.text.toString()
                    configList!![position].name = inputString
                    popupWindow.dismiss() //让PopupWindow消失
                    notifyDataSetChanged()
                }
            }
            holder.delete_config.setOnClickListener {
                val layoutPos = holder.layoutPosition
                onItemClickListener!!.onItemClick(holder.configView, layoutPos)
                Log.d(TAG, "onBindViewHolder: " + position.toString())
                notifyDataSetChanged()
            }

        }
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}