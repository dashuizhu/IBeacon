package com.zby.ibeacon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.zby.ibeacon.R;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        findViewById(R.id.iv).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LogoActivity.this, DeviceListActivity.class));
                finish();
            }
        }, 1000);

    }
}
