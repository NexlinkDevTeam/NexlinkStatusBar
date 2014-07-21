package com.nexlink.statusbar;

import android.app.PendingIntent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class NotificationItem {
	public int notificationID = 0;
	public int priority = 0;
	public long time = 0;
	public boolean clearable = true;
	public String packageName = "";
	public String tag = "";
	public String title = "";
	public String fullText = "";
	public String tickerText = "";
	public Drawable iconDrawable = new ColorDrawable(Color.BLACK);
	public PendingIntent intent = null;
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