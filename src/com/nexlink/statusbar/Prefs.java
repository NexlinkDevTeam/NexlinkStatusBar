package com.nexlink.statusbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Prefs {
	public static final String sharedprefsName = "statusbar_prefs";
	
	private Context mContext;
	private SharedPreferences mSharedPrefs;
	private Editor mEditor;
	
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
	public boolean iconsTime;
	public boolean iconsBattery;
	public boolean iconsNotification;
	public boolean iconsBluetooth;
	public boolean iconsWifi;
	public boolean iconsSignal;
	public boolean iconsVolume;
	public boolean iconsLocation;
	
	public Prefs(Context context) {
		mContext = context;
		mSharedPrefs = mContext.getSharedPreferences(sharedprefsName, Context.MODE_PRIVATE);
		
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
		iconsTime = mSharedPrefs.getBoolean("iconsTime", true);
		iconsBattery = mSharedPrefs.getBoolean("iconsBattery", true);
		iconsNotification = mSharedPrefs.getBoolean("iconsNotification", true);
		iconsBluetooth = mSharedPrefs.getBoolean("iconsBluetooth", true);
		iconsWifi = mSharedPrefs.getBoolean("iconsWifi", true);
		iconsSignal = mSharedPrefs.getBoolean("iconsSignal", true);
		iconsVolume = mSharedPrefs.getBoolean("iconsVolume", true);
		iconsLocation = mSharedPrefs.getBoolean("iconsLocation", true);
	}
	
	public void setNotificationSources(HashSet<String> h) {
		mEditor.putStringSet("notificationSources", h).apply();
	}
	public HashSet<String> getNotificationSources() {
		return (HashSet<String>) mSharedPrefs.getStringSet("notificationSources", new HashSet<String>());
	}
	
	public void saveNotification(NotificationItem ni){
		mEditor.putString("N_"+ni.packageName+ni.notificationID, ni.stringify()).apply();
	}
	public void deleteNotification(NotificationItem ni){
		mEditor.remove("N_"+ni.packageName+ni.notificationID).apply();
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
				mEditor.remove(key).apply();
			}
		 }
	}
}
