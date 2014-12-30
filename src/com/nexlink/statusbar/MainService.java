package com.nexlink.statusbar;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

public class MainService extends Service implements OnTouchListener {
	private PrefsHelper mPrefs;

	private Handler handler;
	private StatusBar statusBar;
	private StatusDrawer statusDrawer;
	private List <NotificationItem> activeNotifications;
	private int tickerIndex;
	
	private WindowManager mWindowManager;
    private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
    private AudioManager mAudioManager;
	private TelephonyManager mTelephonyManager;
	private LocationManager mLocationManager;
	private KeyguardManager mKeyguardManager;

    private Messenger mToNLService;
    private Messenger mToMainService;
    private ServiceConnection mNLServiceConnection;
    
    //Views
	private LinearLayout mStatusBarLayout;
	private RelativeLayout mStatusDrawerLayout;
	
	//Receivers
	private BroadcastReceiver mLockScreenReceiver;
	private BroadcastReceiver mWifiReceiver;
	private BroadcastReceiver mBluetoothReceiver;
	private BroadcastReceiver mBatteryReceiver;
	private BroadcastReceiver mRingerReceiver;
	private BroadcastReceiver mTimeReceiver;
	private BroadcastReceiver mAirplaneReceiver;

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	
	private WindowManager.LayoutParams generateBarLayoutParams(){
		WindowManager.LayoutParams barLayoutParams = new WindowManager.LayoutParams();
		barLayoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		barLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		barLayoutParams.x = -1;
		barLayoutParams.y = -1;
		barLayoutParams.height = getStatusBarHeight() + ((Build.VERSION.SDK_INT >= 21 && mKeyguardManager.isKeyguardLocked()) ? 10 : 0);
		barLayoutParams.width = LayoutParams.MATCH_PARENT;
		barLayoutParams.format = mPrefs.generalEnabled ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
		barLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
		return barLayoutParams;
	}
	
	@Override
	public void onDestroy(){
		//Into the trash it goes
		super.onDestroy();
		if(handler != null) handler.removeCallbacksAndMessages(null);
		if(mNLServiceConnection != null) unbindService(mNLServiceConnection);
		if(mLockScreenReceiver != null) unregisterReceiver(mLockScreenReceiver);
		if(mWifiReceiver != null) unregisterReceiver(mWifiReceiver);
		if(mBluetoothReceiver != null) unregisterReceiver(mBluetoothReceiver);
		if(mBatteryReceiver != null) unregisterReceiver(mBatteryReceiver);
		if(mRingerReceiver != null) unregisterReceiver(mRingerReceiver);
		if(mTimeReceiver != null) unregisterReceiver(mTimeReceiver);
		if(mAirplaneReceiver != null) unregisterReceiver(mAirplaneReceiver);
		if(mStatusBarLayout != null) mWindowManager.removeView(mStatusBarLayout);
		if(mStatusDrawerLayout != null) mWindowManager.removeView(mStatusDrawerLayout);
	}
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate() {
		super.onCreate();
		
		mPrefs = App.reloadPrefs(this);
	
		handler = new Handler();
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		statusBar = new StatusBar(this);
		statusDrawer = new StatusDrawer(this);
		activeNotifications = new ArrayList <NotificationItem> ();
				
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		
		if(!mPrefs.generalEnabled){
			if(mPrefs.generalOpenable){
				//Nothing to do...
			    stopSelf();
			}
			
			else{ //Create an invisible overlay
				mStatusBarLayout = new LinearLayout(this);
				mStatusBarLayout.setAlpha(0);
				mStatusBarLayout.setOnTouchListener(this);
				mWindowManager.addView(mStatusBarLayout, generateBarLayoutParams());
			}
			return;
		}
		
		//Create the standard status bar overlay
		mStatusBarLayout = statusBar.getLayout();
		mStatusDrawerLayout = statusDrawer.getLayout();
		mStatusBarLayout.setOnTouchListener(this);

		WindowManager.LayoutParams drawerLayoutParams = new WindowManager.LayoutParams();
		drawerLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		drawerLayoutParams.height = LayoutParams.MATCH_PARENT;
		drawerLayoutParams.width = LayoutParams.MATCH_PARENT;
		drawerLayoutParams.format = PixelFormat.TRANSLUCENT;
		drawerLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;

		mWindowManager.addView(mStatusDrawerLayout, drawerLayoutParams);
		mStatusDrawerLayout.setVisibility(View.GONE);
		mWindowManager.addView(mStatusBarLayout, generateBarLayoutParams());
		
	  //Doesn't work unless the fullscreen app calls set setSystemUiVisibility;
	  /*mStatusBarLayout.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener(){
			@Override
			public void onSystemUiVisibilityChange(int flags) {
				System.out.println( Integer.toBinaryString(flags));
				if((flags & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.SYSTEM_UI_FLAG_FULLSCREEN ||
					(flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						){
					mStatusBarLayout.setVisibility(View.GONE);
					mStatusDrawerLayout.setVisibility(View.GONE);
				}
				else{
					mStatusDrawerLayout.setVisibility(View.VISIBLE);
				}
			}
		});*/
		
		//Use size changes in this dummy layout to detect full screen mode and react accordingly
		class DummyLayout extends LinearLayout{
			public DummyLayout(Context context) {
				super(context);
			}
			public DummyLayout(Context context, AttributeSet attrs) {
				super(context, attrs);
			}
			public DummyLayout(Context context, AttributeSet attrs, int defStyle) {
				super(context, attrs, defStyle);
			}
			@Override
		    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
		    {        
				WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);		
				if(yNew == size.y){ //Full screen
					mStatusBarLayout.setVisibility(View.GONE);
				}
				else {
					mStatusBarLayout.setVisibility(View.VISIBLE);
				}
				mStatusBarLayout.invalidate();
		        super.onSizeChanged(xNew, yNew, xOld, yOld);
		    }
		}
		
