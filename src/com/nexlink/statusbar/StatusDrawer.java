package com.nexlink.statusbar;

import java.lang.reflect.Method;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

import com.nexlink.statusbar.SlidingDrawer.OnDrawerCloseListener;
import com.nexlink.statusbar.SlidingDrawer.OnDrawerOpenListener;
import com.nexlink.statusbar.SlidingDrawer.OnDrawerScrollListener;
import com.nexlink.utilites.Shell;
import com.nexlink.utilites.Shell.ShellException;


public class StatusDrawer {

	public RelativeLayout getLayout() {
		return slidingDrawerLayout;
	}

	public boolean passTouchEvent(MotionEvent event) {
		return (event.getAction() == MotionEvent.ACTION_DOWN) ? slidingDrawer.onInterceptTouchEvent(event) : slidingDrawer.onTouchEvent(event);
	}

	public void addNotification(NotificationItem ni) {
		notificationsListAdapter.add(ni);
		notificationsListAdapter.doSort();
		notificationsListAdapter.notifyDataSetChanged();
		notificationsListView.setAdapter(notificationsListAdapter);
		notificationsListView.setSelection(notificationsListView.getCount() - 1);
	}

	public void removeNotification(NotificationItem removed) {
		for (int i = 0; i < notificationsListAdapter.getCount(); i++) {
			NotificationItem ni = notificationsListAdapter.getItem(i);
			if (removed.isSameNotification(ni)) {
				notificationsListAdapter.remove(ni);
				break;
			}
		}
		notificationsListAdapter.notifyDataSetChanged();
		notificationsListView.setAdapter(notificationsListAdapter);
		notificationsListView.setSelection(notificationsListView.getCount() - 1);
	}

	public void resetDrawer() {
		if (slidingDrawer.isMoving()) {
			slidingDrawer.close();
		}
		if (!slidingDrawer.mExpanded) {
			slidingDrawer.animateOpen();
			slidingDrawer.animateClose();
		}
	}

	private MainService mMainService;
	private RelativeLayout slidingDrawerLayout;
	private SlidingDrawer slidingDrawer;
	private TabHost tabHost;
	private ListView notificationsListView;
	private NotificationItemAdapter notificationsListAdapter;
	
	private StatusDrawerOptionItem wifiSDOI;
	private StatusDrawerOptionItem bluetoothSDOI;
	private StatusDrawerOptionAdapter SDOIAdapter;
	private StatusDrawerOptionItem displaySDOI;
	private StatusDrawerOptionItem signalSDOI;
	private StatusDrawerOptionItem batterySDOI;
	private StatusDrawerOptionItem gpsSDOI;
	private StatusDrawerOptionItem settingsSDOI;
	private StatusDrawerOptionItem airplaneSDOI;
	
