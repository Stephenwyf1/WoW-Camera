package com.example.wowCamera.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class MyDatabaseHelper(val context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    private val createPattern = "create table Pattern (" +
            " id integer primary key autoincrement," +
            "name text," +
            "imgUrl text"+
            ")"


    private val createFolderImg = "create table Album (" +
            "id integer primary key autoincrement," +
            "imgurl text," +
            "score text,"+
            "fotime text" +
            ")"

    private val createFolder = "create table Folder (" +
            "id integer primary key autoincrement," +
            "foldername text," +
            "ftime text,"+
            "createtime text"+
            ")"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createPattern)
        db.execSQL(createFolder)
        db.execSQL(createFolderImg)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 1) {
            db.execSQL(createPattern)
            db.execSQL(createFolder)
            db.execSQL(createFolderImg)
        }
        if (oldVersion <= 2) {
//            db.execSQL("alter table Book add column category_id integer")
        }
    }

}