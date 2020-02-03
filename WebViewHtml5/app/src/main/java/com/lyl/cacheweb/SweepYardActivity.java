package com.lyl.cacheweb;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class SweepYardActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener {
    private CaptureManager captureManager;
    private DecoratedBarcodeView dbvCustom;
    private TextView tvBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sweep_yard);
        initView();
        dbvCustom.setTorchListener(this);
        //重要代码，初始化捕获
        captureManager = new CaptureManager(this, dbvCustom);
        captureManager.initializeFromIntent(getIntent(), savedInstanceState);
        captureManager.decode();


    }

    private void initView() {
        dbvCustom = findViewById(R.id.dbv_custom);
        tvBack = findViewById(R.id.tv_back);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onTorchOn() {
        //闪光灯开
    }

    @Override
    public void onTorchOff() {
        //闪光灯关
    }

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return dbvCustom.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


}
