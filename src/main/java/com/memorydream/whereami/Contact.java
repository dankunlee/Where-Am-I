package com.memorydream.whereami;

import java.io.Serializable;

public class Contact implements Serializable {
    private String name, phoneNumb;
    private long fileTime;

    public Contact(long dateTime){
        fileTime = dateTime;
    }

    public void setName(String contactName) {
        name = contactName;
    }

    public void setPhoneNumb(String contactPhoneNumb) {
        phoneNumb = contactPhoneNumb;
    }

    public long getFileTime() {
        return fileTime;
    }
    public String getName(){
        return name;
    }

    public String getPhoneNumb() {
        return phoneNumb;
    }
}
