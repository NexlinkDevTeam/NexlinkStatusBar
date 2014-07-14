package com.nexlink.statusbar;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class StatusDrawerOptionItem {
	public Drawable iconLayer1;
	public Drawable iconLayer2;
	public String label;
	public OnClickListener onClickListener;
	public OnLongClickListener onLongClickListener;
	public StatusDrawerOptionItem(Drawable icn1, Drawable icn2, String txt, OnClickListener ocl, OnLongClickListener olcl){ 
		iconLayer1 = icn1;
		iconLayer2 = icn2;
		label = txt;
		onClickListener = ocl;
		onLongClickListener = olcl;
	}
}