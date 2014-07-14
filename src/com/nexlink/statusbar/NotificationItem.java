package com.nexlink.statusbar;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;

public class NotificationItem {
	public int notificationID;
	public int priority;
	public long time;
	public boolean clearable;
	public String packageName;
	public String tag;
	public String title;
	public String fullText;
	public String tickerText;
	public Drawable iconDrawable;
	public PendingIntent intent;
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
}