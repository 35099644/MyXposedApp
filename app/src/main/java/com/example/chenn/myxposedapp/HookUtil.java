package com.example.chenn.myxposedapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by chenn on 2017/8/25.
 */

public final class HookUtil {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

    /**
     * 通过Hook类的名称进行内部查找
     * @param className
     * @param classLoader
     * @param methodName
     * @param parameterTypesAndCallback
     */
    public static void hook_method(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback){

        try{
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
            XposedBridge.log("cnj-->hook:"+className+"["+methodName+"]");
        }catch (Exception e){
            XposedBridge.log(e);
        }
    }

    public static void hook_method(Class<?> clazz, String methodName, Object... parameterTypesAndCallback){

        try{
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            XposedBridge.log("cnj-->hook:"+clazz.getName()+"["+methodName+"]");
        }catch (Exception e){
            XposedBridge.log(e);
        }
    }

    @Deprecated
    public static void hook_constructor(String className, ClassLoader classLoader, Object... paramterTypesAndCallback){

        try{
            XposedHelpers.findAndHookConstructor(className,classLoader,paramterTypesAndCallback);
            XposedBridge.log("cnj->hook:"+className+"[Constructor]");
        }catch (Exception e){
            XposedBridge.log(e);
        }
    }

    @Deprecated
    public static void hook_constructor(Class<?> clazz, Object... paramterTypesAndCallback){

        try{
            XposedHelpers.findAndHookConstructor(clazz,paramterTypesAndCallback);
            XposedBridge.log("cnj->hook:"+clazz.getName()+"[Constructor]");
        }catch (Exception e){
            XposedBridge.log(e);
        }
    }

    @Deprecated
    public static void hook_methods(String className, String methodName, XC_MethodHook xmh){

        try{
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods){
                method.setAccessible(true);
                if (method.getName().equals(methodName)
                        && !Modifier.isAbstract(method.getModifiers())){
                    XposedBridge.hookMethod(method, xmh);
                }
            }
        } catch (Exception e){
            XposedBridge.log(e);
        }
    }

    /** 
     * 判断某个界面是否在前台 
     *
     * @param context 
     * @param className  某个界面名称 
     */
    @Deprecated
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 通过context获取根视图
     * @param context
     * @return
     */
    @Deprecated
    public static View getRootView(Context context){

        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        return rootView;
    }

    public static String getDate2Str(String format, Date date) {
        simpleDateFormat.applyPattern(format);
        return simpleDateFormat.format(date);
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    @Deprecated
    public static void execShellCmd(String cmd){

        try{
            //申请root权限，
            Process process = Runtime.getRuntime().exec("su");
            //获取输出流
            OutputStream os = process.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeBytes(cmd);
            dos.flush();
            dos.close();
            os.close();
        } catch (IOException e) {
            XposedBridge.log(e);
        }
    }

}
