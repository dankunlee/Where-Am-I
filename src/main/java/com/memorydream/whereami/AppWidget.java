package com.memorydream.whereami;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {
    private static final String ACTION = "ACTION"; //broadcast ID
    private static final String MyPREFERENCES = "MyPrefs";
    private boolean gpsON;
    String GPSPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    String SMSPermission = Manifest.permission.SEND_SMS;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        //Creating intent for broadcast
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION.equals(intent.getAction())){ //&& (MainService_notUsed.serviceOn == 1) ) { //service is enabled and when widget is clicked (recieve broadcast upon click)
            if (getServiceStatus(context)) {
                //vibration
                ArrayList<String> recipientList = getRecipientList(context);
                double[] location = getLocation(context);
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {1, 100, 100,100};
                if (gpsON) {
                    Toast.makeText(context, "Sending Location...", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(context, SMSPermission) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(context, GPSPermission) == PERMISSION_GRANTED) {
                        for (String phoneNumb : recipientList) {
                            sendSMS(phoneNumb, "Emergency!!\nSending my location just in case.\nhttps://maps.google.com/?q=" + location[0] + "," + location[1] + "\n");
                        }
                        vibrator.vibrate(pattern, -1);
                        Toast.makeText(context, "Location Sent!", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(context, "Allow SMS and Location permissions first!", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(context, "Service is not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public boolean getServiceStatus(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        boolean serviceStatus = sharedpreferences.getBoolean("serviceStatus",false);

        return serviceStatus;
    }

    public ArrayList<String>  getRecipientList(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedpreferences.getString("recipientList","[]" );
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> recipientList = gson.fromJson(json, type);

        return recipientList;
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            //Vibrate upon success text sent
        } catch (Exception ex) {
        }
    }

    public double[] getLocation(Context context){
        final Context innerContext = context;
        double[] location = new double[2];
        try {
            GPS gps = new GPS(context);
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
}

