package com.nexlink.statusbar;

import java.util.ArrayList;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NLService extends NotificationListenerService {
	public static final String ACTION_CLIENT_BIND = "com.nexlink.statusbar.ACTION_CLIENT_BIND";
    /** Command to the service to display a message */
	public static final int MSG_SUBSCRIBE = 0;
	public static final int MSG_CANCEL_NOTIFICATION = 1;
	public static final int MSG_CANCEL_ALL_NOTIFICATIONS = 2;
	public static final int MSG_GET_ACTIVE_NOTIFICATIONS = 3;
	
    private static final int MSG_NOTIFICATION_POSTED = 4;
    private static final int MSG_NOTIFICATION_REMOVED = 5;
    private static final int MSG_GOT_ACTIVE_NOTIFICATIONS = 6;
    
    private ArrayList<Messenger> clients = new ArrayList<Messenger>();

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_SUBSCRIBE:
            	    clients.add((Messenger) msg.obj);
            	    break;
                case MSG_CANCEL_NOTIFICATION:
                	NotificationItem ni = (NotificationItem) msg.obj;
                	cancelNotification(ni.packageName, ni.tag, ni.notificationID);
                	break;
                case MSG_CANCEL_ALL_NOTIFICATIONS:
                	cancelAllNotifications();
                	break;
                case MSG_GET_ACTIVE_NOTIFICATIONS:
            		//getExistingNotifications has to be called some time later or else it crashes
            		//https://code.google.com/p/android/issues/detail?id=59044
							StatusBarNotification[] active = null;
							try{active = getActiveNotifications();}catch(NullPointerException e){Log.e("NLService", "Notifications not available! (Missing permission?)");}
							int length = active != null ? active.length : 0;
							NotificationItem[] a = new NotificationItem[length];
							for(int i = 0; i < length; i++){
								a[i] = makeNotificationInfo(active[i]);
							}
							Message m = new Message();
							m.what = MSG_GOT_ACTIVE_NOTIFICATIONS;
							m.obj = a;
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
    
    
    private interface ClientHandlerInterface {
    	public void onNotificationPosted(NotificationItem ni);
    	public void onNotificationRemoved(NotificationItem ni);
    	public void onGotActiveNotifications(NotificationItem[] ni);
    }
    public static abstract class ClientHandler extends Handler implements ClientHandlerInterface{
    	 @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case MSG_NOTIFICATION_POSTED:
             	onNotificationPosted((NotificationItem) msg.obj);
             	break;
             case MSG_NOTIFICATION_REMOVED:
                onNotificationRemoved((NotificationItem) msg.obj);
                break;
             case MSG_GOT_ACTIVE_NOTIFICATIONS:
            	 onGotActiveNotifications((NotificationItem[]) msg.obj);
             default:
                super.handleMessage(msg);
             }
         }
    }

    private NotificationItem makeNotificationInfo(StatusBarNotification sbn) {
		Notification n = sbn.getNotification();
		NotificationItem ni = new NotificationItem();
		ni.notificationID = sbn.getId();
		ni.packageName = sbn.getPackageName();
		ni.tag = sbn.getTag();
		ni.clearable = sbn.isClearable();
		ni.time = sbn.getPostTime();
		ni.priority = n.priority;
		ni.intent = n.contentIntent;
		ni.title = (String) (ni.clearable ? "":"📌 ") + n .extras.getCharSequence("android.title"); //Notification.EXTRA_TITLE
		ni.fullText = (String) n.extras.getCharSequence("android.text"); //Notification.EXTRA_TEXT
		ni.tickerText = (n.tickerText != null ? (String) n.tickerText : "");
		ni.iconDrawable = new ColorDrawable(Color.TRANSPARENT);
		try {
			ni.iconDrawable = createPackageContext(ni.packageName, 0).getResources().getDrawable(n.icon);
		} catch (NameNotFoundException e) {e.printStackTrace();}
		return ni;
	} 
    
    @Override
    public IBinder onBind(Intent intent) {
    	//The Android system also needs to bind to this service and it will choke if we return the custom binder
    	return intent.getAction().equals(ACTION_CLIENT_BIND) ? mToNLService.getBinder() : super.onBind(intent);
    }
	@Override
	public void onNotificationPosted(StatusBarNotification arg0) {
		for(Messenger client : clients){
			try {
				client.send(Message.obtain(null, MSG_NOTIFICATION_POSTED, makeNotificationInfo(arg0)));
			} catch (RemoteException e) {e.printStackTrace();}
		}
	}
	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {
		for(Messenger client : clients){
			try {
				client.send(Message.obtain(null, MSG_NOTIFICATION_REMOVED, makeNotificationInfo(arg0)));
			} catch (RemoteException e) {e.printStackTrace();}
		}
	}
}