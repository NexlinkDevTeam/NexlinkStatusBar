package com.nexlink.statusbar;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private static Context mContext;
	private BroadcastReceiver mPackageSelectReceiver;

	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(Prefs.sharedprefsName);
			addPreferencesFromResource(R.xml.preferences);
			findPreference("selectPackages").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					Intent intent = new Intent(mContext, PackageSelectActivity.class);
					startActivity(intent);
					return true;
				}
			});
			findPreference("restart").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					mContext.stopService(new Intent(mContext, MainService.class));
					AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
					PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, MainService.class), 0);
					am.set(AlarmManager.RTC, 1000, pi);
					return true;
				}
			});
			findPreference("start").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					mContext.startService(new Intent(mContext, MainService.class));
					return true;
				}
			});
			findPreference("stop").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					mContext.stopService(new Intent(mContext, MainService.class));
					return true;
				}
			});
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mPackageSelectReceiver != null) unregisterReceiver(mPackageSelectReceiver);
	}
	
	public static String getAppNameByPID(Context context, int pid){
	    ActivityManager manager 
	               = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

	    for(RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()){
	        if(processInfo.pid == pid){
	            return processInfo.processName;
	        }
	    }
	    return "";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		registerReceiver(mPackageSelectReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				ArrayList < String > packageNames = intent.getExtras().getStringArrayList("packageNames");
				if (packageNames != null) {
					Prefs.setNotificationSources(mContext, new HashSet < String > (packageNames));
					Toast.makeText(context, "Saved notification apps!", Toast.LENGTH_LONG).show();
				}
			}
		}, new IntentFilter("com.nexlink.statusbar.PACKAGE_SELECT"));
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
	}
}