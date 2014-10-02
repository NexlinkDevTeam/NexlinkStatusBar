package com.nexlink.statusbar;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemService;

public class HomeActivity extends Activity{
	private BroadcastReceiver mBootReceiver;
	//Call through to the real home app
	private void launchRealHome(){
		final Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
	    final List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(homeIntent, 0);
	    for (ResolveInfo info : resolveInfo) {
	        if (info.activityInfo.applicationInfo.packageName != getPackageName()) {
	        	Intent intent = new Intent();
	        	intent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
	        	startActivity(intent);
	        	break;
	       }
	    }
	}
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Strange things happen when trying to start a new activity or finish this one before boot completes...
	    if(SystemService.isRunning("bootanim")){
	    	mBootReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					launchRealHome();
					finish();
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
			registerReceiver(mBootReceiver, intentFilter);
	    }
	    else{
	    	launchRealHome();
	    	finish();
	    }
	}
	public void onDestroy(){
		super.onDestroy();
		if(mBootReceiver != null){
			unregisterReceiver(mBootReceiver);
		}
	}
}