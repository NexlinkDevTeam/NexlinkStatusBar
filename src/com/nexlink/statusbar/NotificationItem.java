package com.nexlink.statusbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ActivityManagerNative;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

public class NotificationItem implements Serializable{
	private static final long serialVersionUID = 1L;
	public int notificationID = 0;
	public int priority = 0;
	public long time = 0;
	public boolean clearable = true;
	public String packageName = "";
	public String tag = "";
	public String title = "";
	public String fullText = "";
	public String tickerText = "";
	public transient Drawable iconDrawable = new ColorDrawable(Color.TRANSPARENT);
	public transient PendingIntent pendingIntent = null; //Do something on click
	public transient PendingIntent deleteIntent = null; //Do something on delete
	///Need a way to serialize the above three objects for API < 17
	private int iconID = 0;
	String intentUri = "";
	private String deleteUri = "";
	
	   public static List<String> getText(Notification notification)
	    {
	        RemoteViews views = null;
	        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
	        	views = notification.bigContentView;
	        }
	        if (views == null){
	        	views = notification.contentView;
		        if (views == null){
		        	return null;
		        }
	        }
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
	                /*else if (methodName.equals("setTime"))
	                {
	                    // Parameter type (5 = Long)
	                    parcel.readInt();
	                    String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
	                    text.add(t);
	                }*/
	                parcel.recycle();
	            }
	        }
	        catch (Exception e){Log.e("NotificationClassifier", e.toString());}
	        return text;
	    }
	   
	    public NotificationItem(){}
	   
	    //API < 17
	    public NotificationItem(Context context, AccessibilityEvent event) {
	    	Notification n = (Notification) event.getParcelableData();
	    	List<String> l = getText(n);
			notificationID = new Random().nextInt(Integer.MAX_VALUE);
			packageName = (String) event.getPackageName();
			tag = "";
			clearable = true;
			time = n.when;
			priority = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? n.priority : 0;
			title =  l != null ? l.get(0) : "";
			fullText = l != null? l.get(1) : "";
			tickerText = (n.tickerText != null ? (String) n.tickerText : "");
			iconID = n.icon;
			pendingIntent = n.contentIntent;
			deleteIntent = n.deleteIntent;
			try {
				iconDrawable = context.createPackageContext(packageName, 0).getResources().getDrawable(iconID);
			} catch (NameNotFoundException e) {e.printStackTrace();}		
			if(pendingIntent != null){
				try {
					intentUri = ActivityManagerNative.getDefault().getIntentForIntentSender(pendingIntent.getTarget()).toUri(Intent.URI_INTENT_SCHEME);
				} catch (RemoteException e) {}
			}
			if(deleteIntent != null){
				try {
					deleteUri = ActivityManagerNative.getDefault().getIntentForIntentSender(deleteIntent.getTarget()).toUri(Intent.URI_INTENT_SCHEME);
				} catch (RemoteException e) {}
			}
		}
	    
	    //API >= 17
	    public NotificationItem(Context context, StatusBarNotification sbn) {
	    	Notification n = sbn.getNotification();
	    	List<String> l = getText(n);
			notificationID = sbn.getId();
			packageName = sbn.getPackageName();
			tag = sbn.getTag();
			clearable = sbn.isClearable();
			time = sbn.getPostTime();
			priority = n.priority;
			pendingIntent = n.contentIntent;
			deleteIntent = n.deleteIntent;
			title = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? n.extras.getString(Notification.EXTRA_TITLE, "") : l != null ? l.get(0) : "";
			fullText = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? n.extras.getString(Notification.EXTRA_TEXT, "") : l != null ? l.get(1) : "";
			tickerText = (n.tickerText != null ? (String) n.tickerText : "");
			try {
				iconDrawable = context.createPackageContext(packageName, 0).getResources().getDrawable(n.icon);
			} catch (NameNotFoundException e) {e.printStackTrace();}
		}

	public boolean isSameNotification(NotificationItem ni) {
		return (this.packageName.equals(ni.packageName) && this.notificationID == ni.notificationID);
	}
	public String toString() {
		return fullText;
	}
	public int compareTo(NotificationItem x) {
		if (this.clearable != x.clearable) {
			return !this.clearable ? -1 : 1;
		}
		else if (this.priority != x.priority) {
			return this.priority > x.priority ? -1 : 1;
		}
		return 0;
	}
	
	private static String objectToString(Serializable object) {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
	        new ObjectOutputStream(out).writeObject(object);
	        byte[] data = out.toByteArray();
	        out.close();
	        out = new ByteArrayOutputStream();
	        Base64OutputStream b64 = new Base64OutputStream(out, 0);
	        b64.write(data);
	        b64.close();
	        out.close();
	        return new String(out.toByteArray());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	private static Object stringToObject(String encodedObject) {
	    try {
	        return new ObjectInputStream(new Base64InputStream(new ByteArrayInputStream(encodedObject.getBytes()), 0)).readObject();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public String stringify(){
		return objectToString(this);
	}
	
	public static NotificationItem parse(Context context, String str){
		NotificationItem ni = (NotificationItem) stringToObject(str);
		if(ni.iconID != 0){
			try {
				ni.iconDrawable = context.createPackageContext(ni.packageName, 0).getResources().getDrawable(ni.iconID);
			} catch (NameNotFoundException e) {e.printStackTrace();}		
		}
		if(!ni.intentUri.isEmpty()){
			PackageManager pm = context.getPackageManager();
			try {
				Intent intent = Intent.parseUri(ni.intentUri, Intent.URI_INTENT_SCHEME);
				List<ResolveInfo> ri;
				if((ri = pm.queryIntentActivities(intent, 0)) != null && !ri.isEmpty()){
					ni.pendingIntent = PendingIntent.getActivity(context, ni.notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				}
				else if((ri = pm.queryIntentServices(intent, 0)) != null && !ri.isEmpty()){
					ni.pendingIntent = PendingIntent.getService(context, ni.notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				}
				else if((ri = pm.queryBroadcastReceivers(intent, 0)) != null && !ri.isEmpty()){
					ni.pendingIntent = PendingIntent.getBroadcast(context, ni.notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				}
			} catch (Exception e) {}
		}
		if(!ni.deleteUri.isEmpty()){
			PackageManager pm = context.getPackageManager();
			try {
				Intent intent = Intent.parseUri(ni.deleteUri, Intent.URI_INTENT_SCHEME);
				List<ResolveInfo> ri;
				if((ri = pm.queryIntentActivities(intent, 0)) != null && !ri.isEmpty()){
					ni.deleteIntent = PendingIntent.getActivity(context, ni.notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				}
				else if((ri = pm.queryIntentServices(intent, 0)) != null && !ri.isEmpty()){
					ni.deleteIntent = PendingIntent.getService(context, ni.notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				}
				else if((ri = pm.queryBroadcastReceivers(intent, 0)) != null && !ri.isEmpty()){
					ni.deleteIntent = PendingIntent.getBroadcast(context, ni.notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				}
			} catch (Exception e) {}
		}
		return ni;
	}
}