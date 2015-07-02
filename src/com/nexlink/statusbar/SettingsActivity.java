package com.nexlink.statusbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
	private static Context mContext;

	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			findPreference("selectLaunchApps").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					Intent i = new Intent(mContext, PackageSelectActivity.class);
					i.putExtra("MODE", "LAUNCH");
					startActivity(i);
					return true;
				}
			});
			findPreference("selectNotificationApps").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					Intent i = new Intent(mContext, PackageSelectActivity.class);
					i.putExtra("MODE", "NOTIFICATION");
					startActivity(i);
					return true;
				}
			});
			findPreference("restart").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					Intent intent = new Intent(mContext, MainService.class);
					mContext.stopService(intent);
					mContext.startService(intent);
					return true;
				}
			});
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
	}
}