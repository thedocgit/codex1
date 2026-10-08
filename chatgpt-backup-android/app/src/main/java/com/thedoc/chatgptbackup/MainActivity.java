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
    web.setWebChromeClient(new WebChromeClient()); web.setWebViewClient(new WebViewClient());
    web.loadUrl("https://chatgpt.com/");
    all.setOnClickListener(v->{stopped=false; runCollector();});
    pause.setOnClickListener(v->{stopped=true; web.evaluateJavascript("window.__cgptStop=true",null); status.setText("Pausado. Toque Coletar todas para retomar.");});
    export.setOnClickListener(v->exportData());
  }

  void runCollector(){
    status.setText("Iniciando coleta automática...");
    String js="(async()=>{window.__cgptStop=false;const sleep=m=>new Promise(r=>setTimeout(r,m));"+
      "async function j(u){for(let a=0;a<6;a++){let r=await fetch(u,{credentials:'include'});if(r.ok)return await r.json();if(r.status==429||r.status>=500){await sleep(Math.min(30000,1000*Math.pow(2,a)));continue;}throw new Error(r.status+' '+u)}throw new Error('retry '+u)}"+
      "try{let off=0,total=0,seen=new Set();async function one(x,project){let id=x.id||x.conversation_id;if(!id||seen.has(id))return;seen.add(id);if(BackupAndroid.has(id)){total++;BackupAndroid.progress(total,-1,'retomado');return;}try{let c=await j('/backend-api/conversation/'+id);let ms=[];let mp=c.mapping||{};Object.values(mp).forEach(n=>{let m=n&&n.message;if(!m)return;let role=m.author&&m.author.role||'unknown';let p=m.content&&m.content.parts||[];let txt=p.filter(v=>typeof v==='string').join('\\n').trim();if(txt)ms.push({role:role,text:txt,create_time:m.create_time||null})});BackupAndroid.save(JSON.stringify({id:id,title:c.title||x.title||'',create_time:c.create_time||x.create_time||null,update_time:c.update_time||x.update_time||null,project_id:project||c.gizmo_id||x.gizmo_id||null,is_archived:!!(c.is_archived||x.is_archived),messages:ms}));total++;BackupAndroid.progress(total,-1,'salvo');}catch(e){BackupAndroid.error(id,String(e))}await sleep(250)}while(!window.__cgptStop){let d=await j('/backend-api/conversations?offset='+off+'&limit=100&order=updated');let a=d.items||d.conversations||[];if(!a.length)break;for(const x of a){if(window.__cgptStop)break;await one(x,null)}off+=a.length;if(a.length<100)break}if(!window.__cgptStop){let pc=null;do{let pu='/backend-api/gizmos/snorlax/sidebar?owned_only=true&conversations_per_gizmo=0'+(pc?'&cursor='+encodeURIComponent(pc):'');let pd=await j(pu);for(const wrap of (pd.items||[])){let g=wrap.gizmo&&wrap.gizmo.gizmo||wrap.gizmo||wrap;let gid=g.id;if(!gid)continue;let cc='0';do{let cd=await j('/backend-api/gizmos/'+encodeURIComponent(gid)+'/conversations?cursor='+encodeURIComponent(cc));for(const x of (cd.items||[])){if(window.__cgptStop)break;await one(x,gid)}cc=cd.cursor||null}while(cc&&!window.__cgptStop)}pc=pd.cursor||null}while(pc&&!window.__cgptStop)}BackupAndroid.done(total)}catch(e){BackupAndroid.fatal(String(e))}})();";
    web.evaluateJavascript(js,null);
  }

  class Bridge {
    @JavascriptInterface public boolean has(String id){return getSharedPreferences("backup",MODE_PRIVATE).contains("c_"+id);}
    @JavascriptInterface public void save(String json){try{JSONObject o=new JSONObject(json);getSharedPreferences("backup",MODE_PRIVATE).edit().putString("c_"+o.getString("id"),json).apply();}catch(Exception ignored){}}
    @JavascriptInterface public void progress(int n,int total,String msg){runOnUiThread(()->status.setText(total>0?"Coletadas "+n+" de "+total:"Coletadas "+n+" — "+msg));}
    @JavascriptInterface public void error(String id,String e){runOnUiThread(()->status.setText("Continuando após erro em "+id));}
    @JavascriptInterface public void fatal(String e){runOnUiThread(()->status.setText("API indisponível nesta sessão: "+e+"\nUse o ChatGPT normalmente e tente novamente."));}
    @JavascriptInterface public void done(int n){runOnUiThread(()->status.setText("Coleta concluída: "+n+" processadas; "+count()+" armazenadas. Agora toque Exportar JSON."));}
  }
  int count(){int n=0;for(String k:getSharedPreferences("backup",MODE_PRIVATE).getAll().keySet())if(k.startsWith("c_"))n++;return n;}
  void exportData(){
    try{JSONArray arr=new JSONArray();for(Object v:getSharedPreferences("backup",MODE_PRIVATE).getAll().values())if(v instanceof String)arr.put(new JSONObject((String)v));
      JSONObject out=new JSONObject();out.put("exported_at",System.currentTimeMillis());out.put("total",arr.length());out.put("conversations",arr);
      File f=new File(getExternalFilesDir(null),"chatgpt-conversas.json");try(FileOutputStream os=new FileOutputStream(f)){os.write(out.toString(2).getBytes(StandardCharsets.UTF_8));}
      Uri u=androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".provider",f);Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/json");i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Salvar backup JSON"));
    }catch(Exception e){status.setText("Erro ao exportar: "+e.getMessage());}
  }
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}