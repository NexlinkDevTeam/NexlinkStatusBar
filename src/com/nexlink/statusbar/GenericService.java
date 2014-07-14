package com.nexlink.statusbar;

import java.util.ArrayList;

import android.app.Notification;
import android.app.Service;
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

public class GenericService extends Service {
	public static final String ACTION_CLIENT_BIND = "com.nexlink.statusbar.ACTION_CLIENT_BIND";
    /** Command to the service to display a message */
	public static final int MSG_RUN = 0;

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_RUN:
            	    run((Runnable) msg.obj);
            	    break;
                default:
                    super.handleMessage(msg);			
            }
        }
    }
    final Messenger mToNLService = new Messenger(new IncomingHandler());
    /*
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
    }*/
    
    @Override
    public IBinder onBind(Intent intent) {
    	return mToNLService.getBinder();
    }
    
    private void run(Runnable r){
    	System.out.println("running!");
    	Thread t = new Thread(r);
    	t.start();
    }
}