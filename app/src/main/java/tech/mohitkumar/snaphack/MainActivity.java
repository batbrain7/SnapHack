package tech.mohitkumar.snaphack;

import android.content.Context;
import android.content.Intent;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.nio.ByteBuffer;

import tech.mohitkumar.snaphack.Service.ScreenService;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics dp = new DisplayMetrics();
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        imageReader = ImageReader.newInstance(width,height, ImageFormat.RGB_565,2);
        surface = imageReader.getSurface();

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        button = findViewById(R.id.but_ton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenShot();
            }
        });

    }

    private void takeScreenShot() {
        if (surface == null) {
            return;
        }
        if (mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                    PERMISSION_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("ScreenSharingDemo",
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
        virtualDisplay = createVirtualDisplay();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
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
    }
}
