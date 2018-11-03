package com.jankin.smartgeography;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.baidu.location.LocationClient;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.igexin.sdk.PushManager;
import com.jankin.smartgeography.NearbyActivity.mySimpleAdapter.ViewHolder;
import com.jankin.smartgeography.PushJankinReceiver.GetuiClientIDListener;

public class MainActivity extends Activity implements CvCameraViewListener2, GetuiClientIDListener{

	private static String TAG = "MainActivity";

	private MediaPlayer mMediaPlayer = null;
	
	private String ClientID = null;
	
	private String SampleFolder = "北师大";
	private int Imglevels = 90;
	private int FeatureMode = 2;
	private String[] FeatureModeStr = {"饱和度", "蓝色波段", "亮度值", "绿度指数"};
	private double ParameterG = 0.5;
	private int DataCost = 2;
	
	private jankinDBOpenHelper Mydb = null;
	
	private SharedPreferences setting = null;
	private Editor editor = null;		//获取编辑器

	private JavaCameraView   mOpenCvCameraView = null;;
	private TabHost 	mTabHost = null;
	private TextView 	MsgView = null;
	private LinearLayout BatchDatatnLayout = null;
	private ListView 	mListView = null;
	private EditText	findEditText = null;
	private RelativeLayout findLayout = null;
	
	
	private String username = null;
	private String passwd = null;
	private boolean isLogin = false;
	
	private Mat mDisPlay = null;
	private Mat mRgba = null;
	private Mat mGray = null;
	private Mat mSendImg = new Mat();
	
	//加速度陀螺仪传感器
	private SensorManager sEnsor = null;
	private double Pitch = 0;
    private double Roll = 0;
	
	//百度地图相关API使用以及初始化
	private double MyLatitude = 0.0;
	private double MyLongitude = 0.0;
	private String MyAddress = null;
	private String CurrentTime = null;
	
	private String httpURL = HttpConnection.httpURL;
//	private String TelNum = "15311117371";
//	private String Verficode = "666666";
//	private String Username = null;
//	private String Passwd = null;
	
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new BDLocationListener() {
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			//Receive Location
			MyLatitude	= location.getLatitude();
			MyLongitude	= location.getLongitude();
			MyAddress	= location.getAddrStr();
			CurrentTime = location.getTime();
			
			if (isLogin==false && username!=null) {
				NetWorkThreadHandler.obtainMessage(MSG_NET_LOGIN).sendToTarget();
			}
			
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());		// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());	// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());	// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
 
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());	// 位置语义化信息
            List<Poi> list = location.getPoiList();		// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
		}
	};
	
	private void initLocation(){
		mLocationClient = new LocationClient(getApplicationContext());	//声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    		//注册监听函数
        
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");			//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);				//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);			//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);				//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);			//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);	//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);	//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);		//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);	//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);		//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        
        mLocationClient.start();	//开始定位
    }

	
	//陀螺仪角加速度计监听实现
    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

    	@Override
		public void onSensorChanged(SensorEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				float X_lateral = arg0.values[0];
				float Y_longitudinal = arg0.values[1];
				float Z_vertical = arg0.values[2];
				Pitch = Math.atan2(X_lateral, Z_vertical)*180/Math.PI;
				Roll  = Math.atan2(Y_longitudinal, Z_vertical)*180/Math.PI;
//				Log.i("czxLiuYang","\n 翻滚角度: "+Pitch);
//				Log.i("czxLiuYang","\n 俯仰角度: "+Roll);
//				String tmp = String.valueOf(Pitch);
//				if (tmp.length() > 5) {
//					tmp = tmp.substring(0, 5);
//				}
//				String displayStr = "翻滚:" + tmp + "°\t\t\t\t俯仰:";
//				tmp = String.valueOf(Roll);
//				if (tmp.length() > 5) {
//					tmp = tmp.substring(0, 5);
//				}				
//				displayStr += tmp + "°";
			}
		}
    	
    	@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
	
	static{
		System.loadLibrary("opencv_info");
		System.loadLibrary("opencv_java");
		System.loadLibrary("SmartLAI");
	}
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		setting = getSharedPreferences("SystemSeting", MODE_PRIVATE);
		editor = setting.edit();			//获取编辑器
		
		boolean isMember = setting.getBoolean("isMember", false);
		if (isMember == false) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, LoginActivity.class);
			startActivityForResult(intent, LoginActivityRequestCode);
			//startActivity(intent);
		}else {
			username = setting.getString("username", null);
			passwd	 = setting.getString("passwd", null);
		}
		
//		Intent intent = new Intent();
//		intent.setClass(MainActivity.this, LoginActivity.class);
//		startActivity(intent);
		
