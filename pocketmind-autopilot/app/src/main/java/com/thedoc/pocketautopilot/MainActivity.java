package com.thedoc.pocketautopilot;
import android.app.Activity;import android.content.*;import android.os.Bundle;import android.provider.Settings;import android.webkit.*;
public class MainActivity extends Activity{
 @Override public void onCreate(Bundle b){super.onCreate(b);WebView w=new WebView(this);setContentView(w);w.getSettings().setJavaScriptEnabled(true);w.getSettings().setDomStorageEnabled(true);w.setWebViewClient(new WebViewClient());w.addJavascriptInterface(new Bridge(),"Android");w.loadUrl("file:///android_asset/index.html");}
 public final class Bridge{
  @JavascriptInterface public String status(){return PokerAccessibilityService.status(MainActivity.this);}
  @JavascriptInterface public void arm(boolean v){PokerAccessibilityService.arm(MainActivity.this,v);}
  @JavascriptInterface public void clear(){PokerAccessibilityService.clearLog(MainActivity.this);}
  @JavascriptInterface public void access(){runOnUiThread(()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));}
 }
}
