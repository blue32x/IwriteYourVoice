/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.recorda.recorda;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An activity to illustrate how to create a file.
 */
public class CreateFileActivity extends BaseDemoActivity {
    private static final String TAG = "oss/CreateFileActivity";
    SharedPreferences prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // create new contents resource
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }

            final DriveContents driveContents = result.getDriveContents();

            // Perform I/O off the UI thread.
            new Thread() {
                @Override
                public void run() {
                    // write content to DriveContents
                    OutputStream outputStream = driveContents.getOutputStream();
                    /*
                    Writer writer = new OutputStreamWriter(outputStream);

                    try {
                        writer.write("Use upload file option");
                        writer.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    */

                    String filePath = prefs.getString("file_path", "");
                    String fileName = prefs.getString("file_name", "");

                    Log.i("oss", "path : " + filePath + "\nname : " + fileName);

                    File file = new File(filePath);
                    int size = (int) file.length();
                    byte[] bytes = new byte[size];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "FileNotFoundException while opening file as bytes", e);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException while opening file as bytes", e);
                    }
                    try {
                        outputStream.write(bytes);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException while writting to the output stream", e);
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            //.setTitle("Upload ON")
                            //.setMimeType("text/plain")
                            .setTitle(fileName)
                            .setMimeType("audio/basic")
                            //.setMimeType("audio/mpeg")
                            //.setMimeType("audio/mpeg3")
                            //.setMimeType("audio/mp3")
                            .setStarred(true).build();

                    // create a file on root folder
                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
            }.start();
        }
    };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }
            showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
        }
    };
}
