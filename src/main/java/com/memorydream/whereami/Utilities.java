package com.memorydream.whereami;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Utilities {

    public static final String FILE_EXTENSION = ".bin";

    public static boolean saveContact(Context context,Contact contact) {
        FileOutputStream fos;
        ObjectOutputStream oos;

        String fileName = String.valueOf(contact.getFileTime()) + FILE_EXTENSION;

        try {
            fos = context.openFileOutput(fileName, context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(contact);
            oos.close();
            fos.close();
        }catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static ArrayList<Contact> getAllSavedContactss(Context context) {
        ArrayList<Contact> contactList = new ArrayList<>();
        ArrayList<String> contactFiles = new ArrayList<>();
        FileInputStream fis;
        ObjectInputStream ois;

        File fileDir = context.getFilesDir();
        for (String file:fileDir.list()) {
            if(file.endsWith(FILE_EXTENSION)) {
                contactFiles.add(file);
            }
        }

        for (int i = 0;i<contactFiles.size();i++){
            try{
                fis = context.openFileInput(contactFiles.get(i));
                ois = new ObjectInputStream(fis);
                contactList.add((Contact) ois.readObject());
                fis.close();
                ois.close();

            }catch (IOException | ClassNotFoundException e) {
                return null;
            }
        }

        return contactList;
    }

    public static Contact getContact(Context context, String fileName){
        File file = new File(context.getFilesDir(), fileName);

        if(file.exists() && !file.isDirectory()) { //check if file actually exist
            FileInputStream fis;
            ObjectInputStream ois;
            try { //load the file
                fis = context.openFileInput(fileName);
                ois = new ObjectInputStream(fis);
                Contact contactItem = (Contact) ois.readObject();
                fis.close();
                ois.close();

                return contactItem;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;}
        }
        else return null;
    }

    public static void deleteContact(Context context, String fileName) {
        File fileDir = context.getFilesDir();
        File file = new File(fileDir, fileName);
        if (file.exists()) {
            file.delete();
//            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
        }
    }
}
