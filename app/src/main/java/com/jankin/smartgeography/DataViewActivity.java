package com.jankin.smartgeography;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class DataViewActivity extends Activity {
	
	private String username = null;
	private String passwd = null;
	private double MyLatitude = 0.0;
	private double MyLongitude = 0.0;
	private String lai = null;
	private String address = null;
	private String model = null;
	private Bitmap mBitmap = null;
	private String color = null;
	
	private ImageView mImageView = null;
	private TextView  mTextView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_view);
		this.setTitle("数据");
		
		Intent thisIntent = getIntent();
		Bundle IntentBundle = thisIntent.getExtras();
		
		username	= IntentBundle.getString("username");
		passwd	= IntentBundle.getString("passwd");
		MyLatitude = IntentBundle.getDouble("MyLatitude");
		MyLongitude = IntentBundle.getDouble("MyLongitude");
		lai			= IntentBundle.getString("lai");
		address		= IntentBundle.getString("address");
		model		= IntentBundle.getString("model");
		color = IntentBundle.getString("munsell");
		//mBitmap = HttpConnection.StringtoBitmap(IntentBundle.getString("imagedata"));
		mBitmap = HttpConnection.getThumbnail(IntentBundle.getString("imagepath"), 720);
		
		mImageView = (ImageView) findViewById(R.id.mImageView);
		mTextView = (TextView) findViewById(R.id.mTextView);
		
		mImageView.setImageBitmap(mBitmap);
		mTextView.setText("拍摄地址:" + address + "   LAI:"+lai + "   模式:"+model + " 颜色:" + color);
	}
	
	
	

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.data_view, menu);
//		return true;
//	}

}
