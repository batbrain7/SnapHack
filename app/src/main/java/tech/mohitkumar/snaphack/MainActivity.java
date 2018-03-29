package tech.mohitkumar.snaphack;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {


    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;

    ImageReader imageReader;
    VirtualDisplay virtualDisplay;
    MediaProjection mediaProjection;
    MediaProjectionManager mediaProjectionManager;
    Surface surface;
    private static final int PERMISSION_CODE = 1;
    private static final String TAG = "SnapHack";
    int width,height;
    ImageView imageView;

    Bitmap bitmap;
    MyReciever myReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);
        askForSystemOverlayPermission();
    }

    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available to open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(myReciever);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();


        // To prevent starting the service if the required permission is NOT granted.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //Permission is not available. Display error text.
                Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        myReciever = new MyReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CaptureScreenButton.MY_ACTION);
        registerReceiver(myReciever, intentFilter);
        Intent intent = new Intent(this,CaptureScreenButton.class);
        startService(intent);
    }

    private class MyReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] byteArray = intent.getByteArrayExtra("image");
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageView.setImageBitmap(bitmap);
        }
    }

}
