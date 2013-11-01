package com.obs.utils;

import com.obs.object.ListClientObject;
import com.obs.payapp.R;
import com.obs.payapp.R.id;
import com.obs.payapp.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomArrayAdapter extends ArrayAdapter<ListClientObject> 

{
	private final Context context;
	private final ListClientObject[] values;
	
	public CustomArrayAdapter(Context context, ListClientObject[] values) {
		super(context, R.layout.client_view_item, values);
		this.context = context;
		this.values = values;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.client_view_item, parent, false);
		TextView clientid = (TextView) rowView.findViewById(R.id.lv_item_client_id);
		TextView clientname = (TextView) rowView.findViewById(R.id.lv_item_client_name);
		TextView address = (TextView) rowView.findViewById(R.id.lv_item_address);
		TextView phoneno = (TextView) rowView.findViewById(R.id.lv_item_phoneno);
		clientid.setText(values[position].getAccountno());
		clientname.setText(values[position].getDisplayName());
		address.setText(values[position].getAddress());
		phoneno.setText(values[position].getPhoneno());
 		return rowView;
	}
}
