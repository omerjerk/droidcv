package in.omerjerk.droidcv;

import android.media.MediaFormat;
import org.opencv.android.CameraBridgeViewBase;

/**
 * Created by omerjerk on 12/7/14.
 */
public interface DisplayFrameListener {
    public void onNewFrame(CameraBridgeViewBase.CvCameraViewFrame frame);
    public void onDisplayFrameStarted();
    public void onDisplayFrameStopped();
    public void rawFrame(byte[] frame);
    public void decoderOutputFormatChanged(MediaFormat format);
}
