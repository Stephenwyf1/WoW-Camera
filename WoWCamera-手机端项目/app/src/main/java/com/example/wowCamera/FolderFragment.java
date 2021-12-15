package com.example.wowCamera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.wowCamera.adapters.Folder;
import com.example.wowCamera.adapters.FolderAdapter;
import com.example.wowCamera.utils.MyDatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;


public class FolderFragment extends Fragment {
    private MyDatabaseHelper dbHelper;
    int SQL_VERSION = 3;

    RecyclerView RecyclerviewFolder;

    ArrayList<Folder> folderArrayList = new ArrayList<Folder>();
    FolderAdapter folderAdapter=new FolderAdapter(getActivity(),folderArrayList);;


    public FolderFragment() {

    }


    public static FolderFragment newInstance(String param1, String param2) {
        FolderFragment fragment = new FolderFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view = inflater.inflate(R.layout.fragment_folder, container, false);


        initFolderSql();

//        if (fps!=null){
//            fpss = new ArrayList<>(fps);
//        }
        RecyclerviewFolder = view.findViewById(R.id.RecyclerviewFolder);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        RecyclerviewFolder.setLayoutManager(layoutManager);

        initView();

        CardView folder_recycler_view_all = view.findViewById(R.id.folder_recycler_view_all);
        folder_recycler_view_all.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("qxysql", "folder_recycler_view_all——————onClick: ");
                folderAdapter.cancle_select();


            }

        });
        folderAdapter.setOnItemClickListener(new FolderAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(),Album2Activity.class);
                String ftime = getftime(position);
                intent.putExtra("ftime",ftime);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                folder_edit_popupWindow(position);
                folderAdapter.setSelectVisible(position);
            }

        });



        return view;
    }
    public void folder_edit_popupWindow(int position){
        PopupWindow popupWindow;
        View pview = LayoutInflater.from(getActivity()).inflate(R.layout.folder_popup_window,null);
        popupWindow = new PopupWindow(pview, ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setContentView(pview);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View rootview = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder, null);
        popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
        LinearLayout folder_delete = pview.findViewById(R.id.folder_delete);
        LinearLayout folder_rename = pview.findViewById(R.id.folder_rename);

        folder_delete.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                delOneFolder(position);
                folderAdapter.setSelectVisible(position);
//                folderAdapter.notifyDataSetChanged();
                popupWindow.dismiss();//让PopupWindow消失
            }
        });

        folder_rename.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                folder_remane_popupWindow(position);
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                folderAdapter.cancle_select();
            }
        });

    }
    public void folder_remane_popupWindow(int position){
        PopupWindow popupWindow;
        View pview = LayoutInflater.from(getActivity()).inflate(R.layout.popup_window,null);
        popupWindow = new PopupWindow(pview, ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setContentView(pview);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//这里给它设置了弹出的时间

        TextView rename_title = pview.findViewById(R.id.rename_title);
        rename_title.setText("编辑");

        final EditText editText = pview.findViewById(R.id.editText);
        editText.requestFocus();
        editText.setText(folderArrayList.get(position).getName());
        editText.selectAll();
        Button confirm = pview.findViewById(R.id.confirm);
        Button cancel = pview.findViewById(R.id.cancle);

        confirm.setPadding(10,0,10,0);
        cancel.setPadding(10,0,10,0);


        //显示PopupWindow
        View rootview = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder, null);
        popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderAdapter.cancle_select();
                popupWindow.dismiss();//让PopupWindow消失

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputString = editText.getText().toString();
                updateFolder(getftime(position),inputString);
                folderArrayList.get(position).setName(inputString);
                popupWindow.dismiss();//让PopupWindow消失
//
                AlbumActivity albumActivity = (AlbumActivity)getActivity();
                albumActivity.initFragment(1);


            }
        });
    }

    public void initView(){
        folderAdapter = new FolderAdapter(getActivity(), folderArrayList);
//        folderAdapters.add(folderAdapter);
        RecyclerviewFolder.setAdapter(folderAdapter);
    }

    public void initFolderSql(){
        dbHelper = new MyDatabaseHelper(getActivity(), "wowCamera.db",  SQL_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Folder", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                String foldername = cursor.getString(cursor.getColumnIndex("foldername"));
                String fid = cursor.getString(cursor.getColumnIndex("ftime"));
                String createtime = cursor.getString(cursor.getColumnIndex("createtime"));
                Folder folder1 = new Folder(foldername,R.drawable.file,createtime);
                folderArrayList.add(folder1);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    public String getftime(int position){
        String ftime = "";
        dbHelper = new MyDatabaseHelper(getActivity(), "wowCamera.db",  SQL_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Folder", null, null, null, null, null, null,position+",1");
        cursor.moveToFirst();
        ftime = cursor.getString(cursor.getColumnIndex("ftime"));
        return ftime;
    }
    public void updateFolder(String ftime,String newName){
        dbHelper = new MyDatabaseHelper(getActivity(), "wowCamera.db",  SQL_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update Folder set foldername =? where ftime = ?",new String[]{newName,ftime});

    }
    public void delOneFolder(int position){
        dbHelper = new MyDatabaseHelper(getActivity(), "wowCamera.db",  3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Folder", null, null, null, null, null, null, position +",1");
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                String id = cursor.getString(cursor.getColumnIndex("id"));
                db.execSQL("delete from Folder where id = ?",new String []{id});
            } while (cursor.moveToNext());
        }
        cursor.close();
        AlbumActivity albumActivity = (AlbumActivity)getActivity();
        albumActivity.initFragment(1);
    }


}