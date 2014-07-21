package com.nexlink.statusbar;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
//Call through to the real home app
public class HomeActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(null);
	    final Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
	    final List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(homeIntent, 0);
	    for (ResolveInfo info : resolveInfo) {
	        if (info.activityInfo.applicationInfo.packageName != getPackageName()) {
	    	    System.out.println(info.activityInfo.applicationInfo.packageName);
	        	Intent intent = new Intent();
	        	intent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
	        	startActivity(intent);
	        	break;
	       }
	    }
		finish();
	}
}
