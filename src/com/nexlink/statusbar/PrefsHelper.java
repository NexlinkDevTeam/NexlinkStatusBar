package com.nexlink.statusbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
	private Context mContext;
	private SharedPreferences mSharedPrefs;
	
	public HashSet<String> launchApps;
	public boolean notificationsEnabled;
	public boolean notificationsWhitelisting;
	public HashSet<String> notificationsSources;
	public boolean generalOpenable;
	public boolean generalEnabled;
	public boolean optionsDisplay;
	public boolean optionsBluetooth;
	public boolean optionsWifi;
	public boolean optionsMobile;
	public boolean optionsBattery;
	public boolean optionsLocation;
	public boolean optionsAirplane;
	public boolean optionsSettings;
	public boolean optionsMDM;
	public boolean iconsTime;
	public boolean iconsBattery;
	public boolean iconsNotification;
	public boolean iconsBluetooth;
	public boolean iconsWifi;
	public boolean iconsSignal;
	public boolean iconsVolume;
	public boolean iconsLocation;
	
	public PrefsHelper(Context context) {
		mContext = context;

		mSharedPrefs = mContext.getSharedPreferences("com.nexlink.statusbar_preferences", Context.MODE_MULTI_PROCESS);
		
		launchApps = (HashSet<String>) mSharedPrefs.getStringSet("launchApps", new HashSet<String>());
		notificationsEnabled = mSharedPrefs.getBoolean("notificationsEnabled", true);
		notificationsWhitelisting = mSharedPrefs.getBoolean("notificationsWhitelisting", false);
		notificationsSources = (HashSet<String>) mSharedPrefs.getStringSet("notificationsSources", new HashSet<String>());
		generalOpenable = mSharedPrefs.getBoolean("generalOpenable", true);
		generalEnabled = mSharedPrefs.getBoolean("generalEnabled", true);
		optionsDisplay = mSharedPrefs.getBoolean("optionsDisplay", true);
		optionsBluetooth = mSharedPrefs.getBoolean("optionsBluetooth", true);
		optionsWifi = mSharedPrefs.getBoolean("optionsWifi", true);
		optionsMobile = mSharedPrefs.getBoolean("optionsMobile", true);
		optionsBattery = mSharedPrefs.getBoolean("optionsBattery", true);
		optionsLocation = mSharedPrefs.getBoolean("optionsLocation", true);
		optionsAirplane = mSharedPrefs.getBoolean("optionsAirplane", true);
		optionsSettings = mSharedPrefs.getBoolean("optionsSettings", true);
		optionsMDM = mSharedPrefs.getBoolean("optionsMDM", true);
		iconsTime = mSharedPrefs.getBoolean("iconsTime", true);
		iconsBattery = mSharedPrefs.getBoolean("iconsBattery", true);
		iconsNotification = mSharedPrefs.getBoolean("iconsNotification", true);
		iconsBluetooth = mSharedPrefs.getBoolean("iconsBluetooth", true);
		iconsWifi = mSharedPrefs.getBoolean("iconsWifi", true);
		iconsSignal = mSharedPrefs.getBoolean("iconsSignal", true);
		iconsVolume = mSharedPrefs.getBoolean("iconsVolume", true);
		iconsLocation = mSharedPrefs.getBoolean("iconsLocation", true);
	}
	
	public void setLaunchApps(HashSet<String> h) {
		mSharedPrefs.edit().putStringSet("launchApps", h).apply();
	}
	public HashSet<String> getLaunchApps() {
		return (HashSet<String>) mSharedPrefs.getStringSet("launchApps", new HashSet<String>());
	}
	
	public void setNotificationSources(HashSet<String> h) {
		mSharedPrefs.edit().putStringSet("notificationSources", h).apply();
	}
	public HashSet<String> getNotificationSources() {
		return (HashSet<String>) mSharedPrefs.getStringSet("notificationSources", new HashSet<String>());
	}
	
	public void saveNotification(NotificationItem ni){
		mSharedPrefs.edit().putString("N_"+ni.packageName+ni.notificationID, ni.stringify()).apply();
	}
	public void deleteNotification(NotificationItem ni){
		mSharedPrefs.edit().remove("N_"+ni.packageName+ni.notificationID).apply();
	}
	public NotificationItem[] getSavedNotifications(){
		List<NotificationItem> ni = new ArrayList<NotificationItem>();
		Map<String,?> keys = mSharedPrefs.getAll();
		for(Map.Entry<String,?> entry : keys.entrySet()){
			String key = entry.getKey();
			if(key.indexOf("N_") == 0){
				ni.add(NotificationItem.parse(mContext, mSharedPrefs.getString(key, "")));
			}
		 }
		return ni.toArray(new NotificationItem[ni.size()]);
	}
	public void deleteAllNotifications(){
		Map<String,?> keys = mSharedPrefs.getAll();
		for(Map.Entry<String,?> entry : keys.entrySet()){
			String key = entry.getKey();
			if(key.indexOf("N_") == 0){
				mSharedPrefs.edit().remove(key).apply();
			}
		 }
	}
}
