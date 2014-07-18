package in.omerjerk.droidcv.samples;

import android.net.Uri;
import org.opencv.android.CameraBridgeViewBase;

import in.omerjerk.droidcv.CoreDisplayService;

/**
 * Created by omerjerk on 17/7/14.
 */
public class SampleVideoService extends CoreDisplayService {

    @Override
    public int setClassifier() {
        return R.raw.lbpcascade_frontalface;
    }

    @Override
    public int setMode() {
        return CoreDisplayService.MODE_VIDEO;
    }

    @Override
    public Uri setVideoSource() {
        return Uri.parse("/sdcard/derp.mp4");
    }

    @Override
    public void onNewFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        super.onNewFrame(frame);
    }
}
