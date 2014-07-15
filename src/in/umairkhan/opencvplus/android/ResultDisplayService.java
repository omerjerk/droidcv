package in.umairkhan.opencvplus.android;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by omerjerk on 15/7/14.
 */
public class ResultDisplayService extends Service {

    TextView detectionCountTextView = null;
    RelativeLayout resultOverlayLayout = null;
    ResultUpdatesReceiver resultUpdatesReceiver = new ResultUpdatesReceiver();
    WindowManager windowManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE");
        registerReceiver(resultUpdatesReceiver, filter);
        createFaceCountFloatingWindow();
    }

    private void createFaceCountFloatingWindow() {
        LayoutInflater li = LayoutInflater.from(this);
        resultOverlayLayout = (RelativeLayout) li.inflate(R.layout.result_layout, null, false);
        detectionCountTextView = (TextView) resultOverlayLayout.findViewById(R.id.result_text_view);
        detectionCountTextView.setText("FACES = 0");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        windowManager.addView(resultOverlayLayout, params);
    }

    private class ResultUpdatesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            detectionCountTextView.setText("FACES = " + CoreDisplayService.FACES_COUNT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(resultUpdatesReceiver);
        windowManager.removeViewImmediate(resultOverlayLayout);
    }
}
