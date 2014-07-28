package com.nexlink.statusbar;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.nexlink.utilites.SystemApp;

public class App extends Application{
	public static final boolean supportsNLS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	private static NotificationService notificationService;
	
	public static NotificationService getNotificationService(){
		return notificationService;
	}
	
	private static Prefs mPrefs;
	public static Prefs getPrefs(){
		return mPrefs;
	}
	public static Prefs reloadPrefs(Context context){
		mPrefs = new Prefs(context);
		return mPrefs;
	}
	@Override
	public void onCreate(){
		//Load saved prefs
		mPrefs = reloadPrefs(this);
		
	    //Check if this is the default home app and prompt to set it if it's not
	    boolean isDefault = false;
		final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
	    filter.addCategory(Intent.CATEGORY_HOME);
	    final List<IntentFilter> filters = new ArrayList<IntentFilter>();
	    filters.add(filter);
	    List<ComponentName> activities = new ArrayList<ComponentName>();
	    final PackageManager packageManager = getPackageManager();
	    packageManager.getPreferredActivities(filters, activities, null);
	    final String myPackageName = getPackageName();
	    for (ComponentName activity : activities) {
	        if (myPackageName.equals(activity.getPackageName())) {
	        	isDefault = true;
	        	break;
	        }
	    }
	    if(!isDefault){
	        ComponentName componentName = new ComponentName(this, HomeDummy.class);
	        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	    }
	    
	    //Disable unneeded components depending on which version of notification listener we need
    	packageManager.setComponentEnabledSetting(new ComponentName(this, NotifServOld.class), supportsNLS ? 2 : 1, PackageManager.DONT_KILL_APP);
    	packageManager.setComponentEnabledSetting(new ComponentName(this, NotifServOldAcc.class), supportsNLS ? 2 : 1, PackageManager.DONT_KILL_APP);
    	if(supportsNLS){
    	    packageManager.setComponentEnabledSetting(new ComponentName(this, NotifServNew.class), supportsNLS ? 1 : 2, PackageManager.DONT_KILL_APP);
    	}
		
		
		//Try to grant temp system permissions (easier for testing)
    	String packageName = getPackageName();
		SystemApp.grantTempPermissions(packageName, new String[]{
				Manifest.permission.WRITE_SECURE_SETTINGS,
				Manifest.permission.ACCESS_NOTIFICATIONS //Need this for Android < 4.3
		});	
		//...Or install as system app
		if(!((SystemApp.isSystemApp(this, packageName)) != 1 /*|| SystemApp.isUpdate(this, packageName)*/)){
			//System.out.println(SystemApp.installAsSystemApp(packageName, "NexlinkStatusBar.apk", true));
			}
		
		//Once we have permission, automatically add an entry in settings to allow receiving notifications
		ContentResolver contentResolver = getContentResolver();
		notificationService = supportsNLS ? new NotifServNew() : new NotifServOld();
		String enabledSetting = supportsNLS ? "enabled_notification_listeners" : Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;
		String enabledClasses = Settings.Secure.getString(contentResolver, enabledSetting);
        String servicePath = packageName+"/"+packageName+(supportsNLS?".NotifServNew":".NotifServOldAcc");
		if ((enabledClasses == null || !enabledClasses.contains(servicePath)) && checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED){
			Settings.Secure.putString(getContentResolver(), enabledSetting, enabledClasses == null || enabledClasses.isEmpty() ? servicePath : enabledClasses + ":" + servicePath);
	    }
		System.out.println(Settings.Secure.getString(contentResolver, enabledSetting));
		
		//Start the service
		startService(new Intent(this, MainService.class));
	}
}
