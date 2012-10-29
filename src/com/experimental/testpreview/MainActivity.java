package com.experimental.testpreview;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Menu;

public class MainActivity extends Activity {

	private CapturePreview mPreview;  // why does eclipse say this is unused?

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

//	    setFullscreen();

	    setContentView(R.layout.activity_main);

	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	    //mPreview = new CapturePreview(this);
	    //setContentView(mPreview);

	    mPreview = (CapturePreview)this.findViewById(R.id.capturePreview);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
