package in.omerjerk.droidcv.samples;

import android.content.Intent;
import android.view.View;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import org.opencv.core.Scalar;

public class SampleActivity extends Activity {

    private static final String TAG = "SampleActivity";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private static final boolean DEBUG = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);
        startService(new Intent(this, SampleService.class));
        startService(new Intent(this, ResultDisplayService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void stopService (View v) {
        stopService(new Intent(this, SampleService.class));
    }

    public void onNewFrame(CvCameraViewFrame input){

        /*Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;*/
    }
}
