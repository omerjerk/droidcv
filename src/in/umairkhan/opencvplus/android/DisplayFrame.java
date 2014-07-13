package in.umairkhan.opencvplus.android;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by omerjerk on 12/7/14.
 */
public class DisplayFrame implements CameraBridgeViewBase.CvCameraViewFrame {

    private Mat frame;
    private int height;
    private int width;
    public byte[] buffer;

    public DisplayFrame(int width, int height) {
        frame = new Mat(height, width, CvType.CV_8UC3);
        this.height = height;
        this.width = width;
    }

    public void updateFrame(byte[] frameBuffer) {
        frame.put(0, 0, frameBuffer);
        buffer = frameBuffer;
    }

    @Override
    public Mat rgba() {
        return frame;
    }

    @Override
    public Mat gray() {
        Mat grayFrame = new Mat(height, width, CvType.CV_8UC1);
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGB2GRAY);
        return grayFrame;
    }
}
