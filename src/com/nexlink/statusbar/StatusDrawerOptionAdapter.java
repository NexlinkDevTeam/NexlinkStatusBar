package com.nexlink.statusbar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class StatusDrawerOptionAdapter extends ArrayAdapter<StatusDrawerOptionItem> {
    private LayoutInflater mLayoutInflater;
    public StatusDrawerOptionAdapter(Context c) {
        super(c, R.layout.status_drawer_option_item);
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }   
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
        convertView = mLayoutInflater.inflate(R.layout.status_drawer_option_item, null);
    }
        	final StatusDrawerOptionItem sdoi = getItem(position);
            Button b = (Button) convertView.findViewById(R.id.option_button);
            b.setText(sdoi.label);
            Drawable[] d = new Drawable[2];
        	d[0] = sdoi.iconLayer1 != null ? sdoi.iconLayer1 : new ColorDrawable(Color.TRANSPARENT);
        	d[1] = sdoi.iconLayer2 != null ? sdoi.iconLayer2 : new ColorDrawable(Color.TRANSPARENT);
            b.setCompoundDrawablesWithIntrinsicBounds(null, new LayerDrawable(d), null, null);
            if(sdoi.onClickListener != null){
            	b.setOnClickListener(sdoi.onClickListener);
            }
            if(sdoi.onLongClickListener != null){
            	b.setOnLongClickListener(sdoi.onLongClickListener);
            }
        return convertView;
    }
}