package com.thedoc.chatgptbackup;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.json.*;

public class MainActivity extends Activity {
  WebView web; TextView status; Button all, pause, export;
  volatile boolean stopped=false;
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
    LinearLayout bar=new LinearLayout(this);
    all=new Button(this); all.setText("Coletar todas");
    pause=new Button(this); pause.setText("Pausar");
    export=new Button(this); export.setText("Exportar JSON");
    status=new TextView(this); status.setPadding(12,8,12,8); status.setText("Entre no ChatGPT e toque Coletar todas.");
    bar.addView(all,new LinearLayout.LayoutParams(0,-2,1)); bar.addView(pause,new LinearLayout.LayoutParams(0,-2,1)); bar.addView(export,new LinearLayout.LayoutParams(0,-2,1));
    root.addView(bar); root.addView(status);
    web=new WebView(this); root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
    CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
    web.addJavascriptInterface(new Bridge(),"BackupAndroid");
    web.setWebChromeClient(new WebChromeClient()); web.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req){
        Uri u=req.getUrl(); String h=u.getHost();
        if(h!=null && (h.equals("chatgpt.com") || h.endsWith(".chatgpt.com") || h.equals("openai.com") || h.endsWith(".openai.com"))) return false;
        try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception ignored){}
        return true;
      }
    });
    web.loadUrl("https://chatgpt.com/");
    all.setOnClickListener(v->{stopped=false; runCollector();});
    pause.setOnClickListener(v->{stopped=true; web.evaluateJavascript("window.__cgptStop=true",null); status.setText("Pausado. Toque Coletar todas para retomar.");});
    export.setOnClickListener(v->exportData());
  }

  void runCollector(){
    status.setText("Iniciando coleta automática...");
    String js=CollectorScript.CODE;
    web.evaluateJavascript(js,null);
  }

  class Bridge {
    @JavascriptInterface public boolean has(String id){return getSharedPreferences("backup",MODE_PRIVATE).contains("c_"+id);}
    @JavascriptInterface public void save(String json){try{JSONObject o=new JSONObject(json);String id=o.getString("id");getSharedPreferences("backup",MODE_PRIVATE).edit().putString("c_"+id,json).remove("e_"+id).apply();}catch(Exception ignored){}}
    @JavascriptInterface public void progress(int n,int total,String msg){runOnUiThread(()->status.setText(total>0?"Coletadas "+n+" de "+total:"Coletadas "+n+" — "+msg));}
    @JavascriptInterface public void error(String id,String e){getSharedPreferences("backup",MODE_PRIVATE).edit().putString("e_"+id,e).apply();runOnUiThread(()->status.setText("Continuando após erro em "+id));}
    @JavascriptInterface public void fatal(String e){runOnUiThread(()->status.setText("API indisponível nesta sessão: "+e+"\\nUse o ChatGPT normalmente e tente novamente."));}
    @JavascriptInterface public void done(int n){int errors=errorCount();runOnUiThread(()->status.setText(errors==0?"Coleta concluída: "+n+" processadas; "+count()+" armazenadas. Agora toque Exportar JSON.":"Coleta terminou com "+errors+" falha(s): "+count()+" armazenadas. Toque Coletar todas para tentar novamente ou Exportar JSON para auditoria."));}
  }
  int count(){int n=0;for(String k:getSharedPreferences("backup",MODE_PRIVATE).getAll().keySet())if(k.startsWith("c_"))n++;return n;}
  int errorCount(){int n=0;for(String k:getSharedPreferences("backup",MODE_PRIVATE).getAll().keySet())if(k.startsWith("e_"))n++;return n;}
  void exportData(){
    try{JSONArray arr=new JSONArray();for(java.util.Map.Entry<String,?> e:getSharedPreferences("backup",MODE_PRIVATE).getAll().entrySet())if(e.getKey().startsWith("c_")&&e.getValue() instanceof String)arr.put(new JSONObject((String)e.getValue()));
      JSONObject out=new JSONObject();JSONArray errors=new JSONArray();for(java.util.Map.Entry<String,?> e:getSharedPreferences("backup",MODE_PRIVATE).getAll().entrySet())if(e.getKey().startsWith("e_")){JSONObject x=new JSONObject();x.put("id",e.getKey().substring(2));x.put("error",String.valueOf(e.getValue()));errors.put(x);}
      out.put("schema_version",4);out.put("exported_at",System.currentTimeMillis());out.put("total",arr.length());out.put("error_count",errors.length());out.put("errors",errors);out.put("conversations",arr);
      File f=new File(getExternalFilesDir(null),"chatgpt-conversas.json");try(FileOutputStream os=new FileOutputStream(f)){os.write(out.toString(2).getBytes(StandardCharsets.UTF_8));}
      Uri u=androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".provider",f);Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/json");i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Salvar backup JSON"));
    }catch(Exception e){status.setText("Erro ao exportar: "+e.getMessage());}
  }
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}