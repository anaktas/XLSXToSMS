package com.xlsxtosms.zyxt.xlsxtosms;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SendingSMSService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.xlsxtosms.zyxt.xlsxtosms.action.FOO";
    private static final String ACTION_BAZ = "com.xlsxtosms.zyxt.xlsxtosms.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.xlsxtosms.zyxt.xlsxtosms.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.xlsxtosms.zyxt.xlsxtosms.extra.PARAM2";

    public static final String PATH_TO_FILE = "";

    private BroadcastReceiver sendingBroadcastReceiver;
    private BroadcastReceiver deliveringBroadcastReceiver;

    public SendingSMSService() {
        super("SendingSMSService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SendingSMSService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SendingSMSService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }

            String path = intent.getStringExtra(PATH_TO_FILE);
            showToast("Ανάγνωση αρχείου...");
            readCSVFile(path);
        }
    }

    private void readCSVFile(String path) {

        File file = new File(path);
        try {
            FileInputStream fileIS = new FileInputStream(file);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileIS));

                String line;
                int count = 0;
                Handler handler = new Handler(Looper.getMainLooper());
                while ((line = reader.readLine()) != null) {
                    if (count != 0) {
                        try {
                            String[] RowData = line.split(";");
                            String message = "Ποιοτικός Έλεγχος ΕΛΟΓΑΚ " + RowData[0] + "\n";
                            message += "Λίπος: " + RowData[1] + "%, Πρωτεϊνες: " + RowData[2] + "%, Ο.Μ.Χ.: " + RowData[3] + " (x1000), Νοθείες: " + RowData[4];
                            String receiver = RowData[6];
                            //sendSMS(receiver, message);
                            //SystemClock.sleep(2000);
                            final String finalReceiver = receiver;
                            final String finalMessage = message;
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    sendSMS(finalReceiver, finalMessage);
                                }
                            }, 1000 * count);
                        } catch (IndexOutOfBoundsException e) {
                            showToast("Εσφαλμένη δομή αρχείου!");
                            e.printStackTrace();
                        }
                    }
                    count++;
                }

                showNotification("Εστάλησαν " + count + " SMS");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendSMS(String receiver, String smsText) {
        final String finalReceiver = receiver;
        try {
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

            sendingBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            showToast("Αποστολη στο: " + finalReceiver);

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            showToast("Γενικό σφάλμα αποστολής");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            showToast("Η υπηρεσία ασποστολής SMS δεν είναι διαθέσιμη");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            showToast("Null PDU");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            showToast("Radio off");
                            break;
                    }

                    unregisterReceiver(sendingBroadcastReceiver);
                }
            };

            deliveringBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            showToast("Απεσταλη στο: " + finalReceiver);
                            break;
                        case Activity.RESULT_CANCELED:
                            showToast("Αποτυχια αποστολης στο: " + finalReceiver);
                            break;
                    }

                    unregisterReceiver(deliveringBroadcastReceiver);
                }
            };

            registerReceiver(deliveringBroadcastReceiver, new IntentFilter(DELIVERED));
            registerReceiver(sendingBroadcastReceiver , new IntentFilter(SENT));

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(receiver, null, smsText, sentPI, deliveredPI);
            //smsManager.sendTextMessage(receiver, null, smsText, null, null);
            //showToast("Αποστολή στο: " + receiver);
        } catch (Exception e) {
            showToast(e.getMessage());
        }
//        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(receiver, null, smsText, null, null);
//        showToast("Αποστολή στο: " + receiver);
    }

    private void showToast(String text) {
        final String showedText = text;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), showedText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNotification(String notificationMessage) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("CSVToSMS")
                        .setContentText(notificationMessage);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(16041987, mBuilder.build());
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
