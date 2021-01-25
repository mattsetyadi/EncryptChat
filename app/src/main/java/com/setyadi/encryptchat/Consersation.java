package com.setyadi.encryptchat;

import android.os.Message;

import java.util.ArrayList;

public class Consersation {
    private ArrayList<Message> listMessageData;
    public Consersation(){
        listMessageData = new ArrayList<>();
    }

    public ArrayList<Message> getListMessageData() {
        return listMessageData;
    }
}