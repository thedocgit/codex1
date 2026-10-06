package com.thedoc.pocketautopilot;
import android.content.Context;import android.content.SharedPreferences;import org.json.JSONObject;
public final class AppState{
 private static final String P="pma";private AppState(){}
 private static SharedPreferences sp(Context c){return c.getSharedPreferences(P,Context.MODE_PRIVATE);} 
 public static boolean armed(Context c){return sp(c).getBoolean("armed",false);} 
 public static void arm(Context c,boolean v){sp(c).edit().putBoolean("armed",v).apply();log(c,"Autopiloto "+(v?"ATIVADO":"DESATIVADO"));}
 public static void put(Context c,String k,String v){sp(c).edit().putString(k,v==null?"":v).apply();}
 public static String get(Context c,String k){return sp(c).getString(k,"");}
 public static synchronized void log(Context c,String m){String old=get(c,"log");String[] a=old.isEmpty()?new String[0]:old.split("\\n");StringBuilder b=new StringBuilder();for(int i=Math.max(0,a.length-59);i<a.length;i++)b.append(a[i]).append('\n');b.append(System.currentTimeMillis()).append('|').append(m.replace('\n',' '));put(c,"log",b.toString());}
 public static void clear(Context c){put(c,"log","");}
 public static String status(Context c){try{JSONObject o=new JSONObject();o.put("armed",armed(c));o.put("service",get(c,"service"));o.put("state",get(c,"state"));o.put("decision",get(c,"decision"));o.put("last",get(c,"last_action"));o.put("log",get(c,"log"));return o.toString();}catch(Exception e){return "{}";}}
}
