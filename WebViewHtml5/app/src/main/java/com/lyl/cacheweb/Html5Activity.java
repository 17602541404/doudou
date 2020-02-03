package com.lyl.cacheweb;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;

public class Html5Activity extends Activity {

    private String mUrl;

    private FrameLayout mLayout;
    private SeekBar mSeekBar;
    private Html5WebView mWebView;

    private ValueCallback mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSE_RESULTCODE = 3;

    private final static int TAKEPHOTO_RESULTCODE  = 2;
    private static final int PERMISSIONS_REQUEST_CODE_TAKE_PHOTO = 1;

    private String pathTakePhoto;
    private Uri uriTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_web);

        getParameter();

        mLayout = (FrameLayout) findViewById(R.id.web_layout);
        mSeekBar = (SeekBar) findViewById(R.id.web_sbr);

        // 创建 WebView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView = new Html5WebView(getApplicationContext());
        mWebView.setLayoutParams(params);
        mLayout.addView(mWebView);
        mWebView.setWebChromeClient(new Html5WebChromeClient());

        mWebView.addJavascriptInterface(new JSInterFace(),"AndroidMethods");
        mWebView.loadUrl(mUrl);
    }

    // 继承 WebView 里面实现的基类
    class Html5WebChromeClient extends Html5WebView.BaseWebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // 顶部显示网页加载进度
            mSeekBar.setProgress(newProgress);
        }

        public void openFileChooser(ValueCallback uploadMsg){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i,"File Chooser"),REQUEST_SELECT_FILE);
        }

        public void openFileChooser(ValueCallback uploadMsg,String acceptType){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i,"File Browser"),REQUEST_SELECT_FILE);
        }

        public void openFileChooser(ValueCallback uploadMsg,String acceptType,String capture){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i,"File Chooser"),REQUEST_SELECT_FILE);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            uploadMessage = filePathCallback;
            selPic();
            return true;
        }


    }

    /**
     * 弹出对话框，提示拍照或者选择照片文件
     */
    @SuppressWarnings("unused")
    protected final void selPic() {
        if (!checkSDcard()){return;}
        String[] selectPicTypeStr = { "拍照","选择照片" };
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setItems(
                        selectPicTypeStr,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://拍照
                                        chkPrivBeforeTakePhoto();
                                        break;
                                    case 1://选择图片文件
                                        choosePicFile();
                                        break;
                                    default:
                                        break;
                                }

                            }
                        }
                ).setOnCancelListener(
                        new DialogInterface.OnCancelListener() {

                            @SuppressWarnings("unchecked")
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (null != uploadMessage) {
                                    uploadMessage.onReceiveValue(null);
                                    uploadMessage = null;
                                }
                            }
                        }
                ).show();
    }

    /**
     * 检查SD卡是否存在
     */
    public final boolean checkSDcard() {
        boolean flag = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(!flag){Toast.makeText(this, "请插入手机存储卡再使用本功能", Toast.LENGTH_SHORT).show();}
        return flag;
    }

    @SuppressWarnings("unchecked")
    private void chkPrivBeforeTakePhoto(){
        if(
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            if (null != uploadMessage) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            new AlertDialog
                    .Builder(this)
                    .setTitle("提示信息")
                    .setMessage("该功能需要您接受应用对一些关键权限（拍照）的申请，如之前拒绝过，可到手机系统的应用管理授权设置界面再次设置。")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Html5Activity.this, new String[]{
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, PERMISSIONS_REQUEST_CODE_TAKE_PHOTO);
                        }
                    })
                    .show();
        } else {
            chooseTakePhoto();
        }
    }

    private void chooseTakePhoto(){
        pathTakePhoto = System.currentTimeMillis() + ".jpg";
        String CACHE_IMG = Environment.getExternalStorageDirectory()+"/demo/";
        File vFile = new File(CACHE_IMG,pathTakePhoto);
        if (!vFile.exists()) {//必须确保文件夹路径存在，否则拍照后无法完成回调
            File vDirPath = vFile.getParentFile();
            vDirPath.mkdirs();
        } else {
            if (vFile.exists()) {
                vFile.delete();
            }
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        uriTakePhoto = FileProvider.getUriForFile(this,"org.diql.fileprovider", vFile);
        //uriTakePhoto = Uri.fromFile(vFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTakePhoto);
        startActivityForResult(intent, TAKEPHOTO_RESULTCODE);
    }

    /**
     * 选择文件
     */
    private void choosePicFile(){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Html5Activity.this.startActivityForResult(
                Intent.createChooser(i,"文件选择"),
                FILECHOOSE_RESULTCODE
        );
    }


    @Override
    protected void onDestroy() {
        // 销毁 WebView
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();

            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    public void getParameter() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mUrl = bundle.getString("url");
        } else {
            mUrl = "https://wing-li.github.io/";
        }
    }

    //============================= 下面是本 demo  的逻辑代码
    // ======================================================================================

    /**
     * 按目录键，弹出“关闭页面”的选项
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.close:
                Html5Activity.this.finish();
                return true;
            case R.id.copy:
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String url = mWebView.getUrl();
                ClipData clipData = ClipData.newPlainText("test", url);
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getApplicationContext(), "本页网址复制成功", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private long mOldTime;

    /**
     * 点击“返回键”，返回上一层
     * 双击“返回键”，返回到最开始进来时的网页
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*if (System.currentTimeMillis() - mOldTime < 1500) {
                mWebView.clearHistory();
                mWebView.loadUrl(mUrl);
            } else if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                Html5Activity.this.finish();
            }*/
            Html5Activity.this.finish();
            mOldTime = System.currentTimeMillis();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 解决拍照后在相册中找不到的问题
     */
    private void addImageGallery(String path) {
        if (null == path || "".equals(path)) {
            return;
        }
        File file = new File(pathTakePhoto);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSE_RESULTCODE) {//从文件选择器选择照片
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(null == uploadMessage) {return;}
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                uploadMessage = null;
            } else {
                if(null == uploadMessage) {return;}
                Uri result = (data == null || resultCode != RESULT_OK)? null:data.getData();
                Uri[] uris = new Uri[1];
                uris[0] = result;
                uploadMessage.onReceiveValue(uris);
                uploadMessage = null;
            }
        } else if(requestCode == TAKEPHOTO_RESULTCODE){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(null == uploadMessage) {return;}
                if(null == uriTakePhoto) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                    return;
                }
                addImageGallery(pathTakePhoto);
                Uri[] uris = new Uri[1];
                uris[0] = uriTakePhoto;
                uploadMessage.onReceiveValue(uris);
                uploadMessage = null;
                uriTakePhoto = null;
                pathTakePhoto = null;
            } else {
                if(null == uploadMessage) {return;}
                if(null == uriTakePhoto) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                    return;
                }
                Uri[] uris = new Uri[1];
                uris[0] = uriTakePhoto;
                addImageGallery(pathTakePhoto);
                uploadMessage.onReceiveValue(uris);
                uploadMessage = null;
                uriTakePhoto = null;
                pathTakePhoto = null;
            }
        }

        else if(requestCode == REQUEST_SELECT_FILE){
            if(null == mUploadMessage){
                return ;
            }
            Uri result = data ==null || resultCode!=Activity.RESULT_OK ?null:data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                //扫描内容为空
            } else {
                // ScanResult 为 获取到的字符串
                String result = intentResult.getContents();
                mWebView.loadUrl("javascript: ('" + result + "')");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //js调用扫描二维码的接口
    public class JSInterFace {

        @JavascriptInterface
        public void scanQrCodeInApp(){
            goQr();
        }

        private void goQr() {
            new IntentIntegrator(Html5Activity.this)
                    .setOrientationLocked(false)
                    .setBeepEnabled(true)
                    .setPrompt("请将二维码\n条形码\n放于方框内")// 设置提示语
                    .setCaptureActivity(SweepYardActivity.class) // 设置自定义的activity是CustomActivity
                    .initiateScan(); // 初始化扫描
        }
    }

}

