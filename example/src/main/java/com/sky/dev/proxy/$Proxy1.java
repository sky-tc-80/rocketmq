package com.sky.dev.proxy;
import java.lang.reflect.Method;
public class $Proxy1 implements com.sky.dev.proxy.Movable{
    public $Proxy1(InvocationHandler h){
       this.h = h;
    }
     com.sky.dev.proxy.InvocationHandler h;
@Override
 public void move(){
 try {
 Method md = com.sky.dev.proxy.Movable.class.getMethod("move");
 h.invoke(this,md);
 } catch(Exception e) { e.printStackTrace();}
}}