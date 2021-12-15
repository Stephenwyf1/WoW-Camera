package com.example.wowCamera.adapters;

public class Folder {
    String name;
    int imageId;
    String createtime;
    boolean selected = false;

    public Folder(String name,int imageId,String createtime){
        this.name = name;
        this.imageId = imageId;
        this.createtime = createtime;
    }
    public String getName() {
        return name;
    }
    public void setName(String name1){
        this.name = name1;
    }
    public void setImageId(int imageId1){
        this.imageId = imageId1;
    }
    public int getImageId(){
        return imageId;
    }
    public String getCreateTime(){return createtime;}
    public void setSelected(boolean select){
        this.selected = select;
    }
    public boolean getSelected() {
        return selected;
    }
}
