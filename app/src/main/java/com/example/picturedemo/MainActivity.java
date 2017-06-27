package com.example.picturedemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_REQUEST_CODE = 2000;
    public static final int GALLERY_REQUEST_CODE = 2001;
    public static final int GALLERY_REQUEST_CODE2 = 2003;

    public static final int CROP_REQUEST_CODE = 2002;
    public static final String TAG = "MainActivity";
    public static final int RECORD_STORAGE_PERMISSION_REQUEST = 2005;
    public static final int WRITE_STORAGE_PERMISSION_REQUEST = 2006;

    public  boolean hasToSettingStorage = false;
    public  boolean hasToSettingWrite = false;

    private ImageView imageView;
    private Button cameraBtn;
    private Button galleryBtn;
    private File imgFile;//拍照
    private File cropFile;
    private File galleryFile;
    private boolean returnData = false;//剪裁图片是否返回bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.iv);
        cameraBtn = (Button) findViewById(R.id.btn_camera);
        galleryBtn = (Button) findViewById(R.id.btn_gallery);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWriteStoragePermission();
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                checkStoragePermission();
                openGallery();//授权读/写权限以后，写/读自动获得
            }
        });
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //has permission, do operation directly
                Log.i(TAG, "user has the record permission already!");
                openGallery();
            } else {
                //do not have permission
                Log.i(TAG, "user do not have this permission!");

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.i(TAG, "we should explain why we need this permission!");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("我们需要" + "读存储" + "权限完成相关操作")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            RECORD_STORAGE_PERMISSION_REQUEST);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();

                } else {

                    // No explanation needed, we can request the permission.
                    Log.i(TAG, "==request the permission==");

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RECORD_STORAGE_PERMISSION_REQUEST);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }


            }
        }else{
            openGallery();
        }
    }
    private void checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //has permission, do operation directly
                Log.i(TAG, "user has the write permission already!");
                startPhoto();
            } else {
                //do not have permission
                Log.i(TAG, "user do not have write permission!");

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.i(TAG, "we should explain why we need this permission!");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("我们需要" + "写存储" + "权限完成相关操作")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            WRITE_STORAGE_PERMISSION_REQUEST);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();

                } else {

                    // No explanation needed, we can request the permission.
                    Log.i(TAG, "==request the permission==");

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_STORAGE_PERMISSION_REQUEST);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }


            }
        }else{
            startPhoto();
        }
    }

    private void startPhoto() {
        // 指定调用相机拍照后照片的储存路径
        File file = getWorkspaceImage();
        if (null != file) {
            imgFile = new File(file.getAbsolutePath(), createPhotoNameByTime());

            Uri imgUri = null;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imgUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", imgFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                imgUri = Uri.fromFile(imgFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        File file = getWorkspaceImage();
//        if (null != file) {
//            galleryFile = new File(getWorkspaceImage(), "IMAGE_GALLERY_NAME.jpg");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Uri uriForFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", galleryFile);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            startActivityForResult(intent, GALLERY_REQUEST_CODE);
//        } else {
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
//        }
//        }
    }

    private void startPhotoZoom(Uri inputUri) {
        if (inputUri == null) {
            Log.e("error", "The uri is not exist.");
            return;
        }
        File file = getWorkspaceImage();
        if (null != file) {
            cropFile = new File(file, "CROP_FILE_NAME.jpg");
            Uri outputUri = Uri.fromFile(cropFile);
            Intent intent = new Intent("com.android.camera.action.CROP");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setDataAndType(inputUri, "image/*");
                if (!returnData) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    String url = GetImagePath.getPath(this, inputUri);//这个方法是处理4.4以上图片返回的Uri对象不同的处理方法
                    intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
                } else {
                    intent.setDataAndType(inputUri, "image/*");
                }
                if (!returnData) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                }

            }
            //设置裁剪
            intent.putExtra("crop", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 250);
            intent.putExtra("outputY", 250);
            intent.putExtra("return-data", returnData);
            intent.putExtra("noFaceDetection", false);//去除人脸识别，否则和剪裁框重叠
            intent.putExtra("outpugFormat", "JPEG");
            startActivityForResult(intent, CROP_REQUEST_CODE);
        }
    }

    private String getAppExternalPath() {
        Log.d("Path", this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        return this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }

    private String getExternalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    public File getWorkspace() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            File file = Environment.getExternalStorageDirectory();
            File workspace = new File(file, "picturedemo/");
            boolean success = true;
            if (!workspace.exists()) {
                success = workspace.mkdir();
            }
            if (success) {
                // Do something on success
                return workspace;
            } else {
                // Do something else on failure
                return null;
            }
        }
            return null;


    }
    public File getWorkspaceImage() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File workspace = getWorkspace();
            if(null != workspace) {
                File imageWorkspace = new File(workspace, "image/");
                boolean success = true;
                if (!imageWorkspace.exists()) {
                    success = imageWorkspace.mkdirs();
                }
                if (success) {
                    // Do something on success
                    return imageWorkspace;
                } else {
                    // Do something else on failure
                    return null;
                }
            }

        }
        return null;

    }

    /**
     * 根据时间生成拍照图片的名称
     *
     * @return
     */
    private String createPhotoNameByTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault()).format(Calendar.getInstance().getTime()) + ".jpg";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_STORAGE_PERMISSION_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Log.i(TAG, "user granted the READ_EXTERNAL_STORAGE permission!");
                openGallery();

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Log.i(TAG, "user denied the READ_EXTERNAL_STORAGE permission!");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) && !hasToSettingStorage) {

                    openSettingActivity(this,
                            "我们需要的" + "读存储" + "权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！",
                            RECORD_STORAGE_PERMISSION_REQUEST);
                }
            }
        }else if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Log.i(TAG, "user granted the WRITE_EXTERNAL_STORAGE permission!");
                startPhoto();

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Log.i(TAG, "user denied the WRITE_EXTERNAL_STORAGE permission!");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasToSettingWrite) {

                    openSettingActivity(this,
                            "我们需要的" + "写存储" + "权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！",
                            WRITE_STORAGE_PERMISSION_REQUEST);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RECORD_STORAGE_PERMISSION_REQUEST){
            hasToSettingStorage = true;
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //has permission, do operation directly
                Log.i(TAG, "user has the record permission already!");
                openGallery();

            }
        }else if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST){
            hasToSettingWrite = true;
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //has permission, do operation directly
                Log.i(TAG, "user has the write permission already!");
                startPhoto();

            }
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    Uri imgUri = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        imgUri = FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".provider", imgFile);

                    } else {
                        imgUri = Uri.fromFile(imgFile);
                    }
                    startPhotoZoom(imgUri);
                    break;
                case CROP_REQUEST_CODE:
                    if (data != null) {
                        Bitmap bitmap = null;
                        if(returnData){
                            Bundle extras = data.getExtras();
                            if (extras != null) {
                                bitmap = extras.getParcelable("data");
                                if (bitmap == null) {
                                    Log.e(TAG,"图片是空的");
                                    return;
                                }
                            }
                        }
                        else {
                            Uri cropUri = FileProvider.getUriForFile(this,
                                    BuildConfig.APPLICATION_ID + ".provider", cropFile);
                            try {
                                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        //Bitmap bitmap = data.getParcelableExtra("data");
                        imageView.setImageBitmap(bitmap);
                    }
                    break;
                case GALLERY_REQUEST_CODE2:
                    if (data != null) {
                        File imgFile = new File(GetImagePath.getPath(this, data.getData()));
                        Uri dataUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", imgFile);
                        startPhotoZoom(dataUri);
                    } else {
                        Log.e("error", "gallery failed");

                    }
                    break;
                case GALLERY_REQUEST_CODE:

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        String picturePath = GetImagePath.getPath(this, data.getData());
                        uri = FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                new File(picturePath));
                    } else {
                        uri = data.getData();
                    }
                    startPhotoZoom(uri);

                    break;
            }
        }
    }
    protected void openSettingActivity(final Activity activity, String message, final int code) {

        showMessageOKCancel(activity, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent,code);
            }
        });
    }
    protected  void showMessageOKCancel(final Activity context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setTitle("权限申请失败")
                .setMessage(message)
                .setPositiveButton("好，去设置", okListener)
                .setNegativeButton("取消", null)
                .create()
                .show();

    }
}
