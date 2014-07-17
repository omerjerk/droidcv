package in.umairkhan.opencvplus.android;

import android.net.Uri;

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
}
