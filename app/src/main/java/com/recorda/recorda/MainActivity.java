package com.recorda.recorda;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {
    // Variables
    Intent Service;
    CallbackManager callbackManager;
    LoginButton loginButton;
    Activity act;
    ShareDialog shareDialog;
    private String mFilePath, mFileName = null;
    private GoogleApiClient client;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    //Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
        editor = prefs.edit();

        /* Facebook 관련 설정 */
        //facebook api 초기화 , view를 그리기 전에 해야 한다
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        act=this;
        shareDialog=new ShareDialog(act);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Fb Login Success", Toast.LENGTH_LONG);
                //여기서 부터 공유 게시글 올리는 부분

                //shareDialog = new ShareDialog(act);
               // ShareLinkContent linkContent = new ShareLinkContent.Builder()
               //         .setContentTitle("살려주시와요")
               //         .setContentDescription("하꾜에 갇팀")
               //         .setImageUrl(Uri.parse("http://cfile28.uf.tistory.com/image/2056A53D5139F7A301C2BA"))
               //         .build();
                //shareDialog.show(linkContent);
                //act.finish();
                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(
                            AccessToken oldAccessToken,
                            AccessToken currentAccessToken) {
                        // Set the access token using
                        // currentAccessToken when it's loaded or set.
                    }
                };
                // If the access token is available already assign it.
                accessToken = AccessToken.getCurrentAccessToken();
            }
            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Fb on cancel", Toast.LENGTH_LONG);
            }
            @Override
            public void onError(FacebookException e) {
                Toast.makeText(getApplicationContext(), "Fb Login Error", Toast.LENGTH_LONG);
            }
        });


        /* Google Drive 관련 설정 */
        // Drive switch button
        Switch.OnCheckedChangeListener driveCheckedListener=new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) //스위치 on
                {
                    Log.i("oss","on_drive");
                    editor.putBoolean("drive_on_flg", true);
                    editor.commit();
                    /*
                    Intent intent = new Intent(getBaseContext(), GoogleDriveCreateFileService.class);
                    startActivity(intent);
                    */

                }else{ //스위치 off
                    editor.putBoolean("drive_on_flg",false);
                    editor.commit();
                    Log.i("oss", "off_drive");
                }
            }
        };
        Switch driveSwitch = (Switch)findViewById(R.id.swi_drive_btn);
        driveSwitch.setOnCheckedChangeListener(driveCheckedListener);

        /* Record 관련 설정*/
        // Record switch button
        Switch.OnCheckedChangeListener recCheckedListener=new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) //스위치 on
                {
                    Log.i("oss", "on_service");
                    Service = new Intent(getApplicationContext(), BackgroundService.class);
                    startService(Service);

                }else{ //스위치 off
                    Log.i("oss","off_service");
                    stopService(Service);
                }
            }
        };
        Switch recSwitch = (Switch)findViewById(R.id.swi_service_btn);
        recSwitch.setOnCheckedChangeListener(recCheckedListener);

        // Record stop button
        findViewById(R.id.rec_stop_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rec_stop_btn:
                        editor.putBoolean("stop_flg",true);
                        editor.commit();
                        break;
                }
            }
        });





    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}