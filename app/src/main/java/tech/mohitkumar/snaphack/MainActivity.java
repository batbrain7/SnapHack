package tech.mohitkumar.snaphack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
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
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements ImageReader.OnImageAvailableListener{

    ImageReader imageReader;
    Intent mserviceIntent;
    VirtualDisplay virtualDisplay;
    MediaProjection mediaProjection;
    MediaProjectionManager mediaProjectionManager;
    Surface surface;
    private static final int PERMISSION_CODE = 1;
    private static final String TAG = "SnapHack";
    int width,height;
    Button button;
    ImageView imageView;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);
        width = dp.widthPixels;
        height = dp.heightPixels;

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {

                    int volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE",0);

                    Log.i(TAG, "volume = " + volume);
                    Toast.makeText(MainActivity.this,"Bob Vagene Captured bitch",Toast.LENGTH_LONG).show();

                    imageReader = ImageReader.newInstance(width,height, ImageFormat.RGB_565,100);
                    surface = imageReader.getSurface();
                    imageReader.setOnImageAvailableListener(MainActivity.this,null);
                    takeScreenShot();

                }
            }
        };

        this.registerReceiver(broadcastReceiver,intentFilter);


        imageView = findViewById(R.id.image_view);

    }

    private void takeScreenShot() {
        Log.d(TAG, "takeScreenShot: " + "taking ss");
        if (surface == null) {
            Log.d(TAG, "takeScreenShot: " + " Surface is null");
            return;
        }
        if (mediaProjection == null) {
            Log.d(TAG, "takeScreenShot: " + "mProjection is null");
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                    PERMISSION_CODE);
            return;
        }
        Log.d(TAG, "takeScreenShot: " + "Projection is not null");
        virtualDisplay = createVirtualDisplay();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("&lt;SnapHack&gt;",
                width, height, 50,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null , null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        Log.d(TAG, "onActivityResult: " + "mProjection done");
        virtualDisplay = createVirtualDisplay();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.d(TAG, "onImageAvailable: " + "In here");
        Image image = reader.acquireLatestImage();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        // create bitmap
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);
        //Do whatever you want to do with the bitmap now. This is the required screenshot.

        imageView.setImageBitmap(bitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d(TAG, "onImageAvailable: " + encoded);
        sendEmail(encoded);
        imageReader.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    public void sendEmail(String subject) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, "kumar.mohit983@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Image");
        intent.putExtra(Intent.EXTRA_TEXT, subject);
        startActivity(Intent.createChooser(intent, "Send Email"));
    }


}
