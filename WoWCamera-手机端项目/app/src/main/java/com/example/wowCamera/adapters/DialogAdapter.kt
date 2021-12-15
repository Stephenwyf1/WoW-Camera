package com.example.wowCamera.adapters

import android.app.ActionBar
import android.content.Context
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.wowCamera.R
import com.example.wowCamera.utils.ConfigListener
import com.example.wowCamera.utils.HttpUtils
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

/**
 * DialogItemAdapter
 */
class DialogAdapter(val context: Context,val list: ArrayList<ConfigStyle>) : RecyclerView.Adapter<DialogAdapter.ViewHolder>(){
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
     var typeContent: TextView = view.findViewById(R.id.tv)
     var delete_config : MaterialButton = view.findViewById(R.id.delete_config)
     var rename_config : MaterialButton = view.findViewById(R.id.rename_config)
     var use_config : MaterialButton = view.findViewById(R.id.use_config)

}

    interface OnItemClickListener {
        fun onItemClick(config:String)
//        fun onItemLongClick(view: View?, position: Int)
    }
    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.configitem, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int { return list.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val configStyle = list[position]
            holder.typeContent.text = configStyle.name
            holder.delete_config.setOnClickListener{
                Log.d("xdy", "delete_config: ")
                val id = list[position].id
                val json = JSONObject()
                json.put("id",id.toString())
                HttpUtils.sendConfig(json,3, object : ConfigListener {
                    override fun success(json: String) {
                        Toast.makeText(context,"删除成功", Toast.LENGTH_SHORT).show()
                    }

                    override fun error() {
                        Toast.makeText(context,"删除失败", Toast.LENGTH_SHORT).show()
                    }

                })
                list.removeAt(position)
                notifyDataSetChanged()

            }

            holder.rename_config.setOnClickListener {
                val popupWindow: PopupWindow
                val pview = LayoutInflater.from(context).inflate(R.layout.popup_window, null)
                popupWindow = PopupWindow(pview, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true)
                popupWindow.contentView = pview
                popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS) //这里给它设置了弹出的时间
                val editText = pview.findViewById<EditText>(R.id.editText)
                editText.requestFocus()
                editText.setText(list!![position].name)
                editText.selectAll()
                val confirm = pview.findViewById<Button>(R.id.confirm)
                val cancel = pview.findViewById<Button>(R.id.cancle)

                //显示PopupWindow
                val rootview = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
                popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)
                cancel.setOnClickListener {
                    popupWindow.dismiss() //让PopupWindow消失
                }
                confirm.setOnClickListener {
                    val inputString = editText.text.toString()
                    val json = JSONObject()
                    json.put("id",list[position].id.toString())
                    json.put("name",inputString)
                    HttpUtils.sendConfig(json,4, object : ConfigListener {
                        override fun success(json: String) {
                            Toast.makeText(context,"重命名成功", Toast.LENGTH_SHORT).show()
                        }
                        override fun error() {
                            Toast.makeText(context,"重命名失败", Toast.LENGTH_SHORT).show()
                        }
                    })
                    list!![position].name = inputString
                    popupWindow.dismiss() //让PopupWindow消失
                    notifyDataSetChanged()
                }
            }

            if (onItemClickListener!=null){
                holder.use_config.setOnClickListener{
                    val lauOutpos = holder.layoutPosition
                    onItemClickListener!!.onItemClick(configStyle.config)
                }
            }


    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

}