//		username = setting.getString("username", null);
//		passwd	 = setting.getString("passwd", null);
		
		Imglevels = setting.getInt("Imglevels", Imglevels);
		FeatureMode = setting.getInt("FeatureMode", FeatureMode);
		SampleFolder = setting.getString("SampleFolder", "JanKin");
		ParameterG = setting.getFloat("ParameterG", 0.5f);
		DataCost = setting.getInt("DataCost", DataCost);
		
		//传感器初始化
		sEnsor = (SensorManager) getSystemService(SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_ACCELEROMETER;
        sEnsor.registerListener(myAccelerometerListener,sEnsor.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_NORMAL);

		try{  
			mTabHost = (TabHost) this.findViewById(R.id.mTabHost);  
			mTabHost.setup();  
              
			mTabHost.addTab(mTabHost.newTabSpec("TAB1").setContent(R.id.LinearLayout1).setIndicator("测量", this.getResources().getDrawable(R.drawable.bnu))); 
			mTabHost.addTab(mTabHost.newTabSpec("TAB2").setContent(R.id.LinearLayout2).setIndicator("数据", this.getResources().getDrawable(R.drawable.bnu)));
			mTabHost.addTab(mTabHost.newTabSpec("TAB3").setContent(R.id.findLayout).setIndicator("发现", this.getResources().getDrawable(R.drawable.bnu)));  
			mTabHost.addTab(mTabHost.newTabSpec("TAB4").setContent(R.id.LinearLayout4).setIndicator("我",  this.getResources().getDrawable(R.drawable.user)));  
			mTabHost.setCurrentTab(2); 
			mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
				
				@Override
				public void onTabChanged(String tabId) {
					// TODO Auto-generated method stub
					if (tabId == "TAB2") {
						findViewById(R.id.waitProgressBar).setVisibility(View.VISIBLE);
						NetWorkThreadHandler.obtainMessage(MSG_NET_UPTELISTVIEW).sendToTarget();
					}
					
					if (tabId == "TAB1") {
						mOpenCvCameraView.enableView();
					}else {
						mOpenCvCameraView.disableView();
					}
				}
			});
        }catch(Exception ex){  
            ex.printStackTrace();  
        }
		
		MsgView = (TextView) findViewById(R.id.MsgView);
		findEditText = (EditText) findViewById(R.id.findEditText);
		findLayout	= (RelativeLayout) findViewById(R.id.findLayout);
		
		//查找编辑框EditText焦点改变事件
		findEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){
					findViewById(R.id.findBtnLayout).setVisibility(View.VISIBLE);
				}else{
					findViewById(R.id.findBtnLayout).setVisibility(View.INVISIBLE);
					findEditText.getText().clear();
				}
			}
		});
		findLayout.setFocusable(true);
		findLayout.setFocusableInTouchMode(true);
		findLayout.requestFocus();
		//使得查找编辑框EditText失去焦点
		findLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				findLayout.requestFocus();
				return false;
			}
		});
		
		mOpenCvCameraView = (JavaCameraView) findViewById(R.id.CameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setTouchFocusEnable();
        mOpenCvCameraView.enableView();
//        mOpenCvCameraView.setOnClickListener(OnClick);
        
        
        initLocation();
        NetWorkThreadStart();
        
        Mydb = new jankinDBOpenHelper(this);
        
        mListView = (ListView)findViewById(R.id.listview);  
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				if (isBatch)
					return;
				Intent intent = new Intent();
        		intent.putExtra("username", username);
        		intent.putExtra("passwd", passwd);
        		intent.putExtra("MyLatitude", MyLatitude);
        		intent.putExtra("MyLongitude", MyLongitude);
        		intent.putExtra("lai", 		(String)mList.get(arg2).get("lai"));
        		intent.putExtra("address", 	(String)mList.get(arg2).get("address"));
        		intent.putExtra("model", 	(String)mList.get(arg2).get("model"));
                intent.putExtra("munsell", 	(String)mList.get(arg2).get("munsell"));
//        		HttpConnection.saveBitmap(HttpConnection.getThumbnail((String)mList.get(arg2).get("imagepath"), 720));
        		intent.putExtra("imagepath", (String)mList.get(arg2).get("imagepath"));
        		//intent.putExtra("imagedata", HttpConnection.BitmaptoString(HttpConnection.getThumbnail((String)mList.get(arg2).get("imagepath"), 720)));		//(String)mList.get(arg2).get("imagepath")
        		intent.setClass(getApplicationContext(), DataViewActivity.class);
				startActivity(intent);
			}
		});
        BatchDatatnLayout = (LinearLayout) findViewById(R.id.BatchDatatnLayout);
        BatchDatatnLayout.setVisibility(View.GONE);
        
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				isBatch = !isBatch;
				if (isBatch) {
					BatchDatatnLayout.setVisibility(View.VISIBLE);
				}else {
					BatchDatatnLayout.setVisibility(View.GONE);
				}
