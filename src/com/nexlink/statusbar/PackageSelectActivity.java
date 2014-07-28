package com.nexlink.statusbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;

public class PackageSelectActivity extends Activity {

	private TabHost mTabHost;
	private ListView mUserListView;
	private ListView mSystemListView;
	private PackageItemAdapter mUserArrayAdapter;
	private PackageItemAdapter mSystemArrayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_package_select);
		
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();
		mTabHost.addTab(mTabHost.newTabSpec("User").setContent(R.id.package_select_user_listview_container).setIndicator("User", null));
		mTabHost.addTab(mTabHost.newTabSpec("System").setContent(R.id.package_select_system_listview_container).setIndicator("System", null));
		
		mUserListView = (ListView) findViewById(R.id.package_select_user_list);
		mSystemListView = (ListView) findViewById(R.id.package_select_system_list);
		
		mUserArrayAdapter = new PackageItemAdapter(this);
		mSystemArrayAdapter = new PackageItemAdapter(this);
		
		PackageManager pm = getPackageManager();
		List<PackageInfo> list = pm.getInstalledPackages(0);

		HashSet<String> enabled = App.getPrefs().getNotificationSources();

		for(PackageInfo pi : list) {	
		    ApplicationInfo ai = null;
			try {
				ai = pm.getApplicationInfo(pi.packageName, 0);
			} catch (NameNotFoundException e) {continue;}
			if ((ai.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0) {
				mSystemArrayAdapter.add(ai, enabled.contains(ai.packageName));       
		    }
		    else {
		    	mUserArrayAdapter.add(ai, enabled.contains(ai.packageName));
		    }
		}
		mUserListView.setAdapter(mUserArrayAdapter);
		mSystemListView.setAdapter(mSystemArrayAdapter);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		HashSet<String> enabled = new HashSet<String>();
		enabled.addAll(mUserArrayAdapter.getEnabled());
		enabled.addAll(mSystemArrayAdapter.getEnabled());
		Intent intent = new Intent();
		intent.setAction("com.nexlink.statusbar.PACKAGE_SELECT");
		intent.putStringArrayListExtra("packageNames", new ArrayList<String>(enabled));
		sendBroadcast(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.layout.package_select_menu, menu);
        return true;
    }
	
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item)
	 {
		PackageItemAdapter arrayAdapter = (mTabHost.getCurrentTab() == 0) ? mUserArrayAdapter : mSystemArrayAdapter;
		arrayAdapter.setAll(item.getItemId() == R.id.package_select_all);
		arrayAdapter.notifyDataSetChanged();
		return true;
	 }
}
