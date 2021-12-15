package com.example.wowCamera.adapters;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wowCamera.R;
import com.example.wowCamera.utils.MyDatabaseHelper;

import java.util.ArrayList;

public class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.ViewHolder>{

    ArrayList<String> urls;
    Context context;
    ArrayList<Boolean> selectList  = new ArrayList<>();
    Boolean muti_select = false;//多选状态
    Boolean DeleteState = false;//删除状态
    String fotime = "albumid";
    ArrayList<String> scoreList = new ArrayList<>();

    //constructor
    public ImgAdapter(ArrayList<String> ImgUrl, Context context_)
    {
        this.urls = ImgUrl;
        this.context = context_;

        for (int i=0;i<ImgUrl.size();i++){
            selectList.add(false);
            scoreList.add("");
        }

    }


    public interface  onItemClickListener{
        void onItemClick(View view ,int position);
        void  onItemLongClick(View view,int position);
    }
    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(onItemClickListener onItemClickListener){
        this.onItemClickListener=onItemClickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView image;
        private ImageView selectIcon;
        private ImageView selectIcon1;
        private TextView textView;
        private ImageView deleteIcon;


        public ViewHolder(View v)
        {
            super(v);
            image =(ImageView)v.findViewById(R.id.img);
            selectIcon = (ImageView)v.findViewById(R.id.selected);
            selectIcon1 = (ImageView)v.findViewById(R.id.selected1);
            deleteIcon = (ImageView)v.findViewById(R.id.deleteIcon);
            textView = (TextView) v.findViewById(R.id.name);
        }

        public ImageView getImage(){ return this.image;}

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);



        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {

        Glide.with(this.context).load(urls.get(position)).into(holder.getImage());

        if(scoreList.get(position).equals("-40.00")){
            holder.textView.setText("");

        }else{
            holder.textView.setText("分数:"+scoreList.get(position));

        }



        if (onItemClickListener!=null){
            holder.deleteIcon.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    delOnePhoto(position,fotime);
                }
            });


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPos=holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView,layoutPos);
                    //显示标记
                    if (muti_select){//进入多选状态
                        if (selectList.get(position)){//选中
                            holder.selectIcon1.setVisibility(View.VISIBLE);
                            holder.selectIcon.setVisibility(View.GONE);
                        }else {
                            holder.selectIcon1.setVisibility(View.GONE);
                            holder.selectIcon.setVisibility(View.VISIBLE);
                        }

                    }
                    else {
                        holder.selectIcon.setVisibility(View.INVISIBLE);
                        holder.selectIcon1.setVisibility(View.GONE);
                    }


                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//
                    int layoutPos=holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView,layoutPos);
                    return false;
                }
            });
            if (muti_select){
                holder.selectIcon.setVisibility(View.VISIBLE);
                holder.selectIcon1.setVisibility(View.GONE);
                getChecked(position);
                if (selectList.get(position)){
                    holder.selectIcon1.setVisibility(View.VISIBLE);
                    holder.selectIcon.setVisibility(View.GONE);
                }
            }else {
                holder.selectIcon.setVisibility(View.INVISIBLE);
                holder.selectIcon1.setVisibility(View.GONE);

            }
            //进入删除模式
            if(DeleteState){
                holder.deleteIcon.setVisibility(View.VISIBLE);
            }else{
                holder.deleteIcon.setVisibility(View.GONE);
            }



        }
    }

    @Override
    public int getItemCount()
    {
        return urls.size();
    }

    public void setDeleteState(boolean deleteState){
        this.DeleteState = deleteState;
        notifyDataSetChanged();
    }

    public void setFotime(String fotime){
        this.fotime = fotime;
        notifyDataSetChanged();
    }
    public void setScoreList(ArrayList<String> arrayList){
        scoreList = new ArrayList<>();
        for (int i=0;i<arrayList.size();i++){
            scoreList.add(arrayList.get(i));
        }
        notifyDataSetChanged();
    }


    public void setMuti_select(boolean muti_select1){
        this.muti_select = muti_select1;
        notifyDataSetChanged();
    }


    public void setCheck(int position,boolean isSelect){
        selectList.set(position,isSelect);

    }
    public boolean getChecked(int position) {
        return this.selectList.get(position);
    }

    public ArrayList<Boolean> getSelectList(){
        return this.selectList;
    }
    public int getSelectNum(){
        int count = 0;
        for (boolean isSelect :this.selectList){
            if(isSelect){
                count++;
            }
        }
        return count;
    }

    public void initSelectList(){
        selectList  = new ArrayList<>();
        for (int i=0;i<this.urls.size();i++){
            selectList.add(false);
        }
    }

    public void delOnePhoto(int position,String fotime){
        MyDatabaseHelper dbHelper;

        dbHelper = new MyDatabaseHelper(context, "wowCamera.db",  3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        Cursor cursor = db.query("Album", null, null, null, null, null, null, position +",1");
        String orderBy = "score desc";
        if(fotime == "albumid"){
            orderBy = null;
        }
        String[] selectionArgs = new String[]{fotime};
        String table = "Album";
        String limit = position +",1";
        Cursor cursor = db.query(table,null,"fotime = ?",selectionArgs,null,null,orderBy,limit);

        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                String id = cursor.getString(cursor.getColumnIndex("id"));
                db.execSQL("delete from Album where id = ?",new String []{id});
            } while (cursor.moveToNext());
        }
        cursor.close();
        selectList = new ArrayList<>();
        urls.remove(position);
        scoreList.remove(position);
        notifyDataSetChanged();
        Log.d("qxysql", "delonephoto-array-size: "+urls.size()+"---"+scoreList.size());


    }
    public void delAdapterPhoto(int position){
        urls.remove(position);
        scoreList.remove(position);
        selectList.remove(position);
        notifyDataSetChanged();
        Log.d("qxysql", "delAdapterPhoto: "+urls.size()+"---"+scoreList.size());

    }
    public void delSqlPhotoById(String id){
        MyDatabaseHelper dbHelper;

        dbHelper = new MyDatabaseHelper(context, "wowCamera.db",  3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from Album where id = ?",new String []{id});
//        delAdapterPhoto(position);
    }

    public void addAdapterPhoto(String img,String score){
        this.urls.add(img);
        this.scoreList.add(score);
        this.selectList.add(false);
        notifyItemInserted(getUrls().size());

        Log.d("qxysql", "array-size: "+urls.size()+"---"+scoreList.size());
    }
    public ArrayList<String> getUrls(){
        return urls;
    }
    public ArrayList<String> getScoreList(){
        return scoreList;
    }
}
