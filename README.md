OpenCv Plus - Android
===================

* ### Route the display of an Android device to OpenCv and do all the image processing operations as you would do with a normal `Mat` object. ###

  **Requirements :**

  - Rooted device.
  - The app must be pushed into /system/priv-app
  - The device must have Kitkat

  **Usage :**

  Since while processing the display of your Android you will want your app to be run on the background, so we will be writing the core code inside a service.
  You will need to extend the abstract service class `CoreDisplayService`.
  ```
  public class SampleService extends CoreDisplayService {

    /**
     * This method must be implemented and the resource identifier to the classifier file
     * shoud be returned.
     */
    @Override
    public int setClassifier() {        
        return R.raw.lbpcascade_frontalface;
    }

    /**
     * Override this method to receive a new frame.
     * Call frame.gray() or frame.rgba() to get the respective Mat objects.
     * Note : I'm not able to generate rgba image, so for now frame.rgba() will
     * also return the grayscale Mat.
     */
    @Override
    public void onNewFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        //Call the super method only for debugging purposes
        super.onNewFrame(frame);
        Mat gray = frame.gray();
        //TODO: Do what you want with the Mat object
    }

    /**
     * Override this method to receive the raw frame in the form of bytes.
     * Note : The format of this frame will be according to the output format of the decoder.
     */
    @Override
    public void rawFrame(final byte[] frame) {
        //TODO: Callback for getting the raw frames       
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
  }
  ```
  You can start this service the normal way :
  ```
  startService(new Intent(mContext, SampleService.class));
  ```
  And don't forget to add the service to the `AndroidManifest.xml` file.

  **Behind the scene :**

  I create a virtual `Surface` and route the output of Android's display to the surface using `DisplayMAnager` API.
  Then I use this `Surface` as an input to the video encoder. Then I pass the ouput of the encoder to a video decoder.
  And the output from the video decoder is in the form of raw frames on which I do the further processing.
  