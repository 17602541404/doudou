package com.lyl.cacheweb;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends Activity {

    private TextView tvSweepYard;

    private TextView pickImage;
    public static final int CAMERA_REQUEST_CODE = 0x01;

    private static final int REQUEST_CODE_PICK_IMAGE = 601;

    private TextView mTxtHost;
    private EditText mEdtUrl;
    private Button mBtnSreach;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        initView();

    }

    private void initView() {
        tvSweepYard = findViewById(R.id.tv_sweep_yard);
        tvSweepYard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goQr();
            }
        });

        /*pickImage = findViewById(R.id.pick_image);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                //扫描内容为空
            } else {
                // ScanResult 为 获取到的字符串
                mUrl = intentResult.getContents();
                sreachUrl();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void goQr() {
        new IntentIntegrator(MainActivity.this)
                .setOrientationLocked(false)
                .setBeepEnabled(true)
                .setPrompt("请将二维码\n条形码\n放于方框内")// 设置提示语
                .setCaptureActivity(SweepYardActivity.class) // 设置自定义的activity是CustomActivity
                .initiateScan(); // 初始化扫描
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
            // 向用户解释为什么需要这个权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(MainActivity.this).setMessage("申请相机权限").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //申请相机权限
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                    }
                }).show();
            } else {
                //申请相机权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        } else {
            // "相机权限已申请"
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // "相机权限已申请"
            } else {
                //用户勾选了不再询问
                //提示用户手动打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "相机权限已被禁止,请在设置中打开", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /*private void initViews() {
        mTxtHost = (TextView) findViewById(R.id.host);
        mEdtUrl = (EditText) findViewById(R.id.edt_url);
        mBtnSreach = (Button) findViewById(R.id.btn_sreach);
    }

    private void initEvents() {
        // 搜索按钮 和 输入法右下角的“搜索” 点击事件是一致的
        mBtnSreach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sreachUrl();
            }
        });
        mEdtUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    sreachUrl();
                }
                return false;
            }
        });

        // 点击前面 “https://” 切换 http
        mTxtHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = mTxtHost.getText().toString().trim();
                if (host.startsWith("https")) {
                    mTxtHost.setText("http://");
                } else {
                    mTxtHost.setText("https://");
                }
            }
        });
    }*/


    private void sreachUrl() {
        /*String edt = mEdtUrl.getText().toString().trim();
        if (edt.startsWith("https") || edt.startsWith("http")) {
            mUrl = edt;
        } else {
            mUrl = mTxtHost.getText().toString() + edt;
        }*/

        Intent intent = new Intent(MainActivity.this, Html5Activity.class);
        if (!TextUtils.isEmpty(mUrl)) {
            Bundle bundle = new Bundle();
            bundle.putString("url", mUrl);
            intent.putExtra("bundle", bundle);
        }
        startActivity(intent);
    }
}
