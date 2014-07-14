package com.nexlink.statusbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LaunchActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(null);
		//startService(new Intent(this, MainService.class));
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
}
