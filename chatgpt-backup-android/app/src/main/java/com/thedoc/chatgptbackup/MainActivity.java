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

public class MainActivity extends Activity {
  WebView web;
  TextView status;
  Button collect, export;
  final String JS = "(function(){try{const id=(location.pathname.match(/\\/c\\/([^/?#]+)/)||[])[1];if(!id)return JSON.stringify({error:'Abra uma conversa /c/ primeiro'});const main=document.querySelector('main')||document.querySelector('[role=main]');if(!main)return JSON.stringify({error:'Conteudo ainda nao carregado'});const ms=[];main.querySelectorAll('[data-message-author-role]').forEach(n=>{const t=(n.innerText||'').trim();if(t)ms.push({role:n.getAttribute('data-message-author-role')||'unknown',text:t})});return JSON.stringify({id:id,title:document.title,url:location.origin+'/c/'+id,capturedAt:new Date().toISOString(),messages:ms,fullText:ms.length?null:(main.innerText||'').trim()})}catch(e){return JSON.stringify({error:String(e)})}})()";

  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
    LinearLayout bar=new LinearLayout(this); bar.setPadding(8,8,8,8);
    collect=new Button(this); collect.setText("Salvar conversa");
    export=new Button(this); export.setText("Exportar JSON");
    status=new TextView(this); status.setText("Entre no ChatGPT. Abra uma conversa e toque Salvar conversa.");
    bar.addView(collect,new LinearLayout.LayoutParams(0,-2,1)); bar.addView(export,new LinearLayout.LayoutParams(0,-2,1));
    root.addView(bar); root.addView(status);
    web=new WebView(this); root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setUserAgentString(s.getUserAgentString()+" ChatGPTBackup/1.0");
    CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
    web.setWebViewClient(new WebViewClient());
    web.setWebChromeClient(new WebChromeClient());
    web.loadUrl("https://chatgpt.com/");
    collect.setOnClickListener(v->capture());
    export.setOnClickListener(v->exportData());
  }

  void capture(){
    web.evaluateJavascript(JS, val -> {
      try {
        String json = val;
        if(json.startsWith("\"")&&json.endsWith("\"")) json = unquote(json);
        if(json.contains("\"error\"")) { status.setText(json); return; }
        org.json.JSONObject o=new org.json.JSONObject(json);
        String id=o.getString("id");
        getSharedPreferences("backup",MODE_PRIVATE).edit().putString("c_"+id,json).apply();
        int n=count(); status.setText("Salvas: "+n+" conversas. Abra a próxima conversa e toque novamente.");
      } catch(Exception e){ status.setText("Erro: "+e.getMessage()); }
    });
  }

  String unquote(String s) throws Exception { return new org.json.JSONArray("["+s+"]").getString(0); }
  int count(){ int n=0; for(String k:getSharedPreferences("backup",MODE_PRIVATE).getAll().keySet()) if(k.startsWith("c_"))n++; return n; }

  void exportData(){
    try{
      org.json.JSONArray arr=new org.json.JSONArray();
      for(Object v:getSharedPreferences("backup",MODE_PRIVATE).getAll().values()) if(v instanceof String) arr.put(new org.json.JSONObject((String)v));
      org.json.JSONObject out=new org.json.JSONObject(); out.put("total",arr.length()); out.put("conversations",arr);
      File dir=getExternalFilesDir(null); File f=new File(dir,"chatgpt-conversas.json");
      try(FileOutputStream os=new FileOutputStream(f)){os.write(out.toString(2).getBytes(StandardCharsets.UTF_8));}
      Intent i=new Intent(Intent.ACTION_SEND); i.setType("application/json");
      Uri u=androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".provider",f);
      i.putExtra(Intent.EXTRA_STREAM,u); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(Intent.createChooser(i,"Salvar/compartilhar backup"));
    }catch(Exception e){ status.setText("Erro ao exportar: "+e.getMessage()); }
  }
  @Override public void onBackPressed(){ if(web.canGoBack())web.goBack(); else super.onBackPressed(); }
}
