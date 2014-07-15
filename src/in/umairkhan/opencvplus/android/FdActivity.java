package in.umairkhan.opencvplus.android;

import android.content.Intent;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import org.opencv.core.Scalar;

public class FdActivity extends Activity implements DisplayFrameListener {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private static final boolean DEBUG = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);
        startService(new Intent(this, CoreService.class));
        startService(new Intent(this, ResultDisplayService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDisplayFrameStarted() {

    }

    @Override
    public void onDisplayFrameStopped() {
    }

    @Override
    public void rawFrame(final byte[] frame) {

        if (!DEBUG) return;
    }

    @Override
    public void onNewFrame(CvCameraViewFrame input){

        /*Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;*/
    }
}
