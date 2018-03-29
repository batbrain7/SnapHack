package tech.mohitkumar.snaphack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.andremion.counterfab.CounterFab;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by mohitkumar on 29/03/18.
 */

public class CaptureScreenButton extends Service implements ImageReader.OnImageAvailableListener{

    private WindowManager windowManager;
    private View mOverlayView;
    int mWidth;
    CounterFab counterFab;
    boolean activity_background;
    private static final String TAG = "TAG";

    ImageReader imageReader;
    VirtualDisplay virtualDisplay;
    MediaProjection mediaProjection;
    MediaProjectionManager mediaProjectionManager;
    Surface surface;
    int width,height;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent!=null) {
            activity_background = intent.getBooleanExtra("activity_background",false);
        }

        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout,null);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.TOP|Gravity.LEFT;
            params.x = 0;
            params.y = 100;

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.addView(mOverlayView, params);

            DisplayMetrics dp = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dp);
            width = dp.widthPixels;
            height = dp.heightPixels;

            Display display = windowManager.getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);

            counterFab = mOverlayView.findViewById(R.id.fabHead);
        //    counterFab.setCount(1);

            final RelativeLayout layout = mOverlayView.findViewById(R.id.layout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layout.getMeasuredWidth();

                    mWidth = size.x - width;
                }
            });

            counterFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    imageReader = ImageReader.newInstance(width,height, ImageFormat.RGB_565,2);
                    surface = imageReader.getSurface();
                    imageReader.setOnImageAvailableListener((ImageReader.OnImageAvailableListener) getApplicationContext(),null);
                }
            });


        } else {

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setTheme(R.style.AppTheme);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOverlayView != null)
            windowManager.removeView(mOverlayView);
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }


    private void takeScreenShot() {
        Log.d(TAG, "takeScreenShot: " + "taking ss");
        if (surface == null) {
            Log.d(TAG, "takeScreenShot: " + " Surface is null");
            return;
        }
        if (mediaProjection == null) {
            Log.d(TAG, "takeScreenShot: " + "mProjection is null");
           // startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
           //         PERMISSION_CODE);
            mediaProjection = mediaProjectionManager.getMediaProjection(-1, mediaProjectionManager.createScreenCaptureIntent());
            virtualDisplay = createVirtualDisplay();
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
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        // create bitmap
        final Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);
        //Do whatever you want to do with the bitmap now. This is the required screenshot.
        imageReader.close();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + "image_nude"+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
      //  imageView.setImageBitmap(bitmap);
    }
}
