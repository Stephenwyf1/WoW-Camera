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

import com.example.wowCamera.AlbumActivity;
import com.example.wowCamera.R;
import com.example.wowCamera.utils.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder>{
    List<Folder> folderArrayList = new ArrayList<>();
    Context context;
    boolean selected = false;



    public FolderAdapter(Context context1, List<Folder> folderList) {

        folderArrayList = folderList;

    }
    public interface onItemClickListener{
        void onItemClick(View view ,int position);
        void  onItemLongClick(View view,int position);
    }
    private onItemClickListener onItemClickListener;
    public void setOnItemClickListener(onItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View foldView;
        ImageView FolderImage;
        TextView foldName;
        TextView createTime;
        ImageView folder_select;

        public ViewHolder(View view) {
            super(view);
            foldView = view;
            FolderImage = (ImageView) view.findViewById(R.id.img);
            folder_select = (ImageView) view.findViewById(R.id.folder_select);
            foldName = (TextView) view.findViewById(R.id.name);
            createTime = (TextView) view.findViewById(R.id.create_time);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_folder, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    //    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        Folder folder = folderArrayList.get(position);
        holder.FolderImage.setImageResource(folder.getImageId());
        holder.foldName.setText(folder.getName());
        holder.createTime.setText(folder.getCreateTime());
        if (folder.getSelected()){
            holder.folder_select.setVisibility(View.VISIBLE);
        }else{
            holder.folder_select.setVisibility(View.GONE);
        }

        if (onItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPos=holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView,layoutPos);


                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int layoutPos=holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView,layoutPos);

                    return false;
                }
            });

        }


    }

    @Override
    public int getItemCount() {
        return folderArrayList.size();
    }

    public void cancle_select(){
        for (int i =0;i<folderArrayList.size();i++){
            folderArrayList.get(i).setSelected(false);
        }
        notifyDataSetChanged();
    }
    public void setSelectVisible(int position){
        folderArrayList.get(position).setSelected(true);
        notifyDataSetChanged();
    }

}
