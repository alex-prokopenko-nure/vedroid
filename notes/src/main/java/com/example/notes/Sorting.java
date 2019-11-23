package com.example.notes;

public enum Sorting {
    NONE("None"),
    DATE("By Date"),
    ALPHA("Alphabetical");

    private String friendlyName;

    private Sorting(String friendlyName){
        this.friendlyName = friendlyName;
    }

    @Override public String toString(){
        return friendlyName;
    }
}