//				UIHandler.obtainMessage(MSG_UI_UPDATELISTVIEW).sendToTarget();
				return false;
			}
		});
        
        PushJankinReceiver.setGetuiClientIDListener(this);
        if (username != null) {
            //初始化个推服务
            PushManager.getInstance().initialize(this.getApplicationContext());
		}
        
        RelativeLayout nearbyuserLayout = (RelativeLayout) findViewById(R.id.nearbyuserLayout);
        nearbyuserLayout.setOnTouchListener(OnTouch);
        
        RelativeLayout nearbydataLayout = (RelativeLayout) findViewById(R.id.nearbydataLayout);
        nearbydataLayout.setOnTouchListener(OnTouch);
        
        findViewById(R.id.whoaskmeLayout).setOnTouchListener(OnTouch);
        findViewById(R.id.myaskLayout).setOnTouchListener(OnTouch);
        findViewById(R.id.MyInfoLayout).setOnTouchListener(OnTouch);
        
	}
	
	@Override  
    protected void onActivityResult( int requestCode, int resultCode, Intent data )  
    {  
        switch ( requestCode ) {  
            case LoginActivityRequestCode :  
            	if(resultCode==RESULT_OK && data.getExtras().getString("result").equals("login"))
            	{
            		username = setting.getString("username", username);
        			passwd	 = setting.getString("passwd", username);
        			isLogin = false;
        			//初始化个推服务
                    PushManager.getInstance().initialize(this.getApplicationContext());
            	}
                break;  
            case WebViewActivityRequestCode:
            	isLogin = false;
            	break;
            default :  
                break;  
        }  
    } 
	
	private final int WebViewActivityRequestCode = 200;
	private final int LoginActivityRequestCode   = 201;
	private OnTouchListener OnTouch = new OnTouchListener() {
		boolean Continue = true;
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction()==MotionEvent.ACTION_DOWN) 
            { 
				//Btn.getDrawable().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED));
				arg0.setBackgroundColor(Color.rgb(208, 208, 208));
				Continue = true;
            } 
			if (Continue) {
				if (event.getAction()==MotionEvent.ACTION_MOVE) {
					if(event.getX() > 0 && event.getX() < arg0.getWidth() && event.getY()>0 && event.getY() < arg0.getHeight()){
	            		
	            	}else {
	            		arg0.setBackgroundResource(R.drawable.view_shape);
	            		Continue = false;
					}
				}
	            else if(event.getAction()==MotionEvent.ACTION_UP) 
	            { 
	            	arg0.setBackgroundResource(R.drawable.view_shape);
	            	if (isLogin) {
	            		Intent intent = new Intent();
	            		intent.putExtra("username", username);
	            		intent.putExtra("passwd", passwd);
	            		intent.putExtra("MyLatitude", MyLatitude);
	            		intent.putExtra("MyLongitude", MyLongitude);
		            	switch (arg0.getId()) {
		            	
						case R.id.nearbyuserLayout:
							intent.putExtra("action", "附近的用户");
							intent.setClass(getApplicationContext(),NearbyActivity.class);
							startActivity(intent);
							break;
						case R.id.nearbydataLayout:
							intent.putExtra("action", "附近的数据");
							intent.setClass(getApplicationContext(),NearbyActivity.class);
							startActivity(intent);
							break;
						case R.id.whoaskmeLayout:
							intent.putExtra("action", "谁申请查看我的数据");
							intent.setClass(getApplicationContext(),NearbyActivity.class);
							startActivity(intent);
							break;
						case R.id.myaskLayout:
							intent.putExtra("action", "我的申请");
							intent.setClass(getApplicationContext(),NearbyActivity.class);
							startActivity(intent);
							break;
						case R.id.MyInfoLayout:
							intent.putExtra("action", username + "的资料");
							intent.setClass(getApplicationContext(),WebViewActivity.class);
							startActivityForResult(intent, WebViewActivityRequestCode);
							break;
						default:
							break;
						}
					}else {
						Toast.makeText(MainActivity.this, "您未登录，请尝试打开网络", Toast.LENGTH_SHORT).show();
					}
	            }
			}
			return Continue;
		}
	};
	
	
	
	
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.CameraActBtn:
			if (isCalculateLAI) {
				Toast.makeText(MainActivity.this, "正在上传至服务器，请稍候...", Toast.LENGTH_SHORT).show();
			}else {
				isCalculateLAI = true;
			}
			
			break;
		/*
		case R.id.LogoutBtn:
			editor.putBoolean("isMember", false);
			editor.commit();
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, LoginActivity.class);
			startActivityForResult(intent, LoginActivityRequestCode);
			//startActivity(intent);
			break;
		*/
		case R.id.CameraSwtBtn:
			mOpenCvCameraView.disableView();
			if(mOpenCvCameraView.getCameraIndex() == JavaCameraView.CAMERA_ID_BACK){
				flipCode = -1;
				mOpenCvCameraView.setCameraIndex(JavaCameraView.CAMERA_ID_FRONT);
			}else if(mOpenCvCameraView.getCameraIndex() == JavaCameraView.CAMERA_ID_FRONT){
				flipCode = 1;
				mOpenCvCameraView.setCameraIndex(JavaCameraView.CAMERA_ID_BACK);
			}else{
				
			}
			mOpenCvCameraView.enableView();
			break;
		case R.id.nearbyuserLayout:
			break;
		case R.id.nearbydataLayout:
			//Toast.makeText(MainActivity.this, "...", Toast.LENGTH_SHORT).show();
			break;
		case R.id.AllSelectBtn:		//数据全选
			for (int i = 0; i < isChecked.length; i++) {
				isChecked[i] = true;
			}
			((myDataSimpleAdapter)mListView.getAdapter()).notifyDataSetChanged();
			break;
		case R.id.InvertSelectBtn:	//数据反选
			for (int i = 0; i < isChecked.length; i++) {
				isChecked[i] = !isChecked[i];
			}
			((myDataSimpleAdapter)mListView.getAdapter()).notifyDataSetChanged();
			break;
		case R.id.DelDataBtn:
			new AlertDialog.Builder(this).setTitle("确定删除数据吗？")  
		    .setIcon(android.R.drawable.ic_dialog_info)  
		    .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
		        @Override  
		        public void onClick(DialogInterface dialog, int which) {  
		        	// 点击“确认”后的操作  
					String delfiles = "[";
					int index = 0;
					for (int i=isChecked.length-1; i>=0; i--) {
						if(isChecked[i]){
							int keyid = Integer.valueOf((String)mList.get(i).get("keyid")).intValue();
							//从数据库中删除该条数据
							Mydb.delete(keyid);
							//记录删除的数据特征标志，从服务器删除数据
							if (index == 0) {
								delfiles +=  "{\"uid\":\""+mList.get(i).get("uid")+"\"}";
							}else {
								delfiles += ",{\"uid\":\""+mList.get(i).get("uid")+"\"}";
							}
							index++;
							mList.remove(i);
						}
					}
					delfiles += "]";
					if (delfiles.length()>10) {
						NetWorkThreadHandler.obtainMessage(MSG_NET_DELETEDATA, delfiles).sendToTarget();
					}
					BatchDatatnLayout.setVisibility(View.GONE);
					isBatch = false;
		        }  
		    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {  
		        @Override  
		        public void onClick(DialogInterface dialog, int which) {  
		        	// 点击“取消”后的操作,这里不设置没有任何操作 
		        	BatchDatatnLayout.setVisibility(View.GONE);
					isBatch = false;
		        }
		    }).show();  
			
			break;
		case R.id.finduserBtn:
			if (findEditText.getText().length() == 0) {
				Toast.makeText(MainActivity.this, "请设置合法的输入", Toast.LENGTH_SHORT).show();
				break;
			}
			if (isLogin) {
        		Intent mIntent = new Intent();
        		mIntent.putExtra("username", username);
        		mIntent.putExtra("passwd", passwd);
        		mIntent.putExtra("MyLatitude", MyLatitude);
        		mIntent.putExtra("MyLongitude", MyLongitude);
        		mIntent.putExtra("action", "查找用户");
        		mIntent.putExtra("keyWords", findEditText.getText().toString());
        		mIntent.setClass(getApplicationContext(), NearbyActivity.class);
				startActivity(mIntent);
            }else {
            	Toast.makeText(MainActivity.this, "您未登录，请尝试打开网络", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.finddataBtn:
			if (findEditText.getText().length() == 0) {
				Toast.makeText(MainActivity.this, "请设置合法的输入", Toast.LENGTH_SHORT).show();
				break;
			}
			if (isLogin) {
        		Intent mIntent = new Intent();
        		mIntent.putExtra("username", username);
        		mIntent.putExtra("passwd", passwd);
        		mIntent.putExtra("MyLatitude", MyLatitude);
        		mIntent.putExtra("MyLongitude", MyLongitude);
        		mIntent.putExtra("action", "查找数据");
        		mIntent.putExtra("keyWords", findEditText.getText().toString());
        		mIntent.setClass(getApplicationContext(), NearbyActivity.class);
				startActivity(mIntent);
            }else {
            	Toast.makeText(MainActivity.this, "您未登录，请尝试打开网络", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sEnsor.unregisterListener(myAccelerometerListener);
		if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sEnsor.unregisterListener(myAccelerometerListener);
	}


	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sEnsor.registerListener(myAccelerometerListener, sEnsor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mOpenCvCameraView.enableView();
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mOpenCvCameraView.disableView();
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	menu.add(Menu.NONE, Menu.FIRST+1, 0, "    设置").setIcon(android.R.drawable.ic_menu_edit);
    	menu.add(Menu.NONE, Menu.FIRST+2, 0, "    帮助").setIcon(android.R.drawable.ic_menu_edit);
    	menu.add(Menu.NONE, Menu.FIRST+3, 0, "    关于").setIcon(android.R.drawable.ic_menu_edit);
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case Menu.FIRST+1:
			ShowIPinputDialog();
			break;
		case Menu.FIRST+2:
		{
			Intent intent = new Intent();
			intent.putExtra("username", username);
			intent.putExtra("passwd", passwd);
			intent.putExtra("MyLatitude", MyLatitude);
			intent.putExtra("MyLongitude", MyLongitude);
			intent.putExtra("action", username + "的帮助");
			intent.setClass(getApplicationContext(), WebViewActivity.class);
			startActivityForResult(intent, -1);
			//flag = true;
		}
			break;
		case Menu.FIRST+3:
		{
			new AlertDialog.Builder(this).setTitle("北京师范大学")  
		    .setIcon(android.R.drawable.ic_dialog_info)  
		    .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
		        @Override  
		        public void onClick(DialogInterface dialog, int which) {  
		        	
		        }  
		    }).show();
		}
			//ComputerVisionThreadHandler.obtainMessage(MSG_NET_PHOTO_TEST).sendToTarget();
			break;
		case Menu.FIRST+4:
		default:
			break;
		}
		return false;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}
	
	
	private int offset = 0;
	//下面是数据映射关系,mFrom和mTo按顺序一一对应  
	private String[] mFrom = new String[]{"imgView", "TextView1", "TextView2", "TextView3", "mCheckBox"};  
	private int[] mTo = new int[]{R.id.imgView, R.id.TextView1, R.id.TextView2, R.id.TextView3, R.id.mCheckBox};
	//获取数据,这里随便加了10条数据,实际开发中可能需要从数据库或网络读取  
    private List<Map<String,Object>> mList = new ArrayList<Map<String,Object>>();
	
	private static final int MSG_CALCULATELAI = 100;	//获取图片失败的标识
	private static final int MSG_UI_DISPLAYLAI = 101;	//显示LAI等计算结果
	private static final int MSG_UI_DISPLAYMSG = 102;	//Toast显示信息
	private static final int MSG_UI_UPDATELISTVIEW = 103;
	private static final int MSG_UI_LOGIN_SUCCESSED = 105;
	
	private boolean hasUpdateData = false;
	private Handler UIHandler = new Handler() 
	{  
        public void handleMessage (Message msg) 
        {
        	//此方法在ui线程运行  
            switch(msg.what) 
            { 
            case MSG_CALCULATELAI:  
            	
            	break;
            case MSG_UI_UPDATELISTVIEW:
            	if (mList.size()>0) {
					//创建适配器  
            		myDataSimpleAdapter mAdapter = new myDataSimpleAdapter(MainActivity.this, mList, R.layout.listview_item, mFrom, mTo);
	                mListView.setAdapter(mAdapter);
				}else{
					mListView.setAdapter(null);
					Toast.makeText(MainActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
				}
                findViewById(R.id.waitProgressBar).setVisibility(View.INVISIBLE);
                if(hasUpdateData == false){
                	hasUpdateData = true;
                	NetWorkThreadHandler.obtainMessage(MSG_NET_UPDATEDATA).sendToTarget();
                }
            	break;
            case MSG_UI_DISPLAYLAI:
            	MsgView.setText((String)msg.obj);
            	break;
            case MSG_UI_DISPLAYMSG:
            	Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
            	break;
            case MSG_UI_LOGIN_SUCCESSED:
				((TextView)findViewById(R.id.UserNameTextView)).setText(username + "的资料");
				((TextView)findViewById(R.id.MyLocationTextView)).setText(MyAddress + "   积分:" + msg.arg1 + "分");
            	break;
            default:
            	Toast.makeText(MainActivity.this, "未知触发", Toast.LENGTH_SHORT).show();
            	break;
            }  
        }  
    };

    private int flipCode = 1;
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		
		Core.transpose(mRgba, mRgba);
		Core.flip(mRgba, mRgba, flipCode);
		
		if (NET_CV_Done) {
			NET_CV_Done = false;
			Imgproc.cvtColor(mRgba, mSendImg, Imgproc.COLOR_RGBA2RGB);
			NetWorkThreadHandler.obtainMessage(MSG_NET_CV).sendToTarget();
		}
		
		return mRgba;
	}
	
	//网络线程启动
	public void NetWorkThreadStart()
	{
		if (!NetWorkThread.isAlive()) {
			NetWorkThread.start();
		}
	}
	
	private Handler NetWorkThreadHandler = null;
	private boolean NET_CV_Done = true;
	
	private static final int MSG_NET_CV = 100;		//在网络线程中完成图像处理，计算完成之后通过http协议向服务器发送数据
	private static final int MSG_NET_UPDATECID = 101;
	private static final int MSG_NET_LOGIN 	= 102;
	private static final int MSG_NET_UPTELISTVIEW  = 103;
	private static final int MSG_NET_OPENUSBCAMEAR = 107;
	private static final int MSG_NET_DELETEDATA	= 200;
	private static final int MSG_NET_UPDATEDATA	= 201;	//从服务器同步数据
	//网络线程
	private Thread  NetWorkThread = new Thread(new Runnable() {

		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Looper.prepare();
			NetWorkThreadHandler = new Handler()
			{
				public void handleMessage (Message msg)
				{
					switch(msg.what)
					{
					case MSG_NET_CV:	//在这里完成图像处理算法，然后将结果提交到服务器
						double zenithAngle = 180-Roll;
						if(zenithAngle < 90) {
							
						}else if (zenithAngle < 180) {
							zenithAngle = 180-zenithAngle;
						}else if (zenithAngle < 270) {
							zenithAngle = zenithAngle-180;
						}else{
							zenithAngle = 360-zenithAngle;
						}
						
						int max = mSendImg.height();
						if (max<mSendImg.width()) {
							max = mSendImg.width();
						}
						float scale = 960.0f/max;
						Size dsize = new Size(mSendImg.cols()*scale, mSendImg.rows()*scale);
						Imgproc.resize(mSendImg, mSendImg, dsize, 0, 0, Imgproc.INTER_LINEAR);
						int[] num = new int[4];
						String munsell_color = Process(mSendImg.nativeObj, FeatureMode, num);
						double duty = 1.0 - ((double)num[0])/(mSendImg.cols()*mSendImg.rows());
						//textView.setText("植被比例是：" + (duty*100+"").substring(0, 5) + "%");
						if(duty <= 0)
							duty = Double.MIN_VALUE;
						double LAI = -Math.log(duty)*Math.cos(zenithAngle*Math.PI/180.0)/ParameterG;
						//textView.append("\n叶面积指数LAI：" + (LAI+"").substring(0, 7));
						
						String zenithAngleStr = "" + zenithAngle;
						if (zenithAngleStr.length()>5) {
							zenithAngleStr = zenithAngleStr.substring(0, 5);
						}
						String LAIStr = "" + LAI;
						if (LAIStr.length()>5) {
							LAIStr = LAIStr.substring(0, 5);
						}
						String color = "\n颜色:" + num[1] + "|" + num[2] + "|" + num[3] + "\nMunsell:" + munsell_color;
						String Infostr = "天顶角:" + zenithAngleStr + "\nLAI:" + LAIStr + color;
						UIHandler.obtainMessage(MSG_UI_DISPLAYLAI, Infostr).sendToTarget();
						if (isCalculateLAI) 	//保存本次的计算结果
						{
							AudioMsgPlay(R.raw.ding);
							if (storageImg(mSendImg)) {
								Map<String, String> Info = new HashMap<String, String>();
								Info.put("username", username);
								Info.put("image", HttpConnection.BitmaptoString(mCacheBitmap));
								Info.put("imagepath", fileName);
								Info.put("latitude", "" + MyLatitude);
								Info.put("longitude", "" + MyLongitude);
								Info.put("date", CurrentTime);
//								if (MyAddress == null) {
//									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "定位失败，以样方代替地址存储数据").sendToTarget();
//									Info.put("address", SampleFolder);
//								}else {
//									Info.put("address", MyAddress);
//								}
								Info.put("address", SampleFolder);
								Info.put("duty", ""+duty);
								Info.put("lai", LAIStr);
								Info.put("model", FeatureModeStr[FeatureMode-1]);
								Info.put("imei", "ABCDEFGHIIFULL");
								Info.put("cost", ""+DataCost);
								Info.put("uid", ""+CurrentTime+".jpg");
                                Info.put("munsell", munsell_color);
								int isupload = 0;
								String res = HttpConnection.HttpPostAction(httpURL+"upfilesave.php", Info, "UTF-8");
								if (res == null) {
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时，数据已保存至本地").sendToTarget();
								}else {
									try {
										JSONObject jsObj = new JSONObject(res);
										String result = jsObj.getString("result");
										if (result.equals("yes")) {
											isupload = 1;
											UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "数据已保存至服务器").sendToTarget();
										}else {
											UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器出错，数据已保存至本地").sendToTarget();
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错\n"+res).sendToTarget();
									}
								}
								Info.put("isupload", ""+isupload);
								
								Mydb.SaveData(Info);
							}else {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "保存数据失败").sendToTarget();
							}
							
							isCalculateLAI = false;
						}
						mSendImg.release();
						NET_CV_Done = true;
						break;
					case MSG_NET_UPDATECID:
						//获得个推的CID
				        ClientID = (String)msg.obj;
				        Log.i(TAG, ClientID);
				        Map<String, String> Info = new HashMap<String, String>();
    					Info.put("username", username);
    					Info.put("cid", ClientID);
    					String res = HttpConnection.HttpPostAction(httpURL+"updatecid.php", Info, "UTF-8");
    					if (res == null) {
    						UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "更新CID失败，服务器连接超时").sendToTarget();
						}else {
							//UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "更新CID成功").sendToTarget();
						}
						break;
					case MSG_NET_LOGIN:
						Map<String, String> mInfo = new HashMap<String, String>();
						mInfo.put("username", username);
						mInfo.put("passwd", passwd);
						mInfo.put("longitude", ""+MyLongitude);
						mInfo.put("latitude", ""+MyLatitude);
						String mres = HttpConnection.HttpPostAction(httpURL+"login_check.php", mInfo, "UTF-8");
						if (mres == null) {
							//Toast.makeText(MainActivity.this, "登录超时", Toast.LENGTH_SHORT).show();
						}else {
							try {
								JSONObject jsObj = new JSONObject(mres);
								String result = jsObj.getString("result");
								if (result.equals("yes")) {
									isLogin = true;
									int Points = jsObj.getInt("totalpoint");
									UIHandler.obtainMessage(MSG_UI_LOGIN_SUCCESSED, Points, 0).sendToTarget();
								}else {
									isLogin = true;
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "用户名或密码错误").sendToTarget();
									Intent intent = new Intent();
									intent.setClass(MainActivity.this, LoginActivity.class);
									startActivityForResult(intent, LoginActivityRequestCode);
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错\n"+mres).sendToTarget();
							}
						}
						break;
					case MSG_NET_UPTELISTVIEW:
					{
						offset = mList.size();
						List<Map<String,String>> ListData = Mydb.getScrollData(username, offset, 1000);
						if (ListData.size() > 0) {
							for(int i = 0;i < ListData.size();i++){ 
								if(!username.equals(ListData.get(i).get("uname")))
								{
									int keyid = Integer.valueOf(ListData.get(i).get("keyid")).intValue();
									Mydb.delete(keyid);
									continue;
								}
								Map<String, Object> mMap = new HashMap<String,Object>();  
					            Bitmap bm = HttpConnection.getThumbnail(ListData.get(i).get("imagepath"), 96);
					            if(bm != null)
					            {
				            		mMap.put("imgView", bm);  
						            mMap.put("TextView1", "LAI: " + ListData.get(i).get("lai"));  
						            mMap.put("TextView2", ListData.get(i).get("address"));
						            mMap.put("TextView3", ListData.get(i).get("model")+"   分值: " + ListData.get(i).get("cost"));
						            mMap.put("keyid", 	  ListData.get(i).get("keyid"));
						            mMap.put("lai", 	  ListData.get(i).get("lai"));
						            mMap.put("address",   ListData.get(i).get("address"));
						            mMap.put("model", 	  ListData.get(i).get("model"));
						            mMap.put("imagepath", ListData.get(i).get("imagepath"));
						            mMap.put("cost", 	  ListData.get(i).get("cost"));
						            mMap.put("uid", 	  ListData.get(i).get("uid"));
                                    mMap.put("munsell", 	  ListData.get(i).get("munsell"));
						            boolean flag = true;
						            if(Integer.valueOf( ListData.get(i).get("isupload") ).intValue() == 0){
						            	flag = false;
						            	if(isLogin){
						            		UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "正在向服务器同步数据..").sendToTarget();
							            	Map<String, String> Info1 = new HashMap<String, String>();
							            	Info1.put("keyid", 		ListData.get(i).get("keyid"));
											Info1.put("username", 	username);
											Info1.put("image", 		HttpConnection.BitmaptoString(BitmapFactory.decodeFile(ListData.get(i).get("imagepath")) ) );
											Info1.put("latitude",	ListData.get(i).get("lati"));
											Info1.put("longitude",	ListData.get(i).get("longi"));
											Info1.put("date",		ListData.get(i).get("date"));
											Info1.put("address",	ListData.get(i).get("address"));
											Info1.put("duty",		ListData.get(i).get("duty"));
											Info1.put("lai",		ListData.get(i).get("lai"));
											Info1.put("model",		ListData.get(i).get("model"));
											Info1.put("imei",		ListData.get(i).get("imei"));
											Info1.put("cost",		ListData.get(i).get("cost"));
											Info1.put("uid",		ListData.get(i).get("uid"));
                                            Info1.put("munsell",		ListData.get(i).get("munsell"));
											String res1 = HttpConnection.HttpPostAction(httpURL+"upfilesave.php", Info1, "UTF-8");
											if (res1 == null) {
												UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时，同步失败").sendToTarget();
											}else {
												try {
													JSONObject jsObj = new JSONObject(res1);
													String result = jsObj.getString("result");
													if (result.equals("yes")) {
														UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "已成功同步至服务器").sendToTarget();
														flag = true;
														Info1.put("isupload", "1");
														Mydb.UpdateData(Info1);
													}else {
														UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器出错，同步失败").sendToTarget();
													}
												} catch (JSONException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
													UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错，同步失败\n"+res1).sendToTarget();
												}
											}
						            	}
						            }
						            if(flag)
						            	mMap.put("TextView3", mMap.get("TextView3") + "    已同步");
						            else
						            	mMap.put("TextView3", mMap.get("TextView3") + "    未同步");
						            	
						            mList.add(mMap); 
					            }else{
					            	int keyid = Integer.valueOf(ListData.get(i).get("keyid")).intValue();
									Mydb.delete(keyid);
					            }
					        }
						}
						UIHandler.obtainMessage(MSG_UI_UPDATELISTVIEW).sendToTarget();
						break;
					}
					case MSG_NET_DELETEDATA:
						String delfiles = (String)msg.obj;
						Map<String, String> dInfo = new HashMap<String, String>();
						dInfo.put("username", username);
						dInfo.put("passwd", passwd);
						//此处的uid实际上是根据文件名来删除
						dInfo.put("uid", delfiles);
						String dres = HttpConnection.HttpPostAction(httpURL+"delfile.php", dInfo, "UTF-8");
						if (dres == null) {
							UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时，已经删除本地数据").sendToTarget();
						}else {
							try {
								JSONObject jsObj = new JSONObject(dres);
								String result = jsObj.getString("result");
								if (result.equals("yes")) {
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "数据删除成功").sendToTarget();
								}else {
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器出错，已经删除本地数据").sendToTarget();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错\n"+dres).sendToTarget();
							}
						}
						UIHandler.obtainMessage(MSG_UI_UPDATELISTVIEW).sendToTarget();
						break;
					case MSG_NET_UPDATEDATA:
						Map<String, String> Info5 = new HashMap<String, String>();
						Info5.put("username", username);
						Info5.put("model", "alldata");
						String res5 = HttpConnection.HttpPostAction(httpURL+"mydata.php", Info5, "UTF-8");
						if (res5 == null) {
							UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时，同步数据失败").sendToTarget();
						}else {
							try {
								JSONObject jsObj = new JSONObject(res5);
								JSONArray  jsonArr = jsObj.getJSONArray("alldata");
								ArrayList<Integer> Indexs = new ArrayList<Integer>();
								for (int i = 0; i < jsonArr.length(); i++) {
									JSONObject temp = (JSONObject) jsonArr.get(i);
//									if( mList.contains(temp.get("uid")) )
//									{
//										continue;
//									}
									boolean flag = false;
									for (int j = 0; j < mList.size(); j++) {
										
										if(mList.get(j).get("uid").equals( temp.get("uid") )){
											flag = true;
											break;
										}
									}
									if (flag) {
										continue;
									}
									Indexs.add(i);
									if(Indexs.size()>20){
										break;
									}
								}
								if(Indexs.size()>0){
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "正在从服务器同步数据到地遥宝，一共" + Indexs.size() + "近期条数据").sendToTarget();
									for (int i = 0; i < Indexs.size(); i++) {
										JSONObject temp = (JSONObject) jsonArr.get( ((Integer)Indexs.get(i)).intValue() );
										Map<String, String> Info6 = new HashMap<String, String>();
										Info6.put("username", username);
										Info6.put("model", "onedata");
										Info6.put("fileid", temp.getString("fileid"));
										String res6 = HttpConnection.HttpPostAction(httpURL+"mydata.php", Info6, "UTF-8");
										if (res6 == null) {
											UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
										}else {
											JSONObject jsonObj2 = new JSONObject(res6);
											JSONArray  jsonArr2 = jsonObj2.getJSONArray("onedata");
                                            /*
											if (jsonArr2.length() != 1) {
												UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "数据格式出错").sendToTarget();
											}
											*/
											JSONObject temp2 = (JSONObject) jsonArr2.get(0);
											String mCurrentTime = temp2.getString("uid");
											String mfileName = HttpConnection.saveBitmap(HttpConnection.StringtoBitmap(temp2.getString("image")), "LAISmart/" + SampleFolder + "/" + mCurrentTime);
											mCurrentTime = mCurrentTime.substring(0, mCurrentTime.length()-4);
											
											Map<String, String> sInfo = new HashMap<String, String>();
											sInfo.put("username", username);
											sInfo.put("imagepath", mfileName);
                                            sInfo.put("munsell", temp2.getString("munsell"));
											sInfo.put("latitude", temp2.getString("latitude"));
											sInfo.put("longitude", temp2.getString("longitude"));
											sInfo.put("date", mCurrentTime);
											sInfo.put("address", temp2.getString("address"));
											sInfo.put("duty", "-0.00");
											sInfo.put("lai", temp2.getString("lai"));
											sInfo.put("model", temp2.getString("model"));
											sInfo.put("imei", "ABCDEFGHIIFULL");
											sInfo.put("cost", temp2.getString("cost"));
											sInfo.put("uid", mCurrentTime+".jpg");
											sInfo.put("isupload", "1");
											Mydb.SaveData(sInfo);
										}
									}
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "从服务器同步数据到完成").sendToTarget();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错\n"+res5).sendToTarget();
							}
						}
						NetWorkThreadHandler.obtainMessage(MSG_NET_UPTELISTVIEW).sendToTarget();
						//UIHandler.obtainMessage(MSG_UI_UPDATELISTVIEW).sendToTarget();
						break;
					default:

						break;
					}
				}
			};
			Looper.loop();
		}
	});
	

	@Override
	public void getGetuiClientID(String ClientID) {
		// TODO Auto-generated method stub
		NetWorkThreadHandler.obtainMessage(MSG_NET_UPDATECID, ClientID).sendToTarget();
	}
	
	
	private Bitmap mCacheBitmap = null;
	private String fileName = null;
	private boolean isCalculateLAI = false;
	
	//存储照片
	public boolean storageImg(Mat src)
	{
		if (mCacheBitmap == null) {
        	mCacheBitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
		}else if (mCacheBitmap.getWidth() != src.width() || mCacheBitmap.getHeight() != src.height()) {
			mCacheBitmap.recycle();
			mCacheBitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
		}
		Utils.matToBitmap(src, mCacheBitmap);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	CurrentTime = sdf.format(new Date(System.currentTimeMillis()));
		fileName = HttpConnection.saveBitmap(mCacheBitmap, "LAISmart/" + SampleFolder + "/" + CurrentTime + ".jpg");
		if(fileName == null)
			return false;
		else 
			return true;
	}
	
	
	//退出按钮
	private long ExitTime = 0;
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (System.currentTimeMillis() - ExitTime > 2000) {
				ExitTime = System.currentTimeMillis();
	            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_LONG).show();
			}
			else {
                finish();
				System.exit(0);
			}
		}
		return false;
	};
	
	private boolean isBatch = false;
	private boolean[] isChecked = null;
	public class myDataSimpleAdapter extends SimpleAdapter {

		private LayoutInflater inflater = null;   
	    private Context context = null;
	    private List<? extends Map<String, ?>> mData = null;
	    private int LayoutResource = 0;
	    
		public myDataSimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to) 
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
			this.context = context;
			inflater = LayoutInflater.from(context);
			mData = data;
			LayoutResource = resource;
			
			isChecked = new boolean[mData.size()];
			for (int i = 0; i < isChecked.length; i++) {
				isChecked[i] = false;
			}
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder mViewHolder = null;
			if(convertView == null)
			{
				mViewHolder = new ViewHolder();
				//可以理解为从vlist获取view, 之后把view返回给ListView
				convertView = inflater.inflate(LayoutResource, null);
				mViewHolder.imgView   = (ImageView)convertView.findViewById(R.id.imgView);
				mViewHolder.TextView1 = (TextView) convertView.findViewById(R.id.TextView1);
				mViewHolder.TextView2 = (TextView) convertView.findViewById(R.id.TextView2);
				mViewHolder.TextView3 = (TextView) convertView.findViewById(R.id.TextView3);
				mViewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.mCheckBox);
				convertView.setTag(mViewHolder);
			}else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			mViewHolder.imgView.setImageBitmap((Bitmap) mData.get(position).get("imgView"));
			mViewHolder.TextView1.setText((String) mData.get(position).get("TextView1"));
			mViewHolder.TextView2.setText((String) mData.get(position).get("TextView2"));
			mViewHolder.TextView3.setText((String) mData.get(position).get("TextView3"));
			if (isBatch) {
				mViewHolder.mCheckBox.setVisibility(View.VISIBLE);
				mViewHolder.mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						// TODO Auto-generated method stub
						isChecked[position] = arg1;
					}
				});
				mViewHolder.mCheckBox.setChecked(isChecked[position]);
			}else{
				mViewHolder.mCheckBox.setVisibility(View.GONE);
			}
			
			return convertView;
		}
		
		//提取出来方便点  
	    public final class ViewHolder {  
	    	public ImageView imgView;
	        public TextView TextView1;  
	        public TextView TextView2; 
	        public TextView TextView3;
	        public CheckBox mCheckBox;
	    }
	}
	
	

	public void ShowIPinputDialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);  
	    final View DialogView = inflater.inflate(R.layout.setingmessagedialog, null);
	    final TextView mTextView = (TextView)DialogView.findViewById(R.id.ImglevelsTextView);
	    final SeekBar ImglevelsSeekBar = (SeekBar)DialogView.findViewById(R.id.ImglevelsSeekBar);
	    final EditText SampleEditText = (EditText)DialogView.findViewById(R.id.SampleEditText);
	    final EditText GEditText = (EditText)DialogView.findViewById(R.id.GEditText);
	    final EditText CostEditText = (EditText)DialogView.findViewById(R.id.CostEditText);
	    final RadioGroup radioGroup = (RadioGroup) DialogView.findViewById(R.id.radioGrop);
	    final RadioButton SaturabilityRadio = (RadioButton) DialogView.findViewById(R.id.SaturabilityRadio);
	    final RadioButton BlueRadio = (RadioButton) DialogView.findViewById(R.id.BlueRadio);
	    final RadioButton IntensityRadio = (RadioButton) DialogView.findViewById(R.id.IntensityRadio);
	    final RadioButton GreenRadio = (RadioButton) DialogView.findViewById(R.id.GreenRadio);
	    
	    mTextView.setText("压缩质量" + Imglevels);
	    ImglevelsSeekBar.setProgress(Imglevels);
	    SampleEditText.setText(SampleFolder);
	    GEditText.setText(""+(float)ParameterG);
	    if(DataCost<0){
	    	CostEditText.setText("");
	    }else {
	    	CostEditText.setText(""+DataCost);
		}
	    
	    ImglevelsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				mTextView.setText("压缩质量" + arg0.getProgress());
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}
		});
	    
	    if (FeatureMode == 1) {
	    	SaturabilityRadio.setChecked(true);
		}else if (FeatureMode == 2) {
			BlueRadio.setChecked(true);
		}else if (FeatureMode == 3) {
			IntensityRadio.setChecked(true);
		}else if (FeatureMode == 4) {
			GreenRadio.setChecked(true);
		}else {
		}
	    
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	    builder.setCancelable(false);   
	    builder.setTitle("系统参数设置");  
	    builder.setView(DialogView);  
	    builder.setPositiveButton("确认",new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialog, int whichButton) 
	            {  
	            	Imglevels = ImglevelsSeekBar.getProgress();
			        SampleFolder = SampleEditText.getText().toString();
			        ParameterG = Double.valueOf(GEditText.getText().toString());
			        if(ParameterG <= 0)
			        	ParameterG = Double.MIN_VALUE;
			        if (CostEditText.getText().toString().equals("")) {
			        	DataCost = -1;
					}else {
						DataCost = Integer.valueOf(CostEditText.getText().toString());
					}
			        
			        editor.putInt("Imglevels", Imglevels);
			        editor.putString("SampleFolder", SampleFolder);
			        editor.putFloat("ParameterG", (float) ParameterG);
			        editor.putInt("DataCost", DataCost);
			        
			        //设定向上向下拍摄
			        if (radioGroup.getCheckedRadioButtonId() == R.id.SaturabilityRadio) {
			        	FeatureMode = 1;
					}else if (radioGroup.getCheckedRadioButtonId() == R.id.BlueRadio) {
						FeatureMode = 2;
					}else if (radioGroup.getCheckedRadioButtonId() == R.id.IntensityRadio) {
						FeatureMode = 3;
					}else if (radioGroup.getCheckedRadioButtonId() == R.id.GreenRadio) {
						FeatureMode = 4;
					}else {
						FeatureMode = 0;
					}
			        editor.putInt("FeatureMode", FeatureMode);
			        editor.commit();
	            }  
	        });  
	    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {  
	    public void onClick(DialogInterface dialog, int whichButton) {  
//	        setTitle("");  
	            }  
	        });  
	    builder.show();
        		
	};
	
	
	private int previousAudioId = -1;
	private void AudioMsgPlay(int AudioId)
	{
		if (AudioId<0) 
		{
			if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			previousAudioId = AudioId;
			return;
		}
		
		if (mMediaPlayer == null) {
			mMediaPlayer = MediaPlayer.create(MainActivity.this, AudioId);
			mMediaPlayer.start();
		}else {
			if (previousAudioId == AudioId) {
				if (!mMediaPlayer.isPlaying())
				{
					mMediaPlayer.start();
				}
			}else {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
				mMediaPlayer = MediaPlayer.create(MainActivity.this, AudioId);
				mMediaPlayer.start();
			}
		}
		previousAudioId = AudioId;
	}
	
	public static native void downProcess(long matAddrRgba, int[] numOfbackground);
    
    public static native void upProcess(long matAddrRgba, int[] numOfbackground);
    
    public static native String Process(long matAddrRgba, int FeatureMode, int[] numOfbackground);


	
}
