package com.nexlink.statusbar;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class NotifServOldAcc extends AccessibilityService{
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		NotifServOld n = NotifServOld.getInstance();
		if(n!=null){
			n.onAccessibilityEvent(event);
		}
	}

	@Override
	public void onInterrupt() {
		NotifServOld n = NotifServOld.getInstance();
		if(n!=null){
			n.onInterrupt();
		}
	}
}
