package com.nexlink.statusbar;

import android.app.Notification;
import android.os.Parcel;

public interface StatusbarNotificationInterface<T extends StatusbarNotificationInterface<T>> {
	public T clone();
	public int describeContents();
	public int getId();
	public String getKey();
	public Notification getNotification();
	public String getPackageName();
	public long getPostTime();
	public String getTag();
	public int getUserId();
	public boolean isClearable();
	public boolean isOngoing();
	public String toString();
	public void writeToParcel(Parcel out, int flags);
}
