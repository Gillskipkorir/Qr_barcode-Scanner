package com.kip.gillz.qr_two;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.Callback;

public class MainActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private static final int PERMISSION_REQUEST_CODE = 200;
    public static final String ROOT_URL = "Your Url";
    String Cod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (checkPermission()) {
            CodeScannerView scannerView = findViewById(R.id.scanner_view);
            mCodeScanner = new CodeScanner(this, scannerView);
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                            tg.startTone(ToneGenerator.TONE_PROP_BEEP);


                            Vibrator v = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(100);// Vibrate for 1 seconds


                            String encode = Base64.encodeToString(result.getText().getBytes(), Base64.DEFAULT);
                            //endoded
                            byte[] data = Base64.decode(encode, Base64.DEFAULT);
                            String text = new String(data, StandardCharsets.UTF_8);
                            //decoded

                           Cod = result.getText();
                           submitcode();

                        }
                    });
                }
            });
            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCodeScanner.startPreview();
                }
            });

        } else {
            requestPermission();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }
    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();



    }
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void submitcode(){

        //Here we will handle the http request to insert user to mysql db
        //Creating a RestAdapter
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(ROOT_URL) //Setting the Root URL
                .build();

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMessage("Please Wait....");
        progressDoalog.setTitle("Verifying Code");
        progressDoalog.setCancelable(false);
        progressDoalog.setIndeterminate(true);
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();
        SubmitAPI api = adapter.create(SubmitAPI.class);
        api.insertUser(
                Cod,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {

                        progressDoalog.dismiss();

                        BufferedReader reader = null;
                        String output = "";
                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (output.equals("Code Accepted"))
                        {

                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("SUCCESS")
                                    .setContentText(output)
                                    .setConfirmText("Continue")
                                    .show();                        }
                        else {
                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Failed")
                                    .setContentText(output)
                                    .hideConfirmButton()
                                    .setCancelText("Continue")
                                    .show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDoalog.dismiss();
                        Toast.makeText(MainActivity.this, error.toString(),Toast.LENGTH_LONG).show();

                    }
                }
        );
    }


}


