package com.example.notes;

public enum Importance {
    NONE("None"),
    LOW("Trivial"),
    MEDIUM("Important"),
    HIGH("Very Important");

    private String friendlyName;

    private Importance(String friendlyName){
        this.friendlyName = friendlyName;
    }

    @Override public String toString(){
        return friendlyName;
    }
}
