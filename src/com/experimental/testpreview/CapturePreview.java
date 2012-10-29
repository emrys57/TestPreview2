package com.experimental.testpreview;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CapturePreview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    private static final String TAG = "CapturePreview";
    MediaRecorder mMediaRecorder;
    int mFrameNumber = 0;
    private static final boolean mRecordingVideo = true;


// from http://stackoverflow.com/questions/3739661/android-error-inflating-class 
public CapturePreview(Context context) {
    super(context);
    initialiseSurfaceHolder();
}

public CapturePreview(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialiseSurfaceHolder();
}

public void surfaceCreated(SurfaceHolder holder) {
	mCamera = Camera.open();
	try {
		Camera.Parameters parameters = mCamera.getParameters();
		mCamera.setDisplayOrientation(90); // just get it right for testing
		mCamera.setParameters(parameters);
		mCamera.setPreviewDisplay(holder);
		mCamera.setPreviewCallback(new PreviewCallback() {
			public void onPreviewFrame(byte[] data, Camera arg1) {
				Log.d(TAG, String.format("Frame %d", mFrameNumber++)); // see the frames in the logcat
			}
		});
	} catch (IOException exception) {
		mCamera.release();
		mCamera = null;
		Log.d(TAG, "exception setting parameters");
	}
}

public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	// Now that the size is known, set up the camera parameters and begin
	// the preview.
	Camera.Parameters parameters = mCamera.getParameters();
	List<Size> previewSizes = parameters.getSupportedPreviewSizes();
	Size previewSize = getOptimalPreviewSize(previewSizes, w, h);
	int w2 = previewSize.width;
	int h2 = previewSize.height;
	Log.d(TAG, String.format("surfaceChanged called want w=%d h=%d using w=%d h=%d", w, h, w2, h2));
	parameters.setPreviewSize(previewSize.width, previewSize.height);
	parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); 
	mCamera.setParameters(parameters);
	mCamera.startPreview();
	if (mRecordingVideo)
		startVideo(mCamera, holder);
}

// derived from http://developer.android.com/guide/topics/media/camera.html#capture-video
private void startVideo(Camera camera, SurfaceHolder holder) {
	camera.stopPreview(); // not specified in documentation but seems to be needed
	camera.unlock();
	mMediaRecorder = new MediaRecorder();
	mMediaRecorder.setCamera(camera);
	mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // No audio is recorded
	mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
	mMediaRecorder.setOutputFile("/dev/null");
	try {
		mMediaRecorder.setPreviewDisplay(holder.getSurface());
		mMediaRecorder.prepare();
	} catch (IOException e) {
		camera.release();
		Log.d(TAG, "startVideo: Failed.");
		e.printStackTrace();
	}
	mMediaRecorder.start();
}

//from http://alvinalexander.com/java/jwarehouse/android-examples/samples/android-9/ApiDemos/src/com/example/android/apis/graphics/CameraPreview.java.shtml
private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
 final double ASPECT_TOLERANCE = 0.1;
 double targetRatio = (double) w / h;
 // next line added because of some confusion between landscape and portrait - works for testing, at least!
 if (targetRatio < 1.0)
 	targetRatio = 1.0/targetRatio;
 if (sizes == null) return null;

 Size optimalSize = null;
 double minDiff = Double.MAX_VALUE;

 int targetHeight = h;
//   Log.d(TAG, String.format("targetRatio=%f", targetRatio));

 // Try to find an size match aspect ratio and size
 for (Size size : sizes) {
//   	Log.d(TAG, String.format("Trying: w=%d h=%d", size.width, size.height));
     double ratio = (double) size.width / size.height;
//       Log.d(TAG, String.format("ratio=%f", ratio));
     if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
     if (Math.abs(size.height - targetHeight) < minDiff) {
         optimalSize = size;
         minDiff = Math.abs(size.height - targetHeight);
     }
 }

 // Cannot find the one match the aspect ratio, ignore the requirement
 if (optimalSize == null) {
     minDiff = Double.MAX_VALUE;
     for (Size size : sizes) {
         if (Math.abs(size.height - targetHeight) < minDiff) {
             optimalSize = size;
             minDiff = Math.abs(size.height - targetHeight);
         }
     }
 }
 return optimalSize;
}

