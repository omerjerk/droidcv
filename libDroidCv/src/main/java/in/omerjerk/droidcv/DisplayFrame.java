package in.omerjerk.droidcv;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by omerjerk on 12/7/14.
 */
public class DisplayFrame implements CameraBridgeViewBase.CvCameraViewFrame {

    private Mat frame;
    private int height;
    private int width;

    public DisplayFrame(int width, int height) {
        frame = new Mat(height, 1024, CvType.CV_8UC1);
        this.height = height;
        this.width = width;
    }

    public void updateFrame(byte[] frameBuffer) {
        frame.put(0, 0, frameBuffer);
    }

    @Override
    public Mat rgba() {
        //TODO: As of now I'm not able to create rgba frame
        return frame;
    }

    @Override
    public Mat gray() {
        return frame;
    }
}
