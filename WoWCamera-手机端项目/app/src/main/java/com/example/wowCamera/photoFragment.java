package com.example.wowCamera;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.wowCamera.adapters.ImgAdapter;
import com.example.wowCamera.utils.MyDatabaseHelper;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class photoFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    RecyclerView RecyclerviewPhoto;
    ArrayList<String> imgUrl = new ArrayList<>();
    ImgAdapter imgAdapter;
    ArrayList<ImgAdapter> imgAdapters = new ArrayList<ImgAdapter>();
    ArrayList<String> scoreList = new ArrayList<>();
    String ALBUM_ID = "albumid";
    boolean deleteState = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public photoFragment() {
        // Required empty public constructor
    }


    public static photoFragment newInstance(String param1, String param2) {
        photoFragment fragment = new photoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        initAlbumSql();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_photo, container, false);
//        initAlbumSql();
        RecyclerviewPhoto = view.findViewById(R.id.RecyclerviewPhoto);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        RecyclerviewPhoto.setLayoutManager(layoutManager);

        imgAdapter = new ImgAdapter(imgUrl,getActivity());
        imgAdapter.setScoreList(scoreList);

        imgAdapters.add(imgAdapter);
        RecyclerviewPhoto.setAdapter(imgAdapter);

        CardView recycler_all = view.findViewById(R.id.recycler_view_all);
        recycler_all.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                imgAdapter.setDeleteState(false);
                deleteState = false;
            }

        });

        imgAdapter.setOnItemClickListener(new ImgAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(deleteState){
                    imgAdapter.delOnePhoto(position,ALBUM_ID);
                    AlbumActivity albumActivity = (AlbumActivity)getActivity();
                    albumActivity.delImgUrlbyPosition(position);
                }else{
                    Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                    intent1.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                    //        intent1.setType("image/*");
                    intent1.setClass(getActivity(), ProcessActivity.class);
                    intent1.putExtra("uri",imgUrl.get(position));
                    intent1.putExtra("score",scoreList.get(position));
                    intent1.putExtra("imgId",getImgId(position,ALBUM_ID));
                    startActivity(intent1);
                }


            }

            @Override
            public void onItemLongClick(View view, int position) {
//                delOnePhoto(position);
                deleteState = true;
                imgAdapter.setDeleteState(true);
                imgAdapter.setFotime(ALBUM_ID);
            }
        });

        return view;
    }

    public ImgAdapter getImgAdapter() {
        return imgAdapter;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }
    public void initAlbumSql(){
        dbHelper = new MyDatabaseHelper(getActivity(), "wowCamera.db",  3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        imgUrl = new ArrayList<>();
        scoreList = new ArrayList<>();
        Cursor cursor = db.query("Album", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                String photoid = cursor.getString(cursor.getColumnIndex("id"));
                String imgurl = cursor.getString(cursor.getColumnIndex("imgurl"));
                String score = cursor.getString(cursor.getColumnIndex("score"));
                String fid = cursor.getString(cursor.getColumnIndex("fotime"));
                if(fid.equals(ALBUM_ID)){
                    imgUrl.add(imgurl);
                    scoreList.add(score);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public String getImgId(int position,String fotime){
        String id = "";
        dbHelper = new MyDatabaseHelper(getActivity(), "wowCamera.db",  3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] selectionArgs = new String[]{fotime};
        String table = "Album";
        String limit = position +",1";
        Cursor cursor = db.query(table,null,"fotime = ?",selectionArgs,null,null,null,limit);
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                id = cursor.getString(cursor.getColumnIndex("id"));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return id;
    }


}