public void surfaceDestroyed(SurfaceHolder holder) {
	// Surface will be destroyed when we return, so stop the preview.
	// Because the CameraDevice object is not a shared resource, it's very
	// important to release it when the activity is paused.
	if (mRecordingVideo) {
		mMediaRecorder.stop();
	} else {
		mCamera.stopPreview();
	}
	mCamera.release();
	mCamera = null;
}

@SuppressWarnings("deprecation")
private void initialiseSurfaceHolder() {
    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated, but doesn't work if I take it out
}
// --------------------------------------------------------------------
// old unused code below here. Chunks were cut out of this to send to stackoverflow

public void surfaceCreatedOld(SurfaceHolder holder) {
	// The Surface has been created, acquire the camera and tell it where
	// to draw.
	Log.d(TAG, "Connecting to camera");
	mCamera = Camera.open();
	Log.d(TAG, "Opened camera");
	try {
		Camera.Parameters parameters = mCamera.getParameters();
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			// This is an undocumented although widely known feature
			// from http://stackoverflow.com/questions/8603249/android-camera-with-surface-view-potrait?rq=1
			Log.d(TAG, "setting portrait orientation");
			mCamera.setDisplayOrientation(90);
		} else {
			// This is an undocumented although widely known feature
			Log.d(TAG, "setting landscape orientation");
			parameters.set("orientation", "landscape");
			mCamera.setDisplayOrientation(0);
		}

		mCamera.setParameters(parameters);
		mCamera.setPreviewDisplay(holder);
		mCamera.setPreviewCallback(new PreviewCallback() {

			public void onPreviewFrame(byte[] data, Camera arg1) {
				Log.d(TAG, String.format("Frame %d", mFrameNumber));
				mFrameNumber++;

			}
		});
	} catch (IOException exception) {
		mCamera.release();
		mCamera = null;
		// TODO: add more exception handling logic here
	}
}

public void surfaceChangedOld(SurfaceHolder holder, int format, int w, int h) {
	// Now that the size is known, set up the camera parameters and begin
	// the preview.
	Camera.Parameters parameters = mCamera.getParameters();
	List<Size> previewSizes = parameters.getSupportedPreviewSizes();
	Size mPreviewSize = getOptimalPreviewSize(previewSizes, w, h);
	int w2 = mPreviewSize.width;
	int h2 = mPreviewSize.height;
	Log.d(TAG, String.format("surfaceChanged called want w=%d h=%d using w=%d h=%d", w, h, w2, h2));
	parameters.setPreviewSize(w2, h2);
	parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); 
	mCamera.setParameters(parameters);
	mCamera.startPreview();
	startVideo(mCamera, holder);
}

private void startVideoOld(Camera camera, SurfaceHolder holder) {
	camera.stopPreview(); // not specified in documentation but seems to be needed
	camera.unlock();
	mMediaRecorder = new MediaRecorder();
	mMediaRecorder.setCamera(camera);
	mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // No audio is recorded
	mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//			Environment.DIRECTORY_PICTURES), "EmrysTestDirectory");
//	if (! mediaStorageDir.exists()){
//		if (! mediaStorageDir.mkdirs()){
//			Log.d("EmrysTestDirectory", "failed to create directory");
//			return;
//		}
//	}
//	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//	File mediaFile;
//	mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//			"VID_"+ timeStamp + ".mp4");
//	mMediaRecorder.setOutputFile(mediaFile.toString());
	mMediaRecorder.setOutputFile("/dev/null");
	try {
		mMediaRecorder.setPreviewDisplay(holder.getSurface());
		mMediaRecorder.prepare();
	} catch (IOException e) {
		camera.release();
		Log.d(TAG, "startVideo: Failed.");
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	mMediaRecorder.start();
}
private void startVideo2(Camera camera, SurfaceHolder holder) {
	return;
}
}

