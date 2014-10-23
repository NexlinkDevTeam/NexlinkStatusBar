package com.nexlink.statusbar;
 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
 
public class GlobalPrefsReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
    	System.out.println("prefs changed");
    	context.stopService(new Intent(context, MainService.class));
    	context.startService(new Intent(context, MainService.class));
    }
}