package com.nexlink.statusbar;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.accessibility.AccessibilityEvent;

public class NotifServOld extends Service implements NotificationService{
	private static NotifServOld notifServOld;
	public static NotifServOld getInstance(){
		return notifServOld;
	}
    private ArrayList<Messenger> clients = new ArrayList<Messenger>();

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
        	Message broadcastResponse = null;
            switch (msg.what) {
                case MSG_SUBSCRIBE:
            	    clients.add((Messenger) msg.obj);
            	    break;
                case MSG_CANCEL_NOTIFICATION:
                	NotificationItem ni = (NotificationItem) msg.obj;
                	App.getPrefs().deleteNotification(ni);
                	broadcastResponse = Message.obtain(null, MSG_NOTIFICATION_REMOVED, ni);
                	break;
                case MSG_CANCEL_ALL_NOTIFICATIONS:
                	App.getPrefs().deleteAllNotifications();
                	break;
                case MSG_GET_ACTIVE_NOTIFICATIONS:
				try {
					((Messenger) msg.obj).send(Message.obtain(null, MSG_GOT_ACTIVE_NOTIFICATIONS, App.getPrefs().getSavedNotifications()));
				} catch (RemoteException e) {e.printStackTrace();}
                	break;
                default:
                    super.handleMessage(msg);
                    break;
            }
                if(broadcastResponse != null){
                	for(Messenger client : clients){
            			try {
            				client.send(broadcastResponse);
            			} catch (RemoteException e) {e.printStackTrace();}
            		}
                }
            }
        }
    final Messenger mToNLService = new Messenger(new IncomingHandler());
    
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
					client.send(Message.obtain(null, MSG_NOTIFICATION_POSTED, new NotificationItem(this, event)));
				} catch (RemoteException e) {e.printStackTrace();}
			}
	    }
	}

	public void onInterrupt() {
	}
}