		final LinearLayout fullScreenCheckLayout = new DummyLayout(this);
		fullScreenCheckLayout.setAlpha(1);
		LayoutParams fullScreenCheckLayoutParams = new WindowManager.LayoutParams();
		fullScreenCheckLayoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		fullScreenCheckLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		fullScreenCheckLayoutParams.x = 0;
		fullScreenCheckLayoutParams.y = 0;
		fullScreenCheckLayoutParams.height = LayoutParams.MATCH_PARENT;
		fullScreenCheckLayoutParams.width = 0;
		fullScreenCheckLayoutParams.format = PixelFormat.OPAQUE;
		fullScreenCheckLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
		fullScreenCheckLayout.setLayoutParams(fullScreenCheckLayoutParams);
		fullScreenCheckLayout.setOnTouchListener(this);
		fullScreenCheckLayout.setBackgroundColor(Color.MAGENTA);
		mWindowManager.addView(fullScreenCheckLayout, fullScreenCheckLayoutParams);
		
		//Register the receivers for our status bar icons
		IntentFilter screenIntents = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		screenIntents.addAction(Intent.ACTION_SCREEN_ON);
		screenIntents.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(mLockScreenReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				mWindowManager.updateViewLayout(mStatusBarLayout, generateBarLayoutParams());
			}}, screenIntents);
		
		registerReceiver(mTimeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
            	statusBar.setTime();
            }
        }, new IntentFilter(Intent.ACTION_TIME_TICK));

	 registerReceiver(mRingerReceiver = new BroadcastReceiver(){
			@Override
            public void onReceive(Context context, Intent intent) {
            	int mode = mAudioManager.getRingerMode();
            	statusBar.setRinger(mode);
            }
        }, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));

	registerReceiver(mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {    	
            int level = i.getIntExtra("level", 0);
            boolean plugged = i.getIntExtra("plugged", 1) > 0;   
            statusBar.setBattery(level, plugged);
            statusDrawer.setBattery(level, plugged);
            }
    }, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	
        mTelephonyManager.listen(new PhoneStateListener()
        {
        	@Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            	super.onSignalStrengthsChanged(signalStrength);
                int levelDbm = 0;
                int levelEcio = 0;
                int level = 0;
                if (signalStrength.isGsm() && signalStrength.getGsmSignalStrength() != 99){
                    level = (int) Math.round(signalStrength.getGsmSignalStrength()/7.75);
                }
                else {
                    final int snr = signalStrength.getEvdoSnr();
                    final int cdmaDbm = signalStrength.getCdmaDbm();
                    final int cdmaEcio = signalStrength.getCdmaEcio();
                    if (snr == -1) {
                        if (cdmaDbm >= -75) levelDbm = 4;
                        else if (cdmaDbm >= -85) levelDbm = 3;
                        else if (cdmaDbm >= -95) levelDbm = 2;
                        else if (cdmaDbm >= -100) levelDbm = 1;
                        else levelDbm = 0;
                            
                        // Ec/Io are in dB*10
                        if (cdmaEcio >= -90) levelEcio = 4;
                        else if (cdmaEcio >= -110) levelEcio = 3;
                        else if (cdmaEcio >= -130) levelEcio = 2;
                        else if (cdmaEcio >= -150) levelEcio = 1;
                        else levelEcio = 0;
                            
                        level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
                    }
                    else {
                        if (snr == 7 || snr == 8) level = 4;
                        else if (snr == 5 || snr == 6 ) level = 3;
                        else if (snr == 3 || snr == 4) level = 2;
                        else if (snr ==1 || snr ==2) level = 1;
                    }
                }
                
                int type = mTelephonyManager.getNetworkType();
                String carrier = mTelephonyManager.getNetworkOperatorName();
                statusBar.setSignal(level, type);
                statusDrawer.setSignal(level, type, carrier);
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	

        IntentFilter bluetoothIntents = new IntentFilter();
        bluetoothIntents.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothIntents.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				statusBar.setBluetooth(intent);
				statusDrawer.setBluetooth(intent);
			}
        }, bluetoothIntents);
        statusBar.setBluetooth(new Intent().setAction(BluetoothAdapter.ACTION_STATE_CHANGED));
		statusDrawer.setBluetooth(new Intent().setAction(BluetoothAdapter.ACTION_STATE_CHANGED));
             
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mWifiReceiver = new BroadcastReceiver(){
			@Override
            public void onReceive(Context context, Intent intent) {
            	int level = -1;
            	  String ssid = "Wifi Off";
            	  if(mWifiManager.isWifiEnabled()){
            		  level = WifiManager.calculateSignalLevel(mWifiManager.getConnectionInfo().getRssi(), 5);
            		  ssid = "Wifi On";
            	  NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            	  if(networkInfo.isConnected()) {
            		  ssid = "Unknown Network";
            	    WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
            	    if(connectionInfo != null && connectionInfo.getSSID() != null) {
            	      ssid = connectionInfo.getSSID();
            	    }
            	}
            	  }
                statusBar.setWifi(level);
                statusDrawer.setWifi(level, ssid);
            }
        }, wifiFilter);
        
        LocationListener x = new LocationListener(){
        	public int getState(){
        		int state = 0; //0 = off, 1 = network, 2 = gps, 3 = network + gps
        		if(mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) state += 1;
        		if(mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) state += 2;
        		return state;
        	}
			@Override
			public void onLocationChanged(Location arg0) {
			    statusDrawer.setGps(getState());
			    statusBar.setLocation(getState());
			}

			@Override
			public void onProviderDisabled(String arg0) {
				statusDrawer.setGps(getState());
				statusBar.setLocation(getState());
			}

			@Override
			public void onProviderEnabled(String arg0) {
				statusBar.setLocation(getState());
				statusDrawer.setGps(getState());
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				statusBar.setLocation(getState());
				statusDrawer.setGps(getState());
			}};
			if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)){
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, x);
			}
			if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)){
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, x);
			}
			if (mLocationManager.getAllProviders().contains(LocationManager.PASSIVE_PROVIDER)){
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, x);
			}
			
			registerReceiver(mAirplaneReceiver = new BroadcastReceiver() {
	            @Override
	            public void onReceive(Context c, Intent i) {
	            	statusDrawer.setAirplaneMode(i.getBooleanExtra("state", false));
	            }
	        }, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
			boolean isEnabled = false;
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
				isEnabled = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
			}
			else{
				isEnabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
			}
			mAirplaneReceiver.onReceive(this, new Intent().putExtra("state", isEnabled));
        
        if(mPrefs.notificationsEnabled){
    		mToMainService = new Messenger(new NotificationService.ClientHandler() {		
    			@Override
    			public void onNotificationRemoved(final NotificationItem ni) {
    				handler.post(new Runnable() {
    					public void run() {
    						for (int i = 0; i < activeNotifications.size(); i++) {
    							if (activeNotifications.get(i).isSameNotification(ni)) {
    								activeNotifications.remove(i);
    								statusDrawer.removeNotificationListItem(ni);
    								break;
    							}
    						}
    					}
    				});
    			}
    			
    			@Override
    			public void onNotificationPosted(final NotificationItem ni) {
    				if(!mPrefs.notificationsEnabled){
    					return;
    				}
    				handler.post(new Runnable() {
    					public void run() {
    						if(mPrefs.notificationsWhitelisting && !mPrefs.notificationsSources.contains(ni.packageName)){
    							return;
    						}
    						for (int i = 0; i < activeNotifications.size(); i++) {
    							NotificationItem an = activeNotifications.get(i);
    							if (an.isSameNotification(ni)) {
    								activeNotifications.set(i, ni);
    								statusDrawer.modifyNotification(ni);
    								return;
    							}
    						}
    						statusDrawer.addNotificationListItem(ni);
    						//statusBar.setTickerNotification(ni);
    						activeNotifications.add(ni);
    					}
    				});
    			}

    			@Override
    			public void onGotActiveNotifications(NotificationItem[] existingNotifications) {
    				if (existingNotifications.length > 0) {
    					NotificationItem ni = null;
    					for (int i = 0; i < existingNotifications.length; i++) {
    						if(mPrefs.notificationsWhitelisting && !mPrefs.notificationsSources.contains(existingNotifications[i].packageName)){
    							continue;
    						}
    						ni = existingNotifications[i];
    						statusDrawer.addNotificationListItem(ni);
    						activeNotifications.add(ni);
    					}
    					statusBar.setTickerNotification(ni);
    				}
    			}
    		});
    		
    		NotificationService notificationService = App.getNotificationService();
    		bindService(new Intent(this, notificationService.getClass()).setAction(NotificationService.ACTION_CLIENT_BIND), mNLServiceConnection = new ServiceConnection(){
    			@Override
    			public void onServiceConnected(ComponentName name, IBinder service) {
    				mToNLService = new Messenger(service);
    	            try {
    	            	mToNLService.send(Message.obtain(null, NotificationService.MSG_GET_ACTIVE_NOTIFICATIONS, mToMainService));
    	            	mToNLService.send(Message.obtain(null, NotificationService.MSG_SUBSCRIBE, mToMainService));
    	            	} catch (RemoteException e) {}
    			}

    			@Override
    			public void onServiceDisconnected(ComponentName name) {
    				mToNLService = null;
    			}}, Context.BIND_AUTO_CREATE);
    		
    		
    		if(!App.supportsNLS){
    			cancelAllNotifications();
    		}
    }
        
        
    		//Rotate the active activeNotifications every 5 seconds
    		handler.postDelayed(new Runnable() {
    			public void run() {
    				if (activeNotifications.size() == 0) {
    					statusBar.setTickerNotification(new NotificationItem());
    				}
    				else if (activeNotifications.size() > tickerIndex) {
    					statusBar.setTickerNotification((NotificationItem) activeNotifications.get(tickerIndex));
    					tickerIndex++;
    				}
    				else {
    					tickerIndex = 0;
    				}
    				handler.postDelayed(this, 5000);
    			}
    		}, 5000);
    		}      

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return !mPrefs.generalOpenable || statusDrawer.passTouchEvent(event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);		
		//Stops the drawer from glitching when orientation is changed
		statusDrawer.resetDrawer();
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	public void showBrightnessSlider() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.alpha = 1.0f;
        layoutParams.packageName = getPackageName();
        layoutParams.buttonBrightness = 1f;
        layoutParams.windowAnimations = android.R.style.Animation_Dialog;

       final View view = View.inflate(this, R.layout.activity_brightness_popup, null);
       final SeekBar slider = (SeekBar) view.findViewById(R.id.brightness_slider);
       final ToggleButton autoButton = (ToggleButton) view.findViewById(R.id.brightness_slider_toggle);
       final Button okButton = (Button) view.findViewById(R.id.brightness_slider_ok);
       final Button cancelButton = (Button)view.findViewById(R.id.brightness_slider_cancel);
       
       int tmp1 = 127;
       int tmp2 = 0;
       try {
		tmp1 = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		tmp2 = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
	} catch (SettingNotFoundException e) {}
       final int brightness = tmp1;
       final int auto = tmp2;
       slider.setProgress(brightness);
       slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {}
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, arg0.getProgress());
		}
    	   
       });
       slider.setEnabled(auto != 1);
       autoButton.setChecked(auto == 1);
       autoButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			slider.setEnabled(!arg1);
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, arg1 ? 1 : 0);
		}});
        okButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mWindowManager.removeView(view);
			}	        	
        });
        cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(slider.getProgress() != brightness){
					Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
				}
				if(autoButton.isChecked() != (auto == 1)){
					Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, auto);
				}
				mWindowManager.removeView(view);
			}	        	
        });
        mWindowManager.addView(view, layoutParams);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void cancelNotification(NotificationItem ni) {
		if(mToNLService != null){
			try {
				mToNLService.send(Message.obtain(null, NotifServNew.MSG_CANCEL_NOTIFICATION, ni));
			} catch (RemoteException e) {e.printStackTrace();}
		}
	}

	public void cancelAllNotifications() {
		if(mToNLService != null){
			try {
				mToNLService.send(Message.obtain(null, NotifServNew.MSG_CANCEL_ALL_NOTIFICATIONS));
			} catch (RemoteException e) {e.printStackTrace();}
		}
		
		if(!App.supportsNLS){
			activeNotifications.clear();
		}
	}
}