	public StatusDrawer(MainService ms) {
		mMainService = ms;
		slidingDrawerLayout = (RelativeLayout)((LayoutInflater) mMainService.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.status_drawer, null);
		slidingDrawer = (SlidingDrawer) slidingDrawerLayout.findViewById(R.id.status_drawer);
		notificationsListView = (ListView) slidingDrawerLayout.findViewById(R.id.notifications_list);
		notificationsListAdapter = new NotificationItemAdapter(mMainService, R.layout.notification_list_item);
		tabHost = (TabHost) slidingDrawerLayout.findViewById(R.id.tabhost);
		tabHost.setup();
		tabHost.addTab(tabHost.newTabSpec("Notifications").setContent(R.id.nl).setIndicator("Notifications", null));
		tabHost.addTab(tabHost.newTabSpec("Settings").setContent(R.id.settings_list).setIndicator("Settings", null));
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				if (tabHost.getCurrentTab() == 0) {
					//nlService.cancelTicker();
				}
			}
		});
		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			@Override
			public void onDrawerOpened() {
				if (tabHost.getCurrentTab() == 0) {
					//nlService.cancelTicker();
				}
			}
		});

		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
			@Override
			public void onDrawerClosed() {
				slidingDrawerLayout.setVisibility(View.GONE);
			}
		});

		slidingDrawer.setOnDrawerScrollListener(new OnDrawerScrollListener() {
			@Override
			public void onPreScrollStarted() {}
			@Override
			public void onScrollStarted() {
				slidingDrawerLayout.setVisibility(View.VISIBLE);
				slidingDrawerLayout.findViewById(R.id.status_handle).setBackgroundResource(R.drawable.status_bar_close_on);
				slidingDrawer.mExpanded = true;
			}
			@Override
			public void onScroll(boolean willBackward) {}
			@Override
			public void onScrollEnded() {
					slidingDrawerLayout.findViewById(R.id.status_handle).setBackgroundResource(R.drawable.status_bar_close_off);
			}
		});

		notificationsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView <? > arg0, View arg1, int arg2, long arg3) {
				NotificationItem n = (NotificationItem) arg0.getAdapter().getItem(arg2);
				PendingIntent p = n.intent;
				if (p != null) {
					try {
						p.send();
					} catch (CanceledException e) {e.printStackTrace();}
					slidingDrawer.close();
				}
			}

		});
		notificationsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView <? > arg0, View arg1, int arg2, long arg3) {
				NotificationItem ni = (NotificationItem) arg0.getAdapter().getItem(arg2);
				if (!ni.clearable) {
					return true;
				}
				notificationsListAdapter.remove(ni);
				notificationsListAdapter.notifyDataSetChanged();
				mMainService.cancelNotification(ni);
				return true;
			}
		});
		((Button) slidingDrawerLayout.findViewById(R.id.clear_button)).setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View arg0) {
				NotificationItem ni;
				for (int i = 0; i < notificationsListAdapter.getCount(); i++) {
					ni = notificationsListAdapter.getItem(i);
					if (ni.clearable) {
						notificationsListAdapter.remove(ni);
						i--;
					}
				}
				notificationsListAdapter.notifyDataSetChanged();
				mMainService.cancelAllNotifications();
				//nlService.cancelTicker();
			}
		});
		
		wifiSDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_wifi_0), null, "Wifi Off", new OnClickListener(){
			@Override
			public void onClick(View v) {
				WifiManager wifiManager = (WifiManager)mMainService.getSystemService(Context.WIFI_SERVICE);
				wifiManager.setWifiEnabled(wifiManager.isWifiEnabled() ? false : true);
			}
		}, new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				mMainService.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
				return true;
			}
		});
		
		
		bluetoothSDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_bluetooth_off), null, "Bluetooth Off", new OnClickListener(){
			@Override
			public void onClick(View v) {
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if(bluetoothAdapter != null){
				if (bluetoothAdapter.isEnabled()) {
				    bluetoothAdapter.disable(); 
				}
				else{
				    bluetoothAdapter.enable();
				}
				}
			}
		}, new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				mMainService.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
				return true;
			}
		});
		
		displaySDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_brightness_auto_off), null, "Brightness", new OnClickListener(){
			@Override
			public void onClick(View v) {
				mMainService.showBrightnessSlider();
			}
		}, new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				mMainService.startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
				return true;
			}
		});
		
		signalSDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_signal_0), null, "No Signal", new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					 ConnectivityManager conman = (ConnectivityManager) mMainService.getSystemService(Context.CONNECTIVITY_SERVICE);
				        Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
				        setMobileDataEnabledMethod.setAccessible(true);
				        setMobileDataEnabledMethod.invoke(conman, false);

				} catch (Exception e) {e.printStackTrace();}
			}
		}, new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				mMainService.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
				return true;
			}
		});
		
		batterySDOI = new StatusDrawerOptionItem(null, null, "0%", new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mMainService.startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
			}
		}, null);
		
		gpsSDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_location_off), null, "GPS", new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mMainService.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
			}
		}, null);
		
		
		airplaneSDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_airplane_on), null, "Airplane Mode", new OnClickListener(){
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
			@Override
			public void onClick(View arg0) {
				if(mMainService.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED){
				ContentResolver cr = mMainService.getContentResolver();
				boolean isEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
						Settings.System.getInt(cr, Settings.System.AIRPLANE_MODE_ON, 0) != 0:
						Settings.Global.getInt(cr, Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
						Settings.Global.putInt(cr, Settings.Global.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);		
		           try {
		        	   /*
		        	    * Not sure if this is possible for an unsigned app. Setting the app as persistent supposedly allows you
		        	    * to send system broadcasts, but I have not been able to get it to work
		        	    * http://androidforums.com/application-development/397247-notifying-other-applications-newly-installed-application.html
		        	    */
		        	   //Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			           //intent.putExtra("state", !isEnabled);
			           //mMainService.sendBroadcast(intent);
		        	    Shell.sudo("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + !isEnabled);
				} catch (ShellException e) {}
			}
			}
		}, new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				mMainService.startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
				return false;
			}	
		});
		
		settingsSDOI = new StatusDrawerOptionItem(mMainService.getResources().getDrawable(R.drawable.ic_qs_settings), null, "Settings", new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mMainService.startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		    	slidingDrawer.close();
			}
		}, null);
		
		GridView gridView = (GridView) slidingDrawerLayout.findViewById(R.id.options_grid);

		SDOIAdapter = new StatusDrawerOptionAdapter(mMainService);
		SDOIAdapter.add(bluetoothSDOI);
		SDOIAdapter.add(wifiSDOI);
		SDOIAdapter.add(displaySDOI);
		SDOIAdapter.add(signalSDOI);
		SDOIAdapter.add(batterySDOI);
		SDOIAdapter.add(gpsSDOI);
		SDOIAdapter.add(airplaneSDOI);
		SDOIAdapter.add(settingsSDOI);
		gridView.setAdapter(SDOIAdapter);

		SDOIAdapter.notifyDataSetChanged();
	}
	
	public void setWifi(int level, String ssid){
		int iconID;
		switch (level){
		case -1: iconID = R.drawable.ic_qs_wifi_ap_off; break;
		case 0: iconID = R.drawable.ic_qs_wifi_ap_on; break;
		case 1: iconID = R.drawable.ic_qs_wifi_full_1; break;
		case 2: iconID = R.drawable.ic_qs_wifi_full_2; break;
		case 3: iconID = R.drawable.ic_qs_wifi_full_3; break;
		case 4: iconID = R.drawable.ic_qs_wifi_full_4; break;
		default: iconID = 0;
		}

		wifiSDOI.iconLayer1 = iconID > 0 ? mMainService.getResources().getDrawable(iconID) : null;
	    wifiSDOI.label = ssid;
	    
	    SDOIAdapter.notifyDataSetChanged();
	}
	
	public void setBluetooth(Intent intent){
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(intent.getAction().compareTo(BluetoothAdapter.ACTION_STATE_CHANGED) == 0){
			if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
				bluetoothSDOI.iconLayer1 = mMainService.getResources().getDrawable(R.drawable.ic_qs_bluetooth_not_connected);
				bluetoothSDOI.label = "Bluetooth On";
			}
			else{
				bluetoothSDOI.iconLayer1 = mMainService.getResources().getDrawable(R.drawable.ic_qs_bluetooth_off);
				bluetoothSDOI.label = "Bluetooth Off";
			}
		}
		else if(intent.getAction().compareTo(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) == 0){
			if(intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED) == BluetoothAdapter.STATE_DISCONNECTED){
				bluetoothSDOI.iconLayer1 = mMainService.getResources().getDrawable(R.drawable.ic_qs_bluetooth_not_connected);
				bluetoothSDOI.label = "Bluetooth On";
			}
			else{
				bluetoothSDOI.iconLayer1 = mMainService.getResources().getDrawable(R.drawable.ic_qs_bluetooth_on);
				bluetoothSDOI.label = "Bluetooth On";
			}
		}
		SDOIAdapter.notifyDataSetChanged();
	}

	public void modifyNotification(NotificationItem ni) {
		for (int i = 0; i < notificationsListAdapter.getCount(); i++) {
			NotificationItem en = notificationsListAdapter.getItem(i);
			if (en.isSameNotification(ni)) {
				notificationsListAdapter.remove(en);
				notificationsListAdapter.insert(en, i);
				notificationsListAdapter.notifyDataSetChanged();
				notificationsListView.setAdapter(notificationsListAdapter);
				notificationsListView.setSelection(notificationsListView.getCount() - 1);
			}
		}
	}

	public void setSignal(int level, int type, String carrier) {
		int strengthID = R.drawable.ic_qs_signal_0;
        if (level <= 0) strengthID = R.drawable.ic_qs_signal_full_0;
        else if (level <= 1) strengthID = R.drawable.ic_qs_signal_full_1;
        else if (level <= 2) strengthID = R.drawable.ic_qs_signal_full_2;
        else if (level <= 3) strengthID = R.drawable.ic_qs_signal_full_3;
        else if (level <= 4) strengthID = R.drawable.ic_qs_signal_full_4;
        
        int typeID = 0;
        if (type == TelephonyManager.NETWORK_TYPE_HSDPA)  typeID = R.drawable.stat_sys_data_fully_connected_3g;
        else if (type == TelephonyManager.NETWORK_TYPE_HSPAP) typeID = R.drawable.stat_sys_data_fully_connected_4g;
        //else if (type == TelephonyManager.NETWORK_TYPE_EDGE) Log.e("2G enabled","2G enabled");
        
        	signalSDOI.iconLayer1 = mMainService.getResources().getDrawable(strengthID);
        	signalSDOI.iconLayer2 = typeID > 0 ? mMainService.getResources().getDrawable(typeID) : null;
        	signalSDOI.label = level <= 0 ? "No Signal" : carrier;
        	SDOIAdapter.notifyDataSetChanged();
	}
	
	public void setBattery(int level, boolean plugged){
    	DisplayMetrics dm = mMainService.getResources().getDisplayMetrics() ;

		float circleSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        float delta = circleSize - radius;
        float arcSize = (circleSize - (delta / 2)) * 2;

        Bitmap bmp = BitmapFactory.decodeResource(mMainService.getResources(), plugged ? R.drawable.ic_qs_blank : R.drawable.ic_qs_blank).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmp);
        
        Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(delta);

        //Thin circle
        canvas.drawCircle(circleSize, circleSize, radius, mPaint);

        //Arc
        mPaint.setColor(level >= 16 ? Color.WHITE : Color.RED);
        mPaint.setStrokeWidth(delta);
        RectF box = new RectF(delta,delta,arcSize,arcSize);
        float sweep = 360 * level * 0.01f;
        canvas.drawArc(box, 0, sweep, false, mPaint);

        batterySDOI.iconLayer1 = new BitmapDrawable(mMainService.getResources(), bmp);
        batterySDOI.label = level + "%";
        SDOIAdapter.notifyDataSetChanged();
	}
	
	public void setGps(int state){
		int iconID = R.drawable.ic_qs_location_off;
		String label = "Location Off";
		switch(state){
		case 0: break;
		case 1: iconID = R.drawable.ic_qs_location_lowpower; label = "Network Only"; break;
		case 2: iconID = R.drawable.ic_qs_location_on; label = "Device Only"; break;
		case 3: iconID = R.drawable.ic_qs_location_on; label = "High Accuracy"; break;
		}
		
		gpsSDOI.iconLayer1 = mMainService.getResources().getDrawable(iconID);
		gpsSDOI.label = label;
		SDOIAdapter.notifyDataSetChanged();
	}
	
	public void setAirplaneMode(boolean enabled){
		
	}
}