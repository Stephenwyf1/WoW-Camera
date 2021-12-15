package com.example.wowCamera.adapters;

import android.graphics.Bitmap;

public class pattern {
    String name;
//    int imageId;
    Bitmap imgBitmap;

    public pattern(String name,Bitmap imgBitmap){
        this.name = name;
        this.imgBitmap = imgBitmap;
    }
    public String getName() {
        return name;
    }
    public void setName(String name1){
        this.name = name1;
    }
    public Bitmap getImgBitmap() {
        return imgBitmap;
    }
}
