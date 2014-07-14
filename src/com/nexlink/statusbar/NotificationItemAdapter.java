package com.nexlink.statusbar;

import java.text.SimpleDateFormat;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationItemAdapter extends ArrayAdapter < NotificationItem > {
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private int mRowLayout;
	public NotificationItemAdapter(Context context, int resource) {
		super(context, resource);
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRowLayout = resource;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mLayoutInflater.inflate(mRowLayout, parent, false);
		}
		
		TextView textView = (TextView) convertView.findViewById(R.id.label);
		TextView titleTextView = (TextView) convertView.findViewById(R.id.notification_title);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
		TextView timeText = (TextView) convertView.findViewById(R.id.notification_time);
		LinearLayout iconContainer = (LinearLayout) convertView.findViewById(R.id.icon_container);

		NotificationItem ni = getItem(position);

		imageView.setImageDrawable(ni.iconDrawable);
		titleTextView.setText(ni.title);
		textView.setText(ni.fullText);
		timeText.setText(new SimpleDateFormat("h:mm aa").format(ni.time));
		iconContainer.setBackgroundColor(ni.priority > -2 ? Color.DKGRAY : Color.BLACK);
		return convertView;
	}
	public void doSort(){
		sort(new Comparator<NotificationItem>() {
			@Override
			public int compare(NotificationItem arg0, NotificationItem arg1) {
				return arg0.compareTo(arg1);
			}
		});
	}
}