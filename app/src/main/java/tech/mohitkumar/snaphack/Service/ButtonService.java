package tech.mohitkumar.snaphack.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by mohitkumar on 12/02/18.
 */

public class ButtonService extends Service {


    int volumePrev = 0;
    private static final String TAG = "SERVICEBUTTON";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {

                int volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE",0);

                if (volumePrev  < volume) {
                    Log.i(TAG, "You have pressed volume up button");
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }
}
