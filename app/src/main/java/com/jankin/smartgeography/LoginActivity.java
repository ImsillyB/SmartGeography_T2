package com.jankin.smartgeography;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LoginActivity extends Activity {
	
	private SharedPreferences setting = null;
	private Editor editor = null;		//获取编辑器

	private RelativeLayout RegisterLayout = null;
	private RelativeLayout LoginLayout = null;
	private RelativeLayout TempLoginLayout = null;
	
	private EditText TelEdit = null;
	private EditText VerficodeEdit = null;
	private EditText RUsernameEdit = null;
	private EditText RPasswdEdit = null;
	private EditText LUsernameEdit = null;
	private EditText LPasswdEdit = null;
	private EditText TLPhoneNumberEdit = null;
	
	

	
	private String httpURL = HttpConnection.httpURL;
	private String TelNum = "15311117371";
	private String Verficode = "666666";
	private String username = null;
	private String passwd = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		
		setting = getSharedPreferences("SystemSeting", MODE_PRIVATE);
		editor = setting.edit();			//获取编辑器
		
		username = setting.getString("username", null);
		passwd	 = setting.getString("passwd", null);
		
		RegisterLayout = (RelativeLayout) findViewById(R.id.RegisterLayout);
		LoginLayout = (RelativeLayout) findViewById(R.id.LoginLayout);
		//TempLoginLayout = (RelativeLayout) findViewById(R.id.TempLoginLayout);
		
		TelEdit			= (EditText) findViewById(R.id.TelEdit);
		//VerficodeEdit	= (EditText) findViewById(R.id.VerficodeEdit);
		RUsernameEdit	= (EditText) findViewById(R.id.UsernameEdit);
		RPasswdEdit		= (EditText) findViewById(R.id.PasswdEdit);
		LUsernameEdit	= (EditText) findViewById(R.id.LUsernameEdit);
		LPasswdEdit		= (EditText) findViewById(R.id.LPasswdEdit);
		//TLPhoneNumberEdit = (EditText) findViewById(R.id.TLPhoneNumberEdit);
		
		if (username != null) {
			LUsernameEdit.setText(username);
			RegisterLayout.setVisibility(View.INVISIBLE);
			LoginLayout.setVisibility(View.VISIBLE);
			TempLoginLayout.setVisibility(View.INVISIBLE);
		}
	}
	
	
	public void OnClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
            /*
		case R.id.GetVerficodeBtn:
			String TelEditText = TelEdit.getText().toString();
			if (TelEditText.length() == 11) 
			{
				TelNum = TelEditText;
				Verficode = "";
				for (int i = 0; i < 6; i++) {
					Verficode = Verficode + (int)(Math.random()*10);
				}
				Map<String, String> Info = new HashMap<String, String>();
				Info.put("tel", TelNum);
				Info.put("secode", Verficode);
				String res = HttpConnection.HttpPostAction(httpURL+"telsdk/telsent.php", Info, "UTF-8");
				if (res == null) {
					Toast.makeText(LoginActivity.this, "连接服务器超时", Toast.LENGTH_SHORT).show();
				}
			}else {
				Toast.makeText(LoginActivity.this, "号码格式不正确", Toast.LENGTH_SHORT).show();
			}
			break;
			*/
		case R.id.RegisterBtn:
            TelNum = TelEdit.getText().toString();
			if (TelNum.length() != 11) {
				Toast.makeText(LoginActivity.this, "手机号码不正确", Toast.LENGTH_SHORT).show();
				break;
			}
			/*
			if (Verficode==null || !VerficodeEdit.getText().toString().equals(Verficode)) {
				Toast.makeText(LoginActivity.this, "验证码不正确", Toast.LENGTH_SHORT).show();
				break;
			}
			*/
			String RUsername = RUsernameEdit.getText().toString();
			String RPasswd	= RPasswdEdit.getText().toString();
			
			if (RUsername.length() < 5) {
				Toast.makeText(LoginActivity.this, "用户名长度太短", Toast.LENGTH_SHORT).show();
				break;
			}
			if (RPasswd.length() < 5) {
				Toast.makeText(LoginActivity.this, "密码长度太短", Toast.LENGTH_SHORT).show();
				break;
			}
			
			Map<String, String> Info = new HashMap<String, String>();
			Info.put("username", RUsername);
			Info.put("passwd", RPasswd);
			Info.put("tel", TelNum);
			Info.put("cid", "16498ge46g16194616fd16");
			Info.put("email", "roboman@foxmail.com");
			Info.put("imei", "4461649846");
			Info.put("truename", "jankin");
			String res = HttpConnection.HttpPostAction(httpURL+"regist_ok.php", Info, "UTF-8");
			if (res == null) {
				Toast.makeText(LoginActivity.this, "连接服务器超时", Toast.LENGTH_SHORT).show();
			}else {
				try {
					JSONObject jsObj = new JSONObject(res);
					String result = jsObj.getString("result");
					if (result.equals("yes")) {
						Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
						username = RUsername;
						passwd = RPasswd;
						editor.putString("username", username);
						editor.putString("passwd", passwd);
						editor.putBoolean("isMember", true);
						editor.commit();
						
						Intent in = new Intent();  
		                in.putExtra("result", "login" );
		                setResult(RESULT_OK, in );
						finish();
					}else {
						String err_msg = jsObj.getString("err_msg");
						Toast.makeText(LoginActivity.this, err_msg, Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case R.id.mRegisterBtn:
			RegisterLayout.setVisibility(View.INVISIBLE);
			LoginLayout.setVisibility(View.VISIBLE);
			//TempLoginLayout.setVisibility(View.INVISIBLE);
			break;
		case R.id.LoginBtn:
			String LUsername = LUsernameEdit.getText().toString();
			String LPasswd	= LPasswdEdit.getText().toString();
			Map<String, String> mInfo = new HashMap<String, String>();
			mInfo.put("username", LUsername);
			mInfo.put("passwd", LPasswd);
			String mres = HttpConnection.HttpPostAction(httpURL+"login_check.php", mInfo, "UTF-8");
			if (mres == null) {
				Toast.makeText(LoginActivity.this, "连接服务器超时", Toast.LENGTH_SHORT).show();
			}else {
				try {
					JSONObject jsObj = new JSONObject(mres);
					String result = jsObj.getString("result");
					if (result.equals("yes")) {
						Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
						username = LUsername;
						passwd = LPasswd;
						editor.putString("username", username);
						editor.putString("passwd", passwd);
						editor.putBoolean("isMember", true);
						editor.commit();
						
						Intent in = new Intent();  
		                in.putExtra("result", "login" );
		                setResult(RESULT_OK, in );
						finish();
					}else {
						Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case R.id.mLoginBtn:
			RegisterLayout.setVisibility(View.VISIBLE);
			LoginLayout.setVisibility(View.INVISIBLE);
			TempLoginLayout.setVisibility(View.INVISIBLE);
			break;
        /*
		case R.id.forgetpwdBtn:
			RegisterLayout.setVisibility(View.INVISIBLE);
			LoginLayout.setVisibility(View.INVISIBLE);
			TempLoginLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.TLBackBtn:
			RegisterLayout.setVisibility(View.INVISIBLE);
			LoginLayout.setVisibility(View.VISIBLE);
			TempLoginLayout.setVisibility(View.INVISIBLE);
			break;
		case R.id.TLoginBtn:
			String PhoneNumberText = TLPhoneNumberEdit.getText().toString();
			if (PhoneNumberText.length() == 11) 
			{
				TelNum = PhoneNumberText;
				Verficode = "";
				for (int i = 0; i < 6; i++) {
					Verficode = Verficode + (int)(Math.random()*10);
				}
				Map<String, String> TLInfo = new HashMap<String, String>();
				TLInfo.put("tel", TelNum);
				TLInfo.put("secode", Verficode);
				String TLres = HttpConnection.HttpPostAction(httpURL+"telsdk/pwdsent.php", TLInfo, "UTF-8");
				if (TLres == null) {
					Toast.makeText(LoginActivity.this, "连接服务器超时", Toast.LENGTH_SHORT).show();
				}else {
					try {
						JSONObject jsObj = new JSONObject(TLres);
						String result = jsObj.getString("result");
						if (result.equals("yes")) {
							String TLusername = jsObj.getString("username");
							LUsernameEdit.setText(TLusername);
							RegisterLayout.setVisibility(View.INVISIBLE);
							LoginLayout.setVisibility(View.VISIBLE);
							TempLoginLayout.setVisibility(View.INVISIBLE);
						}else {
							//String err_msg = jsObj.getString("err_msg");
							Toast.makeText(LoginActivity.this, "该号码未注册", Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(LoginActivity.this, "JSON数据解析出错\n"+TLres, Toast.LENGTH_SHORT).show();
					}
				}
				
			}else {
				Toast.makeText(LoginActivity.this, "号码格式不正确", Toast.LENGTH_SHORT).show();
			}
			break;
			*/
		default:
			break;
		}
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.login, menu);
//		return true;
//	}
	
	
	
	//退出按钮
	private long ExitTime = 0;
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (System.currentTimeMillis() - ExitTime > 2000) {
				ExitTime = System.currentTimeMillis();
	            Toast.makeText(LoginActivity.this, "再按一次退出程序", Toast.LENGTH_LONG).show();
			}
			else {
				
				Intent intent = new Intent(Intent.ACTION_MAIN);  
                intent.addCategory(Intent.CATEGORY_HOME);  
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
                startActivity(intent);
                
                finish();
				System.exit(0);
			}
		}
		return false;
	};
	

}
