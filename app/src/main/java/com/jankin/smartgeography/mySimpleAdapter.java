package com.jankin.smartgeography;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class mySimpleAdapter extends SimpleAdapter {

//	LayoutInflater inflater;   
    private Context context;
    
	public mySimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) 
	{
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.getView(position, convertView, parent);
		
	}
	
	
	

}
