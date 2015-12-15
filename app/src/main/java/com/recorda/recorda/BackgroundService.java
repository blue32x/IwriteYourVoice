package com.recorda.recorda;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by choiseunghyuk on 2015-12-13.
 */
public class BackgroundService extends Service {
    //Variables
    private static MediaRecorder mediaRecorder;
    boolean isRecording = false;
    private static String audioFilePath;
    private Handler mHandler;
    private String mFilePath, mFileName = null;
    private GoogleApiClient client;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    //Methods
    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
        editor = prefs.edit();

        //Register hardware key pattern using SCREEN ON/OFF
        ScreenOnReceiver screenOnReceiver = new ScreenOnReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(screenOnReceiver, filter);

        //Thread for REC STOP
        mHandler=new Handler();
        StopCheckThread mStopCheckThread = new  StopCheckThread();
        mStopCheckThread.start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // TODO Auto-generated method stub

        return super.onStartCommand(intent, flags, startId);


    }
    @Override
    public IBinder onBind(Intent intent) {

        // TODO Auto-generated method stub

        return null;

    }
    @Override
    public void onDestroy() {

        // TODO Auto-generated method stub
        stopRecording();
        super.onDestroy();

    }
    void stopRecording() {
        if(isRecording) {
            mediaRecorder.stop();
            isRecording = false;

            boolean isDriveOn = prefs.getBoolean("drive_on_flg",false);
            if(isDriveOn){
                //Upload file into google drive
                Intent intent = new Intent(getBaseContext(), GoogleDriveCreateFileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        else{
            Log.i("oss", "It is not recording, now");
        }
    }
    void createFilefunction() {
        if(isRecording) {
            Log.i("oss", "It is already recording, now");
            return;
        }

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyMMdd_HHmm");
        String strNow = sdfNow.format(date);

        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
        File file = new File(dirPath);
        if (!file.exists()) file.mkdirs();


        audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record" + "/" + strNow + ".mp3";

        editor.putString("file_path", audioFilePath);
        editor.putString("file_name", strNow + ".mp3");
        editor.commit();

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        isRecording = true;
        // startchronometer();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // Inner Class for Screen On/Off
    class ScreenOnReceiver extends BroadcastReceiver {
        int count = 0;
        boolean flg = false;
        long onTime = 0;
        long offTime = 0;

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                onTime = System.currentTimeMillis();
                if ((onTime - offTime) > 3000) {
                    count = 0;
                    Log.i("oss", "count initialize, time : " + (onTime - offTime));
                }
                if (flg == false) {
                    count++;
                    Log.i("oss", "trigger");
                }
                if (count == 3) {
                    count = 0;
                    createFilefunction();
                    Log.i("oss", "record start");
                }
                flg = true;
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                offTime = System.currentTimeMillis();
                if ((offTime - onTime) > 3000) {
                    count = 0;
                    Log.i("oss", "count initialize, time : " + (offTime - onTime));
                }
                if (flg == true) {
                    count++;
                    Log.i("oss", "trigger");
                }
                if (count == 3) {
                    count = 0;
                    createFilefunction();
                    Log.i("oss", "record start");
                }
                flg = false;
            }
        }
    }

    // Inner Class for REC STOP Thread
    class StopCheckThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean stop_flg = prefs.getBoolean("stop_flg", false);

                        if(stop_flg==true)
                        {
                            editor.putBoolean("stop_flg", false);
                            editor.commit();

                            stopRecording();
                            Log.i("oss", "record stop");
                        }
                    }
                });
            }
        }
    }
}