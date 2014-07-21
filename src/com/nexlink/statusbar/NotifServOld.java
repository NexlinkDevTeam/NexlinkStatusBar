package com.nexlink.statusbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.INotificationManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

import com.google.android.gms.drive.internal.n;

public class NotifServOld extends Service implements NotificationService{
	private static NotifServOld notifServOld;
	public static NotifServOld getInstance(){
		return notifServOld;
	}
    private ArrayList<Messenger> clients = new ArrayList<Messenger>();
    
    private INotificationManager notificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_SUBSCRIBE:
            	    clients.add((Messenger) msg.obj);
            	    break;
                case MSG_CANCEL_NOTIFICATION:
                	NotificationItem ni = (NotificationItem) msg.obj;
                	//cancelNotification(ni.packageName, ni.tag, ni.notificationID);
                	break;
                case MSG_CANCEL_ALL_NOTIFICATIONS:
                	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                    	try {
                    		Class NM = Class.forName("android.app.INotificationManager");
                    		Method cancelAllNotifications = NM.getMethod("cancelAllNotifications", String.class);
                    		cancelAllNotifications.invoke(notificationManager, getPackageName());
            			} catch (Exception e) {e.printStackTrace();}
                    	}
                	break;
                case MSG_GET_ACTIVE_NOTIFICATIONS:
							Message m = new Message();
							m.what = MSG_GOT_ACTIVE_NOTIFICATIONS;
							m.obj = new NotificationItem[0];
				try {
					((Messenger) msg.obj).send(m);
				} catch (RemoteException e) {e.printStackTrace();}
                	break;
                default:
                    super.handleMessage(msg);	
            }
        }
    }
    final Messenger mToNLService = new Messenger(new IncomingHandler());
    
    public static List<String> getText(Notification notification)
    {
        RemoteViews views = notification.contentView;
        if (views == null) return null;
        List<String> text = new ArrayList<String>();
        try
        {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);
            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions)
            {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) continue;
                // View ID
                parcel.readInt();
                String methodName = parcel.readString();
                //System.out.println(methodName);
                if (methodName == null) continue;
                // Save strings
                else if (methodName.equals("setText"))
                {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();
                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                }
                // Save times. Comment this section out if the notification time isn't important
                else if (methodName.equals("setTime"))
                {
                    // Parameter type (5 = Long)
                    parcel.readInt();
                    String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
                    text.add(t);
                }
                parcel.recycle();
            }
        }
        catch (Exception e){Log.e("NotificationClassifier", e.toString());}
        return text;
    }

    private NotificationItem makeNotificationInfo(AccessibilityEvent event) {
    	Notification n = (Notification) event.getParcelableData();
    	List<String> l = getText(n);
		NotificationItem ni = new NotificationItem();
		ni.notificationID = new Random().nextInt(Integer.MAX_VALUE);
		ni.packageName = (String) event.getPackageName();
		ni.tag = "";
		ni.clearable = true;
		ni.time = n.when;
		ni.priority = 0;
		ni.intent = n.contentIntent;
		ni.title = l.get(0);
		ni.fullText = l.get(1);
		ni.tickerText = (n.tickerText != null ? (String) n.tickerText : "");
		ni.iconDrawable = new ColorDrawable(Color.TRANSPARENT);
		try {
			ni.iconDrawable = createPackageContext(ni.packageName, 0).getResources().getDrawable(n.icon);
		} catch (NameNotFoundException e) {e.printStackTrace();}
		return ni;
	}
    
    @Override
    public IBinder onBind(Intent intent) {
    	notifServOld = this;
    	return mToNLService.getBinder();
    }
    
    @Override
    public void onDestroy(){
    	notifServOld = null;
    }


	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			for(Messenger client : clients){
				try {
					client.send(Message.obtain(null, MSG_NOTIFICATION_POSTED, makeNotificationInfo(event)));
				} catch (RemoteException e) {e.printStackTrace();}
			}
	}
	}

	public void onInterrupt() {
	}
}