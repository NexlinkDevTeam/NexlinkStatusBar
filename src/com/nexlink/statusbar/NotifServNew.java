package com.nexlink.statusbar;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotifServNew extends NotificationListenerService implements NotificationService{
	private static NotifServNew notifServNew;
	public static NotifServNew getInstance(){
		return notifServNew;
	}
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
							try{active = getActiveNotifications();}catch(NullPointerException e){Log.e("NotifServNew", "Notifications not available! (Missing permission?)");}
							int length = active != null ? active.length : 0;
							NotificationItem[] a = new NotificationItem[length];
							for(int i = 0; i < length; i++){
								a[i] = new NotificationItem(notifServNew, active[i]);
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
    
    @Override
    public IBinder onBind(Intent intent) {
    	notifServNew = this;
    	//The Android system also needs to bind to this service and it will choke if we return the custom binder
    	return intent.getAction().equals(ACTION_CLIENT_BIND) ? mToNLService.getBinder() : super.onBind(intent);
    }
	@Override
	public void onNotificationPosted(StatusBarNotification arg0) {
		for(Messenger client : clients){
			try {
				client.send(Message.obtain(null, MSG_NOTIFICATION_POSTED, new NotificationItem(this, arg0)));
			} catch (RemoteException e) {e.printStackTrace();}
		}
	}
	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {
		for(Messenger client : clients){
			try {
				client.send(Message.obtain(null, MSG_NOTIFICATION_REMOVED, new NotificationItem(this, arg0)));
			} catch (RemoteException e) {e.printStackTrace();}
		}
	}
}