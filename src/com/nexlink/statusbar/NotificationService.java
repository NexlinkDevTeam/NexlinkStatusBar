package com.nexlink.statusbar;

import android.os.Handler;
import android.os.Message;

public interface NotificationService {
	public static final String ACTION_CLIENT_BIND = "com.nexlink.statusbar.ACTION_CLIENT_BIND";
    /** Command to the service to display a message */
	public static final int MSG_SUBSCRIBE = 0;
	public static final int MSG_CANCEL_NOTIFICATION = 1;
	public static final int MSG_CANCEL_ALL_NOTIFICATIONS = 2;
	public static final int MSG_GET_ACTIVE_NOTIFICATIONS = 3;
	
    static final int MSG_NOTIFICATION_POSTED = 4;
    static final int MSG_NOTIFICATION_REMOVED = 5;
    static final int MSG_GOT_ACTIVE_NOTIFICATIONS = 6;
    
    interface ClientHandlerInterface {
    	public void onNotificationPosted(NotificationItem ni);
    	public void onNotificationRemoved(NotificationItem ni);
    	public void onGotActiveNotifications(NotificationItem[] ni);
    }
    public static abstract class ClientHandler extends Handler implements ClientHandlerInterface{
    	 @Override
         public final void handleMessage(Message msg) {
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
}
