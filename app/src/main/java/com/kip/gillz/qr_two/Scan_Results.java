package com.kip.gillz.qr_two;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Scan_Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan__results);

        // todo: get value from previouas Avtivity
        String Act = getIntent().getStringExtra("actual");
        String Dec = getIntent().getStringExtra("decode");

        ((TextView) findViewById(R.id.act)).setText(Act);
        ((TextView)findViewById(R.id.dec)).setText(Dec);

    }
}
