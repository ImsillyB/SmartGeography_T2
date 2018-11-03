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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

public class NearbyActivity extends Activity {

	private String username = null;
	private String passwd = null;
	private double MyLatitude = 0.0;
	private double MyLongitude = 0.0;
	private String action = null;
	private String httpURL = HttpConnection.httpURL;;
	private String keyWords = null;
	
	private ListView mListView = null;
	private MapView mBaiduMapView = null;
	private BaiduMap mBaiduMap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		
		mListView = (ListView)findViewById(R.id.listview);
		mBaiduMapView = (MapView) findViewById(R.id.mBaiduMapView); 
        mBaiduMap	  = mBaiduMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(14));
		
		Intent thisIntent = getIntent();
		Bundle IntentBundle = thisIntent.getExtras();
		
		username	= IntentBundle.getString("username");
		passwd	= IntentBundle.getString("passwd");
		MyLatitude = IntentBundle.getDouble("MyLatitude");
		MyLongitude = IntentBundle.getDouble("MyLongitude");
		action = IntentBundle.getString("action");
		keyWords = IntentBundle.getString("keyWords");
		
		this.setTitle(action);
		
		//username = "wenquan0001";
		
		if (action.equals("附近的用户") || action.equals("附近的数据")|| action.equals("查找用户")|| action.equals("查找数据")) {
			mBaiduMapAddOverlay(MyLatitude, MyLongitude, R.drawable.ico_markerm, username);
			//设定marker单击事件
			mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker arg0) {
					// TODO Auto-generated method stub
					mBaiduMapView.setVisibility(View.GONE);
					return false;
				}
			});
		}else {
			mBaiduMapView.setVisibility(View.GONE);
		}
		 
		NetWorkThreadStart();
	}
	
	
	private int offset = 0;
	//下面是数据映射关系,mFrom和mTo按顺序一一对应  
	private String[] mFrom = new String[]{"imgView", "TextView1", "TextView2", "TextView3"};  
	private int[] mTo = new int[]{R.id.imgView, R.id.TextView1, R.id.TextView2, R.id.TextView3};
	//获取数据,这里随便加了10条数据,实际开发中可能需要从数据库或网络读取  
    private List<Map<String,Object>> mList = new ArrayList<Map<String,Object>>();
	
	
	
	private static final int MSG_UI_UPTELIST = 100;		//获取图片失败的标识
	private static final int MSG_UI_DISPLAYMSG = 102;	//Toast显示信息
	private static final int MSG_UI_CHANGEDTITLE = 103;	//修改标题
	
	private Handler UIHandler = new Handler() 
	{  
        public void handleMessage (Message msg) 
        {
        	//此方法在ui线程运行  
            switch(msg.what) 
            { 
            case MSG_UI_UPTELIST:  
            	if (action.equals("附近的用户") || action.equals("查找用户")) {
            		for (int i = 0; i < mList.size(); i++) {
            			double latitude  = (Double)mList.get(i).get("latitude");
            			double longitude = (Double)mList.get(i).get("longitude");
            			mBaiduMapAddOverlay(latitude, longitude, R.drawable.icon_marke, (String)mList.get(i).get("reproman"));//
					}
            	}else if(action.equals("附近的数据") || action.equals("查找数据")){
            		for (int i = 0; i < mList.size(); i++) {
            			double latitude  = (Double)mList.get(i).get("latitude");
            			double longitude = (Double)mList.get(i).get("longitude");
            			mBaiduMapAddOverlay(latitude, longitude, R.drawable.icon_markd, (String)mList.get(i).get("reproman"));//
					}
            	}else {
            		
				}
            	//创建适配器  
            	mySimpleAdapter mAdapter = new mySimpleAdapter(NearbyActivity.this, mList, R.layout.nearby_listview_item, mFrom, mTo);
                mListView.setAdapter(mAdapter);
            	findViewById(R.id.waitProgressBar).setVisibility(View.INVISIBLE);
            	break;
            case MSG_UI_DISPLAYMSG:
            	Toast.makeText(NearbyActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
            	break;
            case MSG_UI_CHANGEDTITLE:
            	NearbyActivity.this.setTitle((String)msg.obj);
            	break;
            default:
            	Toast.makeText(NearbyActivity.this, "未知触发", Toast.LENGTH_SHORT).show();
            	break;
            }  
        }  
    };

	
	//网络线程启动
	public void NetWorkThreadStart()
	{
		isRuning = true;
		if (!NetWorkThread.isAlive()) {
			NetWorkThread.start();
		}
	}
	
	//网络线程退出
	public void NetWorkThreadExit()
	{
		isRuning = false;
		if (NetWorkThreadHandler != null) {
			NetWorkThreadHandler.getLooper().quit();
		}
	}
	
	private boolean isRuning = false;
	private boolean NetWorkThreadIsBusy	 = false;
	private Handler NetWorkThreadHandler = null;
			
	private static final int MSG_NET_ASKACTION = 100;
	private static final int MSG_NET_AGREEACTION = 101;
	private static final int MSG_NET_REFUSEACTION = 102;
	
	
	//网络线程
	private Thread  NetWorkThread = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(isRuning)
			{
				String res = null;
				Map<String, String> mInfo = new HashMap<String, String>();
				mInfo.put("username", username);
				mInfo.put("radius", ""+300);
				
				if (action.equals("附近的用户")) {
					mInfo.put("model", "user");
					res = HttpConnection.HttpPostAction(httpURL+"nearby.php", mInfo, "UTF-8");
				}else if(action.equals("查找用户")){
					mInfo.put("model", "user");
					mInfo.put("likestring", keyWords);
					res = HttpConnection.HttpPostAction(httpURL+"checkdata.php", mInfo, "UTF-8");
				}else if (action.equals("附近的数据")) {
					mInfo.put("model", "data");
					res = HttpConnection.HttpPostAction(httpURL+"nearby.php", mInfo, "UTF-8");
				}else if(action.equals("查找数据")){
					mInfo.put("model", "address");
					mInfo.put("likestring", keyWords);
					res = HttpConnection.HttpPostAction(httpURL+"checkdata.php", mInfo, "UTF-8");
				}else if (action.equals("谁申请查看我的数据")) { 
					mInfo.put("model", "all");
					res = HttpConnection.HttpPostAction(httpURL+"interactcheck.php", mInfo, "UTF-8");
				}else if (action.equals("我的申请")) {
					res = HttpConnection.HttpPostAction(httpURL+"myapplication.php", mInfo, "UTF-8");
				}else{
					UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "模式错误").sendToTarget();
					break;
				}
				isRuning =  false;
				
				
				if (res == null) {
					UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
				}else {
					//UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, res).sendToTarget();
					try {
						JSONObject jsonObj = new JSONObject(res);
						if (action.equals("附近的用户") || action.equals("查找用户")) {
							JSONArray jsonArr = jsonObj.getJSONArray("nearbyuser");
							Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user);
							if (jsonArr.length()<=0) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "未找到符合条件的用户").sendToTarget();
							}
							for (int i = 0; i < jsonArr.length(); i++) {
								JSONObject temp = (JSONObject) jsonArr.get(i);
								Map<String,Object> mMap = new HashMap<String,Object>();
								mMap.put("imgView", bm);  
					            mMap.put("TextView1", temp.getString("username")); 
					            mMap.put("TextView2", "");
					            mMap.put("TextView3", "距离:"+temp.getString("distance")+"m");
					            mMap.put("reproman", temp.getString("username")); 
					            mMap.put("longitude", temp.getDouble("longitude"));
					            mMap.put("latitude",  temp.getDouble("latitude"));
					            mList.add(mMap);
							}
						}else if (action.equals("附近的数据") || action.equals("查找数据")) {
							JSONArray jsonArr = jsonObj.getJSONArray("nearbydata");
							if (jsonArr.length()<=0) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "未找到符合条件的数据").sendToTarget();
							}
							for (int i = 0; i < jsonArr.length(); i++) {
								JSONObject temp = (JSONObject) jsonArr.get(i);
								Map<String,Object> mMap = new HashMap<String,Object>();
								mMap.put("imgView", HttpConnection.StringtoBitmap(temp.getString("image")));  
					            mMap.put("TextView1", temp.getString("username")); 
					            mMap.put("TextView2", temp.getString("address")+"  距我:"+temp.getString("distance")+"m");  
						        mMap.put("TextView3", "LAI:"+temp.getString("lai"));
						        mMap.put("fileid", temp.getString("fileid"));
						        mMap.put("reproman", temp.getString("username")); 
					            mMap.put("longitude", temp.getDouble("longitude"));
					            mMap.put("latitude",  temp.getDouble("latitude"));
					            mMap.put("cost",  temp.getInt("cost"));
					            mList.add(mMap);
							}
						}else if (action.equals("谁申请查看我的数据")) {
							JSONArray jsonArr = jsonObj.getJSONArray("all");
							Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user);
							for (int i = 0; i < jsonArr.length(); i++) {
								JSONObject temp = (JSONObject) jsonArr.get(i);
								Map<String,Object> mMap = new HashMap<String,Object>();
								mMap.put("imgView", bm);
					            mMap.put("TextView1", temp.getString("proman"));
					            mMap.put("TextView2", "时间:"+temp.getString("date"));
				            	mMap.put("TextView3", temp.getString("message"));
				            	mMap.put("proman", temp.getString("proman"));
				            	mMap.put("fileid", temp.getString("fileid"));
					            mMap.put("model", temp.getString("model"));
					            
					            mList.add(mMap);
							}
						}else if (action.equals("我的申请")) {
							JSONArray jsonArr = jsonObj.getJSONArray("myapplication");
							Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user);
							for (int i = 0; i < jsonArr.length(); i++) {
								JSONObject temp = (JSONObject) jsonArr.get(i);
								Map<String,Object> mMap = new HashMap<String,Object>();
					            mMap.put("TextView1", temp.getString("reproman"));
					            mMap.put("reproman", temp.getString("reproman"));
					            mMap.put("mode", temp.getString("mode"));
					            if (temp.getString("mode").equals("alldata")) {
					            	mMap.put("imgView", bm);  
					            	mMap.put("TextView2", "您申请查看该用户所有数据");
					            	mMap.put("TextView3", "");
								}else if (temp.getString("mode").equals("onedata")){
									//mMap.put("imgView", bm);  
									mMap.put("imgView", HttpConnection.StringtoBitmap(temp.getString("image")));
					            	mMap.put("TextView2", "您申请查看该用户的一条数据");
					            	mMap.put("TextView3", "数据地址:"+temp.getString("address"));
					            	mMap.put("fileid", temp.getString("fileid"));
								}else if (temp.getString("mode").equals("onedata_cost")){
									mMap.put("imgView", HttpConnection.StringtoBitmap(temp.getString("image")));
					            	mMap.put("TextView2", "您已经购买该用户的该条数据");
					            	mMap.put("TextView3", "数据地址:"+temp.getString("address"));
					            	mMap.put("fileid", temp.getString("fileid"));
								}else {
									
								}
					            mMap.put("model", temp.getString("model"));
					            mList.add(mMap);
							}
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析异常\n"+res).sendToTarget();
					} 
				}
			}
			UIHandler.obtainMessage(MSG_UI_UPTELIST).sendToTarget();
			
			/**********************************接下来进入消息循环****************************/
			Looper.prepare();
			NetWorkThreadHandler = new Handler()
			{
				public void handleMessage (Message msg)
				{
					switch(msg.what)
					{
					case MSG_NET_ASKACTION:
						NetWorkThreadIsBusy = true;
						if (action.equals("附近的用户") || action.equals("查找用户")) {
							Map<String, String> mInfo = new HashMap<String, String>();
							mInfo.put("username", username);
							mInfo.put("reproman", (String)mList.get(msg.arg1).get("TextView1"));
							mInfo.put("model", "alldata");
							String res = HttpConnection.HttpPostAction(httpURL+"interact.php", mInfo, "UTF-8");
							if (res == null) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
							}else {
								try {
									JSONObject jsonObj = new JSONObject(res);
									if (jsonObj.getString("result").equals("yes")) {
										UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, jsonObj.getString("message")).sendToTarget();
									}else if(jsonObj.getString("result").equals("no")){
										UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "NO\n"+jsonObj.getString("message")).sendToTarget();
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析异常\n"+res).sendToTarget();
								}
							}
						}else if (action.equals("附近的数据") || action.equals("查找数据")) {
							Map<String, String> mInfo = new HashMap<String, String>();
							int cost = (Integer)mList.get(msg.arg1).get("cost");
							if (cost < 0) {
								mInfo.put("model", "onedata");
							}else {
								mInfo.put("model", "onedata_cost");
								mInfo.put("cost", ""+cost);
							}
							mInfo.put("username", username);
							mInfo.put("reproman", (String)mList.get(msg.arg1).get("reproman"));
							mInfo.put("fileid", (String)mList.get(msg.arg1).get("fileid"));
							String res = HttpConnection.HttpPostAction(httpURL+"interact.php", mInfo, "UTF-8");
							if (res == null) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
							}else {
								try {
									JSONObject jsonObj = new JSONObject(res);
									if (jsonObj.getString("result").equals("yes")) {
										UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, jsonObj.getString("message")).sendToTarget();
										if (cost >= 0)
											LookOtherData(msg.arg1);
									}else if(jsonObj.getString("result").equals("no")){
										UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "NO\n"+jsonObj.getString("message")).sendToTarget();
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析异常\n"+res).sendToTarget();
								}
							}
						}else if (action.equals("我的申请")) {
							//查看一条数据失败，则说明是查看所有数据
							if (!LookOtherData(msg.arg1)) {
								if(LookSomebodyAllData(msg.arg1))
									UIHandler.obtainMessage(MSG_UI_UPTELIST).sendToTarget();
							}
						}else if (action.equals("查看某人所有数据")){
							if (!LookOtherData(msg.arg1)) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "未知原因失败").sendToTarget();
							}
						}else {
							UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "模式错误").sendToTarget();
						}
						NetWorkThreadIsBusy = false;
						break;
					case MSG_NET_AGREEACTION:
						if (action.equals("谁申请查看我的数据")) {
							Map<String, String> mInfo = new HashMap<String, String>();
							mInfo.put("username", username);
							mInfo.put("proman", (String)mList.get(msg.arg1).get("proman"));
							mInfo.put("fileid", (String)mList.get(msg.arg1).get("fileid"));
							mInfo.put("model", "agree");
							String res = HttpConnection.HttpPostAction(httpURL+"interactdeal.php", mInfo, "UTF-8");
							if (res == null) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
							}
						}else {
							UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "模式错误").sendToTarget();
						}
						break;
					case MSG_NET_REFUSEACTION:
						if (action.equals("谁申请查看我的数据")) {
							Map<String, String> mInfo = new HashMap<String, String>();
							mInfo.put("username", username);
							mInfo.put("proman", (String)mList.get(msg.arg1).get("proman"));
							mInfo.put("fileid", (String)mList.get(msg.arg1).get("fileid"));
							mInfo.put("model", "refuse");
							String res = HttpConnection.HttpPostAction(httpURL+"interactdeal.php", mInfo, "UTF-8");
							if (res == null) {
								UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
							}
						}else {
							UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "模式错误").sendToTarget();
						}
						break;
					default:

						break;
					}
				}
			};
			Looper.loop();
		}
	});
	
	//查看别人所有的数据
	public boolean LookSomebodyAllData(int index){
		if(index > mList.size()-1)
			return false;
		Map<String, String> mInfo = new HashMap<String, String>();
		mInfo.put("username", username);
		mInfo.put("reproman", (String)mList.get(index).get("reproman"));
		mInfo.put("model", "alldata");
//		mInfo.put("fileid", (String)mList.get(index).get("fileid"));
		String res = HttpConnection.HttpPostAction(httpURL+"otherdata.php", mInfo, "UTF-8");
		if (res == null) {
			UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
		}else {
			try {
				JSONObject jsonObj = new JSONObject(res);
				JSONArray  jsonArr = jsonObj.getJSONArray("alldata");
				if (jsonArr.length()>0) {
					UIHandler.obtainMessage(MSG_UI_CHANGEDTITLE, mInfo.get("reproman")+"的所有数据").sendToTarget();
					action = "查看某人所有数据";
					mList.clear();
				}
				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject temp = (JSONObject) jsonArr.get(i);
					Map<String,Object> mMap = new HashMap<String,Object>();
					mMap.put("imgView", HttpConnection.StringtoBitmap(temp.getString("image")));  
		            mMap.put("TextView1", temp.getString("username")); 
		            mMap.put("TextView2", temp.getString("address"));  
			        mMap.put("TextView3", "LAI:"+temp.getString("lai"));
			        mMap.put("fileid", temp.getString("fileid"));
			        mMap.put("reproman", temp.getString("username")); 
		            mMap.put("longitude", temp.getDouble("longitude"));
		            mMap.put("latitude",  temp.getDouble("latitude"));
		            mMap.put("cost",  temp.getInt("cost"));
		            mList.add(mMap);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错\n"+res).sendToTarget();
			}
		}
		return true;
	}
	
	
	//查看别人大图数据
	public boolean LookOtherData(int index){
		if(index > mList.size()-1)
			return false;
		if (!mList.get(index).containsKey("fileid"))
			return false;
		Map<String, String> mInfo = new HashMap<String, String>();
		mInfo.put("username", username);
		mInfo.put("reproman", (String)mList.get(index).get("reproman"));
		mInfo.put("model", "onedata");
		mInfo.put("fileid", (String)mList.get(index).get("fileid"));
		String res = HttpConnection.HttpPostAction(httpURL+"otherdata.php", mInfo, "UTF-8");
		if (res == null) {
			UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "服务器连接超时").sendToTarget();
		}else {
			try {
				JSONObject jsonObj = new JSONObject(res);
				JSONArray  jsonArr = jsonObj.getJSONArray("onedata");
				if (jsonArr.length() != 1) {
					UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "数据格式出错").sendToTarget();
				}
				JSONObject temp = (JSONObject) jsonArr.get(0);
				Intent intent = new Intent();
        		intent.putExtra("username", username);
        		intent.putExtra("passwd", passwd);
        		intent.putExtra("MyLatitude", MyLatitude);
        		intent.putExtra("MyLongitude", MyLongitude);
        		intent.putExtra("lai", 		(String)temp.get("lai"));
        		intent.putExtra("address", 	(String)temp.get("address"));
        		intent.putExtra("model", 	(String)temp.get("model"));
        		Bitmap mBitmap = HttpConnection.StringtoBitmap((String)temp.get("image"));
        		intent.putExtra("imagepath", HttpConnection.saveBitmap(mBitmap, "LAISmart/tmp/tmpview.tmp"));
//        		UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, (String)temp.get("image")).sendToTarget();
//        		intent.putExtra("imagedata",(String)temp.get("image"));		//(String)mList.get(arg2).get("imagepath")
        		intent.setClass(getApplicationContext(), DataViewActivity.class);
				startActivity(intent);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				UIHandler.obtainMessage(MSG_UI_DISPLAYMSG, "JSON数据解析出错").sendToTarget();
			}
		}
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			NetWorkThreadExit();
	        finish();
		}
		return false;
	};
	
	
	private void mBaiduMapAddOverlay(double Latitude, double Longitude, int resource, String msg){
		//定义Maker坐标点  
		LatLng LocationP = new LatLng(Latitude, Longitude);  
		//构建Marker图标  
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(resource);  
		//构建MarkerOption，用于在地图上添加Marker  
		OverlayOptions option = new MarkerOptions().position(LocationP).icon(bitmap);  
		//构建文字Option对象，用于在地图上添加文字  
		OverlayOptions textOption = new TextOptions()  
		    .bgColor(0xAAFFFF00)  
		    .fontSize(26)  
		    .fontColor(0xFFFF00FF)  
		    .text(msg)  
		    .rotate(0)  
		    .position(LocationP); 
		
		if (mBaiduMap != null) {
			//在地图上添加Marker，并显示  
			mBaiduMap.addOverlay(option);
			//在地图上添加该文字对象并显示  
			mBaiduMap.addOverlay(textOption);
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(MyLatitude, MyLongitude)));
		}
	}
	
	
	public class mySimpleAdapter extends SimpleAdapter {

		private LayoutInflater inflater = null;
	    private Context context = null;
	    private List<? extends Map<String, ?>> mData = null;
	    
		public mySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to) 
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
			this.context = context;
			inflater = LayoutInflater.from(context);
			mData = data;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder mViewHolder = null;
			if(convertView == null)
			{
				mViewHolder = new ViewHolder();
				//可以理解为从vlist获取view, 之后把view返回给ListView
				convertView = inflater.inflate(R.layout.nearby_listview_item, null);
				mViewHolder.imgView   = (ImageView)convertView.findViewById(R.id.imgView);
				mViewHolder.TextView1 = (TextView) convertView.findViewById(R.id.TextView1);
				mViewHolder.TextView2 = (TextView) convertView.findViewById(R.id.TextView2);
				mViewHolder.TextView3 = (TextView) convertView.findViewById(R.id.TextView3);
				mViewHolder.askBtn 	  = (Button) convertView.findViewById(R.id.askBtn);
				mViewHolder.agreeBtn  = (Button) convertView.findViewById(R.id.agreeBtn);
				mViewHolder.refuseBtn = (Button) convertView.findViewById(R.id.refuseBtn);
				convertView.setTag(mViewHolder);
			}else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			
			OnClickListener OnClick = new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (NetWorkThreadHandler == null) {
						Toast.makeText(NearbyActivity.this, "程序异常", Toast.LENGTH_SHORT).show();
						return;
					}
					switch (arg0.getId()) {
					case R.id.askBtn:
						if (NetWorkThreadIsBusy) {
							Toast.makeText(NearbyActivity.this, "正在下载数据，请稍候...", Toast.LENGTH_SHORT).show();
						}else {
							NetWorkThreadHandler.obtainMessage(MSG_NET_ASKACTION, position, 0).sendToTarget();
						}
//						arg0.setEnabled(false);
						break;
					case R.id.agreeBtn:
						NetWorkThreadHandler.obtainMessage(MSG_NET_AGREEACTION, position, 0).sendToTarget();
						arg0.setEnabled(false);
						break;
					case R.id.refuseBtn:
						NetWorkThreadHandler.obtainMessage(MSG_NET_REFUSEACTION, position, 0).sendToTarget();
						arg0.setEnabled(false);
						break;
					default:
						break;
					}
				}
			};
			
			if (action.equals("附近的用户") || action.equals("查找用户")) {
				mViewHolder.agreeBtn.setVisibility(View.GONE);
				mViewHolder.refuseBtn.setVisibility(View.GONE);
			}else if (action.equals("附近的数据") || action.equals("查找数据")) {
				mViewHolder.agreeBtn.setVisibility(View.GONE);
				mViewHolder.refuseBtn.setVisibility(View.GONE);
				int cost = (Integer)mData.get(position).get("cost");
				if (cost < 0) {
					mViewHolder.askBtn.setText("申请查看");
				}else if (cost == 0){
					mViewHolder.askBtn.setText("免费查看");
				}else {
					mViewHolder.askBtn.setText("积分购买");
				}
			}else if (action.equals("谁申请查看我的数据")) {
				String model = (String) mData.get(position).get("model");
				if (model.equals("未处理")) {
					mViewHolder.askBtn.setVisibility(View.GONE);
				}else if (model.equals("拒绝")) {
					mViewHolder.agreeBtn.setVisibility(View.GONE);
					mViewHolder.refuseBtn.setVisibility(View.GONE);
					mViewHolder.askBtn.setText("您已拒绝");
					mViewHolder.askBtn.setEnabled(false);
					mViewHolder.askBtn.setBackgroundColor(Color.alpha(0));
					mViewHolder.askBtn.setTextColor(Color.RED);
				}else if (model.equals("同意")) {
					mViewHolder.agreeBtn.setVisibility(View.GONE);
					mViewHolder.refuseBtn.setVisibility(View.GONE);
					mViewHolder.askBtn.setText("您已同意");
					mViewHolder.askBtn.setEnabled(false);
					mViewHolder.askBtn.setBackgroundColor(Color.alpha(0));
					mViewHolder.askBtn.setTextColor(Color.BLUE);
				}
			}else if (action.equals("我的申请")) {
				mViewHolder.agreeBtn.setVisibility(View.GONE);
				mViewHolder.refuseBtn.setVisibility(View.GONE);
				String model = (String) mData.get(position).get("model");
				if (model.equals("未处理")) {
					mViewHolder.askBtn.setText(model);
					mViewHolder.askBtn.setEnabled(false);
					mViewHolder.askBtn.setBackgroundColor(Color.alpha(0));
					mViewHolder.askBtn.setTextColor(Color.BLUE);
				}else if (model.equals("不同意")) {
					mViewHolder.askBtn.setText(model);
					mViewHolder.askBtn.setEnabled(false);
					mViewHolder.askBtn.setBackgroundColor(Color.alpha(0));
					mViewHolder.askBtn.setTextColor(Color.RED);
				}else if (model.equals("同意")) {
					mViewHolder.askBtn.setText("查看数据");
					mViewHolder.askBtn.setTextColor(Color.BLACK);
					mViewHolder.askBtn.setEnabled(true);
					mViewHolder.askBtn.setBackgroundResource(R.drawable.btnselector);
				}
			}else if (action.equals("查看某人所有数据")) {
				mViewHolder.agreeBtn.setVisibility(View.GONE);
				mViewHolder.refuseBtn.setVisibility(View.GONE);
				mViewHolder.askBtn.setText("查看数据");
			}
			
			mViewHolder.imgView.setImageBitmap((Bitmap) mData.get(position).get("imgView"));
			mViewHolder.TextView1.setText((String) mData.get(position).get("TextView1"));
			mViewHolder.TextView2.setText((String) mData.get(position).get("TextView2"));
			mViewHolder.TextView3.setText((String) mData.get(position).get("TextView3"));
			mViewHolder.askBtn.setOnClickListener(OnClick);
			mViewHolder.agreeBtn.setOnClickListener(OnClick);
			mViewHolder.refuseBtn.setOnClickListener(OnClick);
			
			return convertView;
		}
		
		//提取出来方便点  
	    public final class ViewHolder {  
	    	public ImageView imgView;
	        public TextView TextView1;  
	        public TextView TextView2; 
	        public TextView TextView3;
	        public Button 	askBtn;  
	        public Button 	agreeBtn;
	        public Button 	refuseBtn;
	    }

	}
	
	
}
