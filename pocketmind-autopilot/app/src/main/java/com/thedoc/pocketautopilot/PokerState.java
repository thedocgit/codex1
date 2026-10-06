package com.thedoc.pocketautopilot;
import java.util.*;
public final class PokerState{
 public final List<String> hero=new ArrayList<>(),board=new ArrayList<>();public final List<Action> actions=new ArrayList<>();public int pot=-1,stack=-1;public String hash="";
 public boolean actionable(){return hero.size()==2&&!actions.isEmpty();}
 public String signature(){return hash+hero+board+pot+stack+actions;}
 public String summary(){return "cartas="+hero+" mesa="+board+" pote="+pot+" stack="+stack+" ações="+actions;}
 public static final class Action{public enum K{CHECK,CALL,BET,RAISE,FOLD,ALLIN}public final K k;public final int n;public final String cmd;public Action(K k,int n,String c){this.k=k;this.n=n;this.cmd=c;}public String toString(){return cmd;}}
}
