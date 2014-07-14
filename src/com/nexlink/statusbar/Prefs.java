package com.nexlink.statusbar;

import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
	public static final String sharedprefsName = "statusbar_prefs";
	
	public static boolean notificationsEnabled = true;
	public static boolean notificationsWhitelisting = false;
	public static HashSet<String> notificationsSources = new HashSet<String>();
	public static boolean generalOpenable = true;
	public static boolean generalEnabled = true;
	public static boolean optionsDisplay = true;
	public static boolean optionsBluetooth = true;
	public static boolean optionsWifi = true;
	public static boolean iconsTime = true;
	public static boolean iconsBattery = true;
	public static boolean iconsNotification = true;
	public static boolean iconsBluetooth = true;
	public static boolean iconsWifi = true;
	public static boolean iconsSignal = true;
	public static void init(Context c) {
		SharedPreferences p = c.getSharedPreferences("statusbar_prefs", Context.MODE_PRIVATE);
		notificationsEnabled = p.getBoolean("notificationsEnabled", true);
		notificationsWhitelisting = p.getBoolean("notificationsWhitelisting", false);
		notificationsSources = (HashSet<String>) p.getStringSet("notificationsSources", new HashSet<String>());
		generalOpenable = p.getBoolean("generalOpenable", true);
		generalEnabled = p.getBoolean("generalEnabled", true);
		optionsDisplay = p.getBoolean("optionsDisplay", true);
		optionsBluetooth = p.getBoolean("optionsBluetooth", true);
		optionsWifi = p.getBoolean("optionsWifi", true);
		iconsTime = p.getBoolean("iconsTime", true);
		iconsBattery = p.getBoolean("iconsBattery", true);
		iconsNotification = p.getBoolean("iconsNotification", true);
		iconsBluetooth = p.getBoolean("iconsBluetooth", true);
		iconsWifi = p.getBoolean("iconsWifi", true);
		iconsSignal = p.getBoolean("iconsSignal", true);
		
	}
	public static void setNotificationSources(Context c, HashSet<String> h) {
		c.getSharedPreferences(sharedprefsName, Context.MODE_PRIVATE).edit().putStringSet("notificationSources", h).apply();
	}
	public static HashSet<String> getNotificationSources(Context c) {
		return (HashSet<String>) c.getSharedPreferences(sharedprefsName, Context.MODE_PRIVATE).getStringSet("notificationSources", new HashSet<String>());
	}
}
