package in.omerjerk.droidcv.samples;

import android.content.Intent;
import android.media.MediaFormat;
import org.opencv.android.CameraBridgeViewBase;

import in.omerjerk.droidcv.CoreDisplayService;

/**
 * Created by omerjerk on 15/7/14.
 */
public class SampleService extends CoreDisplayService {

    @Override
    public int setClassifier() {
        return R.raw.lbpcascade_frontalface;
    }

    @Override
    public int setMode() {
        return CoreDisplayService.MODE_DISPLAY_SCREEN;
    }

    @Override
    public void onNewFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        //Call the super method only for debugging purposes
        super.onNewFrame(frame);
    }

    @Override
    public void rawFrame(final byte[] frame) {
        //TODO: Callback for getting the raw frames.
        //The format if this frame will be according to the output format of the decoder
    }

    @Override
    public void onDisplayFrameStarted() {
        //TODO: Callback when the codec is just going to start
    }

    @Override
    public void onDisplayFrameStopped() {
        //TODO: Callback when the codec finishes
    }

    @Override
    public void decoderOutputFormatChanged(MediaFormat format) {
        //TODO: Override this to get info about the output format of the decoder
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ResultDisplayService.class));
    }
}
