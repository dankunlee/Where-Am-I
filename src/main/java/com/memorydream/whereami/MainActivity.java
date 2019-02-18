package com.memorydream.whereami;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    String GPSPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    String SMSPermission = Manifest.permission.SEND_SMS;
    private AdView mAdView;
    private ListView listView;
    private ToggleButton onOff;
    private ProgressBar progressBar;
    private Button menuButton, listButton, shareButton, addButton, infoButton, sendingLocationButton;
    private int menuButtonClicked;
    private boolean serviceStatus, firstTimeUsing, gpsON;
    private ArrayList<String> recipientList;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "Type MobileAds Id");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        checkGPS();
        try {
            if ((ActivityCompat.checkSelfPermission(this, SMSPermission) != PERMISSION_GRANTED) ||
                    (ActivityCompat.checkSelfPermission(this, GPSPermission) != PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(this, new String[]{SMSPermission,GPSPermission}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        menuButtonClicked = 0;
        listView = (ListView)findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listButton = (Button) findViewById(R.id.listButton);
        shareButton = (Button) findViewById(R.id.shareButton);
        addButton = (Button) findViewById(R.id.addButton);
        infoButton = (Button) findViewById(R.id.infoButton);
        menuButton = (Button) findViewById(R.id.menuButton);
        onOff = (ToggleButton) findViewById(R.id.toggleButton);
        sendingLocationButton = (Button) findViewById(R.id.sendLocationButton);

        final Animation blinkAni = AnimationUtils.loadAnimation(this,R.anim.menubutton);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedpreferences.getString("recipientList","[]" );
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        recipientList = gson.fromJson(json, type);
        firstTimeUsing = sharedpreferences.getBoolean("firstTimeUsing", true);
        if (firstTimeUsing) {
            infoFunction();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean("firstTimeUsing",false);
            editor.commit();
        }

        serviceStatus = sharedpreferences.getBoolean("serviceStatus",false);
        if (serviceStatus) {
            progressBar.setVisibility(View.VISIBLE);
            sendingLocationButton.setVisibility(View.VISIBLE);
//            startNotification();
        } else progressBar.setVisibility(View.INVISIBLE);
        onOff.setChecked(serviceStatus);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoFunction();
                closeButtons();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButton.startAnimation(blinkAni);
                shareFunction();
                closeButtons();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuButton.startAnimation(blinkAni);
                if (menuButtonClicked == 0) {
                    openButtons();
                    if (listView.getVisibility() == View.VISIBLE) hideListView();
                }else if (menuButtonClicked == 1) {
                    closeButtons();
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeButtons();
                ContactPicker(v);
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFunction();
                checkContactListStatus();
                closeButtons();
            }
        });

        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!recipientList.isEmpty()) {
                        sendingLocationButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        startNotification();
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean("serviceStatus", true);
                        editor.commit();
                        if (menuButtonClicked == 1) closeButtons();
                        if (listView.getVisibility() == View.VISIBLE) hideListView();
                    }else {
                        sendingLocationButton.setVisibility(View.INVISIBLE);
                        AlertDialog.Builder dialog2 = new AlertDialog.Builder(MainActivity.this);
                        dialog2.setTitle("Add a Contact First");
                        dialog2.setMessage("There is no contact selected.\nAdd a contact to enable this service.");
                        dialog2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dialog2.show();
                        onOff.setChecked(false);
                        if (menuButtonClicked == 1) closeButtons();
                        if (listView.getVisibility() == View.VISIBLE) hideListView();
                    }
                }
                else {
                    progressBar.setVisibility(View.INVISIBLE);
                    sendingLocationButton.setVisibility(View.INVISIBLE);
                    stopNotification();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("serviceStatus",false);
                    editor.commit();
                    if (menuButtonClicked == 1) closeButtons();
                    if (listView.getVisibility() == View.VISIBLE) hideListView();
                }
            }
        });

        sendingLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double[] location = getLocation();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {1, 100, 100,100};
                if (gpsON) {
                    Toast.makeText(MainActivity.this, "Sending Location...", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, SMSPermission) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this, GPSPermission) == PERMISSION_GRANTED) {
                        for (String phoneNumb : recipientList) {
                            sendSMS(phoneNumb, "Emergency!!\nSending my location just in case.\nhttps://maps.google.com/?q=" + location[0] + "," + location[1] + "\n");
                        }
                        vibrator.vibrate(pattern, -1);
                        Toast.makeText(MainActivity.this, "Location Sent!", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(MainActivity.this, "Allow SMS and Location permissions first!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void openButtons(){
        final Animation firstAppear = AnimationUtils.loadAnimation(this,R.anim.firstappear);
        final Animation secondAppear = AnimationUtils.loadAnimation(this,R.anim.secondappear);
        final Animation thirdAppear = AnimationUtils.loadAnimation(this,R.anim.thirdappear);
        final Animation fourthAppear = AnimationUtils.loadAnimation(this,R.anim.fourthappear);

        infoButton.setVisibility(View.VISIBLE);
        infoButton.startAnimation(firstAppear);
        shareButton.setVisibility(View.VISIBLE);
        shareButton.startAnimation(secondAppear);
        listButton.setVisibility(View.VISIBLE);
        listButton.startAnimation(thirdAppear);
        addButton.setVisibility(View.VISIBLE);
        addButton.startAnimation(fourthAppear);

        menuButtonClicked = 1- menuButtonClicked;
    }

    public void closeButtons(){
        final Animation firstDisappear = AnimationUtils.loadAnimation(this,R.anim.firstdisappear);
        final Animation secondDisappear = AnimationUtils.loadAnimation(this,R.anim.seconddisappear);
        final Animation thirdDisappear = AnimationUtils.loadAnimation(this,R.anim.thirddisappear);
        final Animation fourthDisappear = AnimationUtils.loadAnimation(this,R.anim.fourthdisappear);

        addButton.setVisibility(View.INVISIBLE);
        addButton.startAnimation(firstDisappear);
        listButton.setVisibility(View.INVISIBLE);
        listButton.startAnimation(secondDisappear);
        shareButton.setVisibility(View.INVISIBLE);
        shareButton.startAnimation(thirdDisappear);
        infoButton.setVisibility(View.INVISIBLE);
        infoButton.startAnimation(fourthDisappear);

        menuButtonClicked = 1- menuButtonClicked;
    }

    public void shareFunction() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"MUST-HAVE APP for Emergency");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Search \"W.A.I - One Click Location Sender\" in Google Play Store!\n\nhttps://play.google.com/store/apps/details?id=com.memorydream.whereami");
        startActivity(Intent.createChooser(sharingIntent, "Tell Your Friends"));
    }

    public void infoFunction() {
        AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
        dialog1.setTitle("About this app...");
        dialog1.setMessage("This app sends your location in case of emergency. \n\n1. Add people who will receive your location." +
                        "\n\n2. Touch the background to enable/disable the service." +
                "\n\n3. Touch inside of the blue circle to send your location." +
                "\n\n4. You can also add a widget for this app on Home Screen." +
                "\n\n5. Touching the widget will send your location." +
                "\n\n* Location will be sent only when the service is enabled.");
        dialog1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog1.show();
    }

    public void listFunction() {
        final Animation slide = AnimationUtils.loadAnimation(this,R.anim.slide);
        listView.setVisibility(View.VISIBLE);
//        contactMenu.setVisibility(View.VISIBLE);
        listView.startAnimation(slide);
//        contactMenu.startAnimation(slide);
    }

    public void hideListView(){
        final Animation unslide = AnimationUtils.loadAnimation(this,R.anim.unslide);
        listView.setVisibility(View.INVISIBLE);
//        contactMenu.setVisibility(View.INVISIBLE);
        listView.startAnimation(unslide);
//        contactMenu.startAnimation(unslide);
    }

    public void startNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setContentTitle("Service Enabled...");
        mBuilder.setContentText("Click to disable.");
        mBuilder.setVibrate(new long[] {100, 200, 300});
        mBuilder.setOngoing(true);
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, mBuilder.build());
        Toast.makeText(this, "Service Enabled", Toast.LENGTH_SHORT).show();
    }

    public void stopNotification() {
        Toast.makeText(this, "Service Disabled", Toast.LENGTH_SHORT).show();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
    }

    public void ContactPicker(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
//                    closeButtons(); close the buttons too slow
                    contactPicked(data);
                    break;
            }
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor;
        int  phoneIndex, nameIndex;
        String phoneNumb, name;

        try {
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNumb = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);

            Contact contact = new Contact(System.currentTimeMillis()) ;
            contact.setName(name);
            contact.setPhoneNumb(phoneNumb);

            if (!recipientList.contains(phoneNumb)) {
                if (Utilities.saveContact(this,contact)) {
                    recipientList.add(phoneNumb);//add contact to recipient list
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(recipientList);
                    editor.putString("recipientList",json);
                    editor.commit();
                    Toast.makeText(this, "New contact added", Toast.LENGTH_SHORT).show(); //saved successfully
                }
                else Toast.makeText(this, "Not enough space on your device", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, "Existing contact selected", Toast.LENGTH_SHORT).show();

        }catch (Exception e) {e.printStackTrace();}
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGPS();
        updateContactList();
    }

    private void updateContactList(){
        ArrayList<Contact> contactList= Utilities.getAllSavedContactss(getApplicationContext());
        if (contactList!=null && contactList.size()!=0) {
            final ContactAdaptor contactAdaptor= new ContactAdaptor(this,R.layout.layout_contact, contactList);
            listView.setAdapter(contactAdaptor);

            SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
                    new SwipeDismissListViewTouchListener.DismissCallbacks() {
                        @Override
                        public boolean canDismiss(int position) {
                            return true;
                        }

                        @Override
                        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                Contact contact = contactAdaptor.getItem(position);
                                Utilities.deleteContact(getApplicationContext(),contact.getFileTime()+Utilities.FILE_EXTENSION);
                                contactAdaptor.remove(contactAdaptor.getItem(position));

                                SharedPreferences.Editor editor = sharedpreferences.edit();
//                                editor.commit();
                                //remove from recipient contact
                                if (!recipientList.isEmpty()) recipientList.remove(contact.getPhoneNumb());
                                checkContactListStatus();
                                if (recipientList.isEmpty()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    onOff.setChecked(false);
                                    stopNotification();
//                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putBoolean("serviceStatus",false);
//                                    editor.commit();
                                }
                                Gson gson = new Gson();
                                String json = gson.toJson(recipientList);
                                editor.putString("recipientList",json);
                                editor.commit();
                            }
//                            contactAdaptor.notifyDataSetChanged();
                        }
                    });
            listView.setOnTouchListener(touchListener);
            listView.setOnScrollListener(touchListener.makeScrollListener());
        }
    }

    private void checkContactListStatus(){
        ArrayList<Contact> contactList= Utilities.getAllSavedContactss(getApplicationContext());
        if (contactList.size() == 0){
            Toast.makeText(this, "No saved contacts", Toast.LENGTH_SHORT).show();
            hideListView();
        }
    }

    private boolean checkGPS(){
        GPS gps = new GPS(this);
        if (!gps.canGetLocation) {
            gps.showSettingsAlert();
            return false;
        }
        return true;
    }

    private double[] getLocation(){
        double[] location = new double[2];
        try {
            GPS gps = new GPS(this);
            // check if GPS enabled
            if (gps.canGetLocation()) {

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                location[0] = latitude;
                location[1] = longitude;
                gpsON = true;
            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
                location[0] = 0;
                location[1] = 0;
                gpsON = false;
            }
//        } else {
        }catch (Exception e) {
            gpsON = false;
        }
        return location;
    }

    private void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            //Vibrate upon success text sent
        } catch (Exception ex) {
        }
    }
}
