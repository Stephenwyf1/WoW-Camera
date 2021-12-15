package com.example.wowCamera.adapters;

public class ConfigStyle {

    private int id;
    private String name;
    private String config;

    public ConfigStyle(String name, String config,int id){
        this.config = config;
        this.name = name;
        this.id = id ;
    }

    public String getConfig() {
        return config;
    }
    public int getId(){return id; }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
