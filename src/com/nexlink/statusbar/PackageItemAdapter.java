package com.nexlink.statusbar;

import java.util.HashSet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class PackageItemAdapter extends ArrayAdapter < ApplicationInfo > {

	private final HashSet < String > mEnabledSet;
	private PackageManager mPackageManager;
	private LayoutInflater mLayoutInflater;

	public PackageItemAdapter(Context context) {
		super(context, R.layout.package_list_item);
		mEnabledSet = new HashSet <String> ();
		mPackageManager = context.getPackageManager();
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.package_list_item, null);
		}
		final ApplicationInfo ai = getItem(position);
		((TextView) convertView.findViewById(R.id.package_select_package_name)).setText(ai.packageName);
		((TextView) convertView.findViewById(R.id.package_select_app_name)).setText(mPackageManager.getApplicationLabel(ai));
		try {
			((ImageView) convertView.findViewById(R.id.package_select_icon)).setImageDrawable(mPackageManager.getApplicationIcon(ai.packageName));
		} catch (NameNotFoundException e) {}
		((CheckBox) convertView.findViewById(R.id.package_select_checkbox)).setChecked(mEnabledSet.contains(ai.packageName));
		convertView.setOnClickListener(new OnClickListener() {@Override
			public void onClick(View arg0) {
				CheckBox checkbox = (CheckBox) arg0.findViewById(R.id.package_select_checkbox);
				if (mEnabledSet.contains(ai.packageName)) {
					mEnabledSet.remove(ai.packageName);
					checkbox.setChecked(false);
				}
				else {
					mEnabledSet.add(ai.packageName);
					checkbox.setChecked(true);
				}
			}
		});
		return convertView;
	}

	public void add(ApplicationInfo ai, boolean enabled) {
		super.add(ai);
		if (enabled) {
			mEnabledSet.add(ai.packageName);
		}
		else {
			mEnabledSet.remove(ai.packageName);
		}
	}

	public HashSet < String > getEnabled() {
		return mEnabledSet;
	}

	public void setAll(boolean enabled) {
		if (enabled) {
			for (int i = 0, l = getCount(); i < l; i++) {
				mEnabledSet.add(getItem(i).packageName);
			}
		}
		else {
			mEnabledSet.clear();
		}
	}
}