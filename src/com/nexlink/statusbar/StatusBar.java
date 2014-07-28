package com.nexlink.statusbar;

import java.text.SimpleDateFormat;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusBar{
    private MainService mMainService;
    private Prefs mPrefs;
    private LinearLayout mStatusBarLayout;
    private TextView timeText, notificationText;
    private ImageView batteryIcon, signalIcon, wifiIcon, bluetoothIcon, volumeIcon, notificationIcon;
  //Let's not redraw these if we don't have to
    private int signalTypeID = 0;
    private int signalStrengthID = R.drawable.stat_sys_signal_null;
    private int wifiIconID = R.drawable.stat_sys_wifi_signal_null;
	private ImageView locationIcon;
	
	private NotificationItem tickerNotification = new NotificationItem();
   
    public LinearLayout getLayout(){
    	return mStatusBarLayout;
    }
    
    public void setTickerNotification(NotificationItem ni){
    	tickerNotification = ni;
    	notificationText.setText(ni.tickerText);
    	notificationIcon.setImageDrawable(ni.iconDrawable);
    }
    
    public NotificationItem getTickerNotification(){
    	return tickerNotification;
    }
    
    public StatusBar(MainService ms){
        mMainService = ms;
        mPrefs = App.getPrefs();
        		
        mStatusBarLayout = (LinearLayout) ((LayoutInflater) mMainService.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.status_bar, null);
        
        notificationIcon = (ImageView) mStatusBarLayout.findViewById(R.id.notification_icon);        
        notificationText = (TextView) mStatusBarLayout.findViewById(R.id.notification_text);
        volumeIcon = (ImageView) mStatusBarLayout.findViewById(R.id.volume_icon);
        locationIcon = (ImageView) mStatusBarLayout.findViewById(R.id.gps_icon);
        bluetoothIcon = (ImageView) mStatusBarLayout.findViewById(R.id.bluetooth_icon);
        wifiIcon = (ImageView) mStatusBarLayout.findViewById(R.id.wifi_icon);
        signalIcon = (ImageView) mStatusBarLayout.findViewById(R.id.signal_icon);
        batteryIcon = (ImageView) mStatusBarLayout.findViewById(R.id.battery_icon); 
        timeText = (TextView) mStatusBarLayout.findViewById(R.id.time_text);
        timeText.setText(new SimpleDateFormat("h:mm aa").format(System.currentTimeMillis()));
        
        notificationText.setSelected(true);

        if(!mPrefs.iconsTime) timeText.setVisibility(View.GONE);
        if(!mPrefs.iconsBattery)batteryIcon.setVisibility(View.GONE);
        if(!mPrefs.iconsSignal) signalIcon.setVisibility(View.GONE);
        if(!mPrefs.iconsWifi) wifiIcon.setVisibility(View.GONE);
        if(!mPrefs.iconsVolume) volumeIcon.setVisibility(View.GONE);
        if(!mPrefs.iconsLocation) locationIcon.setVisibility(View.GONE);
        if(!mPrefs.iconsBluetooth || BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()){
        	bluetoothIcon.setVisibility(View.GONE);
        }
        if(!mPrefs.iconsNotification){
        	notificationIcon.setVisibility(View.GONE);
        	notificationText.setVisibility(View.GONE);
        }
    }
        
    public void setTime(){
    	timeText.setBackgroundColor(Color.BLACK);
        timeText.setText(new SimpleDateFormat("h:mm aa").format(System.currentTimeMillis()));
    }
    
    public void setBattery(int level, boolean plugged){
    	DisplayMetrics dm = mMainService.getResources().getDisplayMetrics() ;

		float circleSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);
        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, dm);
        float delta = circleSize - radius;
        float arcSize = (circleSize - (delta / 2)) * 2;

        Bitmap bmp = BitmapFactory.decodeResource(mMainService.getResources(), plugged ? R.drawable.stat_sys_battery_charge : R.drawable.stat_sys_battery_blank).copy(Bitmap.Config.ARGB_8888, true);
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

        batteryIcon.setImageBitmap(bmp);
    }
    
    
    public void setSignal(int level, int type){
    	
    	int strengthID = R.drawable.stat_sys_signal_null;
        if (level <= 0) strengthID = R.drawable.stat_sys_signal_0_fully;
        else if (level <= 1) strengthID = R.drawable.stat_sys_signal_1_fully;
        else if (level <= 2) strengthID = R.drawable.stat_sys_signal_2_fully;
        else if (level <= 3) strengthID = R.drawable.stat_sys_signal_3_fully;
        else if (level <= 4) strengthID = R.drawable.stat_sys_signal_4_fully;
        
        int typeID = 0;
        if (type == TelephonyManager.NETWORK_TYPE_HSDPA)  typeID = R.drawable.stat_sys_data_fully_connected_3g;
        else if (type == TelephonyManager.NETWORK_TYPE_HSPAP) typeID = R.drawable.stat_sys_data_fully_connected_4g;
        //else if (type == TelephonyManager.NETWORK_TYPE_EDGE) Log.e("2G enabled","2G enabled");
        
        if(strengthID != signalStrengthID || typeID != signalTypeID){
        	Drawable[] d = new Drawable[2];
        	d[0] = mMainService.getResources().getDrawable(strengthID);
        	d[1] = typeID != 0 ? mMainService.getResources().getDrawable(typeID) : new ColorDrawable(Color.TRANSPARENT);
        	LayerDrawable ld = new LayerDrawable(d);
            signalIcon.setImageDrawable(ld);
            signalStrengthID = strengthID;
            signalTypeID = typeID;
        }
    }
    
    public void setRinger(int mode){
            	int id = 0;
            	if(mode == AudioManager.RINGER_MODE_SILENT){
            		id = R.drawable.stat_sys_ringer_silent;
            	}
            	else if(mode == AudioManager.RINGER_MODE_VIBRATE){
            		id = R.drawable.stat_sys_ringer_vibrate;
            	}
            	if(mPrefs.iconsVolume && id != 0){
            		volumeIcon.setImageDrawable(mMainService.getResources().getDrawable(id));
            		volumeIcon.setVisibility(View.VISIBLE);
            	}          	
            	else{
            		volumeIcon.setVisibility(View.GONE);
            	}
    }
    
    public void setBluetooth(Intent intent){
    	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(intent.getAction().compareTo(BluetoothAdapter.ACTION_STATE_CHANGED) == 0){
		    bluetoothIcon.setVisibility(mPrefs.iconsBluetooth && bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? View.VISIBLE : View.GONE);
		}
		else if(intent.getAction().compareTo(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) == 0){
			bluetoothIcon.setImageDrawable((intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED) == BluetoothAdapter.STATE_DISCONNECTED) ? mMainService.getResources().getDrawable(R.drawable.stat_sys_data_bluetooth): mMainService.getResources().getDrawable(R.drawable.stat_sys_data_bluetooth_connected));
		}
    }
    
    public void setWifi(int level){
        int iconID = R.drawable.stat_sys_wifi_signal_null;
        switch(level){
        case 1: iconID = R.drawable.stat_sys_wifi_signal_1_fully; break;
        case 2: iconID = R.drawable.stat_sys_wifi_signal_2_fully; break;
        case 3: iconID = R.drawable.stat_sys_wifi_signal_3_fully; break;
        case 4: iconID = R.drawable.stat_sys_wifi_signal_4_fully; break;
        }

        if(iconID != wifiIconID){
            wifiIcon.setImageDrawable(mMainService.getResources().getDrawable(iconID));
            wifiIconID = iconID;
        }
    }
    
    public void setLocation(int state){
    	if(mPrefs.iconsLocation){
		switch(state){
		case 0:
		case 1: locationIcon.setVisibility(View.GONE); break;
		case 2:
		case 3: locationIcon.setVisibility(View.VISIBLE); break;
		}
    	}
    }
}