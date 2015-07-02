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
	private boolean startLaunchApp(){
	    final PrefsHelper prefs = App.getPrefs();
	    Intent launchAppIntent = null;
	    for(String pkg : prefs.launchApps){
	    	System.out.println(pkg);
	    	launchAppIntent = getPackageManager().getLaunchIntentForPackage(pkg);
	    	if(launchAppIntent != null){
	    	    launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	    	    startActivity(launchAppIntent);
	    	}
	    }
	    if(launchAppIntent == null){
	    //Start our own launcher if it's installed or else look for another one
		List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
	    for(ResolveInfo info : resolveInfo) {
            if(info.activityInfo.applicationInfo.packageName.equals("com.nexlink.launcher")) {
        	    launchAppIntent = new Intent();
        	    launchAppIntent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
        	    break;
           }
        }
	    if(launchAppIntent == null){
	        for(ResolveInfo info : resolveInfo) {
	            if(!info.activityInfo.applicationInfo.packageName.equals(getPackageName())) {
	        	    launchAppIntent = new Intent();
	        	    launchAppIntent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
	        	    break;
	           }
	        }
	    }
	    if(launchAppIntent != null){
	    	launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	    	startActivity(launchAppIntent);
	    }
	    }
	    return launchAppIntent != null;
	}
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Strange things happen when trying to start a new activity or finish this one before boot completes...
	    if(SystemService.isRunning("bootanim")){
	    	mBootReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					if(startLaunchApp()){
					    finish();
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
			registerReceiver(mBootReceiver, intentFilter);
	    }
	    else if(startLaunchApp()){
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