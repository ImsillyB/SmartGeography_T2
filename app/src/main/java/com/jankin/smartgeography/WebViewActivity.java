package com.jankin.smartgeography;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewActivity extends Activity {

	private ProgressBar mProgressbar = null;
	private WebView MyWebView = null;;
	
	private String username = null;
	private String passwd = null;
	private String action = null;
	private String httpURL = HttpConnection.httpURL;
	private String WebViewUrl = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		
		
		
		Intent thisIntent = this.getIntent();
		Bundle thisBundle = thisIntent.getExtras();
		
		username= thisBundle.getString("username");
		passwd	= thisBundle.getString("passwd");
		action 	= thisBundle.getString("action");
		if(action.contains("的帮助")){
			WebViewUrl = "file:///android_asset/read_me.html";
		}else if(action.contains("的资料")){
			WebViewUrl = httpURL + "modify/modify.php?username="+username+"&passwd="+passwd;
		}else{
			WebViewUrl = "http://www.baidu.com";
		}
		
		this.setTitle(action);
		
		MyWebView = (WebView) findViewById(R.id.MyWebView);
		
		mProgressbar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
		mProgressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 10));
		MyWebView.addView(mProgressbar);
		
		//启用支持javascript
		WebSettings settings = MyWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		
		//覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
		MyWebView.setWebViewClient(new WebViewClient(){
           @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            // TODO Auto-generated method stub
	               //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
	             view.loadUrl(url);
	            return true;
	        }
		});
		MyWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                if (newProgress == 100) {
                    // 网页加载完成
                	mProgressbar.setVisibility(View.GONE);
                } else {
                    // 加载中
                	 if (mProgressbar.getVisibility() == View.GONE)
                		 mProgressbar.setVisibility(View.VISIBLE);
                	 mProgressbar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
            
        });
		
		
		MyWebView.loadUrl(WebViewUrl);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.web_view, menu);
//		return true;
//	}
	
	//改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
//            if(MyWebView.canGoBack())
//            {
//            	MyWebView.goBack();//返回上一页面
//                return true;
//            }
//            else
//            {
//                finish();
//            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
