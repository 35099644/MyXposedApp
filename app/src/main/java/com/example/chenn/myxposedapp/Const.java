package com.example.chenn.myxposedapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chenn.myxposedapp.AidlLogger.SendManager;
import com.example.chenn.myxposedapp.SharePref.PreferenceUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chenn on 2017/8/28.
 */

public class Const {

    private static final boolean _Debug = true;

    //////////////////////////
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    //////////////////////////

    public static String SERVER_IP = "192.168.8.107";
    public static int SERVER_PORT = 9999;

    public static final String ZHIFUBAO_PACKAGENAME = "com.eg.android.AlipayGphone";
    public static final String LAUNCHER_UI = "com.alipay.mobile.quinox.LauncherActivity";
    public static final String SHOUQIAN_BUTTON_VIEW = "com.alipay.android.phone.home.homeheader.HomeHeadView";
    public static final String GERENSHOUQIAN_UI = "com.alipay.mobile.payee.ui.PayeeQRActivity"; //个人收钱页面
    public static final String SHEZHIJINE_UI = "com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity";  //设置金额页面
    public static final String PAYEEQR_UI = "com.alipay.mobile.payee.ui.BasePayeeQRActivity"; //二维码生成页面
    public static final String PAYCALLBACKIMPL = "com.alipay.mobile.payee.ui.k";    //二维码回调
    public static final String TRANSPAY_UI = "com.alipay.mobile.transferapp.TransferApp";    //转账页面
    public static final String ZHUANZHANG_CLICK = "com.alipay.mobile.commonui.widget.APViewEventHelper";

    public static final int COMMAND_STOP_MESSAGETHREAD = 888;   //停止消息处理线程
    public static final int COMMAND_START_COMMANDTHREAD = 101;  //启动命令接收线程
    public static final int COMMAND_CLICK_SZJE = 102;           //模拟点击设置金额按钮
    public static final int COMMAND_INPUT_SZJE = 103;           //输入金额
    public static final int COMMAND_RE_CONNECT = 107;           //改变ip，重置连接
    public static final int COMMAND_AUTO_CASH = 118;            //自动提现
    public static final int COMMAND_RETURN_ERWMA = 119;         //返回二维码
    public static final int COMMAND_PAY_SUCCESS = 120;          //支付成功返回
    public static final int COMMAND_PAY_FAILED = 121;           //支付失败返回

    public static Context context = null;
    public static MessageThread messageThread = null;           //消息处理线程
    public static ClassLoader classLoader = null;
    public static String PREFERENCE_NAME = "zfbhookpayset";      //共享文件名

    public static Activity ACTIVITY_GERENSHOUQIAN = null;   //个人收钱
    public static Activity ACTIVITY_GERENSHOUQIAN_BASE = null;//个人收钱父类
    public static Activity ACTIVITY_SHEZHIJINE = null;  //设置金额
    public static TextView view_szje = null;    //设置金额按钮
    public static EditText view_szje_input = null;    //设置金额输入框
    public static Button btn_szje_ok = null;    //设置金额确定按钮


    /*********扫描***********/

    public static final int REQUEST_CODE_SCAN       = 101;                //扫描请求码
    public static final int RESULT_OK               = -1;                //扫描返回码
    public static final String DECODED_CONTENT_KEY  = "codedContent";

    /**
     * 网络byte类型的字节不能转成string，需要进行转换
     * @param bytes
     * @return
     */
    public static String getStringFromSocketBytes(byte[] bytes){

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        InputStreamReader isr = new InputStreamReader(bis);
        BufferedReader br = new BufferedReader(isr);

        String line = ""; String result = "";
        try {
            while ((line = br.readLine()) != null){

                result += line;
            }
        } catch (IOException e) {

            XposedBridge.log("cnj->read bytes:"+e.getMessage());
        } finally{
            if (null != bis){
                try {
                    bis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (null != isr){
                try {
                    isr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (null != br){
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * 合并两个byte数组
     * @param byte_1
     * @param byte_2
     * @return
     */
    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    @Deprecated
    public static String parse(String s, String regx){

        Pattern pattern = Pattern.compile(regx);

        Matcher matcher = pattern.matcher(s);

        String result = "";

        while(matcher.find()){

            result += matcher.group(1);
        }

        return result;
    }

    @Deprecated
    static String parse(String s, String regx, String dep){

        Pattern pattern = Pattern.compile(regx,Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(s);

        String result="";

        while(matcher.find()){

            int groupcount = matcher.groupCount();
            for (int i=1; i<=groupcount; i++){

                result += matcher.group(i)+((dep==null)?"":dep);
            }
        }

        return result;
    }

    // 统计某个字符的个数
    public static int count(String s, char c) {
        int num = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                num++;
            }
        }
        return num;
    }

    private static synchronized Date getStrToDate(String format, String str) {
        simpleDateFormat.applyPattern(format);
        try {
            return simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将某指定的字符串转换为时间
     *
     * @param str
     *            将被转换为Date的字符串
     * @return 转换后的Date
     */
    @Deprecated
    public static Date getStr2Date(String str) {
        str = str.trim();
        if (str.indexOf('-') != -1 && str.indexOf(':') != -1
                && count(str, ':') == 2)
            return getStrToDate("yyyy-MM-dd HH:mm:ss", str);
        if (str.indexOf('/') != -1 && str.indexOf(':') != -1
                && count(str, ':') == 2)
            return getStrToDate("yyyy/MM/dd HH:mm:ss", str);
        if (str.indexOf('-') != -1 && str.indexOf(':') != -1
                && count(str, ':') == 1)
            return getStrToDate("yyyy-MM-dd HH:mm", str);
        if (str.indexOf('/') != -1 && str.indexOf(':') != -1
                && count(str, ':') == 1)
            return getStrToDate("yyyy/MM/dd HH:mm", str);
        if (str.indexOf('-') != -1 && str.indexOf(':') == -1)
            return getStrToDate("yyyy-MM-dd", str);
        if (str.indexOf('/') != -1 && str.indexOf(':') == -1)
            return getStrToDate("yyyy/MM/dd", str);
        return null;
    }

    public static void putValues(Context ctx, String key, String value) {
        if (null == ctx)return;
        SharedPreferences preferences = PreferenceUtil.getSharedPreference(ctx, PREFERENCE_NAME);
        preferences.edit().putString(key, value).commit();
    }

    public static String getValues(Context ctx, String key) {
        if (null == ctx)return "none";
        SharedPreferences preferences = PreferenceUtil.getSharedPreference(ctx, PREFERENCE_NAME);
        return preferences.getString(key, "none");
    }

    /***********bitmap********************/
    @Deprecated
    public static byte[] getByteArrayFromImageView(ImageView imageView){

        BitmapDrawable bitmapDrawable = (BitmapDrawable)imageView.getDrawable();
        Bitmap bitmap;

        if (null == bitmapDrawable){
            imageView.buildDrawingCache();
            bitmap = imageView.getDrawingCache();
            imageView.buildDrawingCache(false);
        }else{
            bitmap = bitmapDrawable.getBitmap();
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @Deprecated
    public static Bitmap getBitmapFromImageView(ImageView imageView){

        BitmapDrawable bitmapDrawable = (BitmapDrawable)imageView.getDrawable();
        Bitmap bitmap;

        if (null == bitmapDrawable){
            imageView.buildDrawingCache();
            bitmap = bitmapDrawable.getBitmap();
            imageView.buildDrawingCache(false);
        }else{
            bitmap = bitmapDrawable.getBitmap();
        }

        return bitmap;
    }

    public static String getDefaultCameraPath(){

        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    }

    @Deprecated
    public static void  saveBitmapToCamera(Bitmap bitmap){

        File camera_dir = new File(getDefaultCameraPath());

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "image-"+n+".jpg";
        File file = new File(camera_dir, fname);
        if (file.exists())file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            XposedBridge.log("cnj->save bitmap error\n"+e.getMessage());
        } catch (IOException e) {
            XposedBridge.log("cnj->save bitmap error\n"+e.getMessage());
        }
    }
    /***********消息处理*******************/

    /**
     * 启动消息处理线程
     * @param context
     */
    public static void startMessageThread(Context context){

        try {
            if (messageThread != null && messageThread.isAlive()) {
                XposedBridge.log("cnj-->消息处理线程已经在运行中");
                SendManager.instance().sendMessage("消息处理线程已经在运行中!");
                return;
            }
        }catch (Exception e){}
        SendManager.instance().init(Const.context);
        XposedBridge.log("cnj-->启动后台消息处理线程");
        messageThread = new MessageThread(context);
        messageThread.start();
    }

    public static void stopMessageThread(){

        try{
            if (messageThread != null && messageThread.isAlive()){
                XposedBridge.log("cnj-->消息处理线程停止中");
                Message msg = new Message();
                msg.what = COMMAND_STOP_MESSAGETHREAD;
                messageThread.getHandler().sendMessage(msg);
                messageThread = null;
            }
        } catch (Exception e){

            XposedBridge.log("cnj->消息处理线程停止异常："+e.getMessage());
        }
    }
    /**
     * 命令接收线程
     */
    public static void startCmdRecv(){

        Message msg = new Message();
        msg.what = COMMAND_START_COMMANDTHREAD;
        messageThread.getHandler().sendMessage(msg);
    }

    /**
     * 传送消息
     */
    public static void sendMessage(int what, Bundle bundle){

        if ((null == messageThread) || (!messageThread.isAlive()))return;

        Message msg = new Message();
        if (null != bundle) {
            msg.setData(bundle);
        }
        msg.what = what;
        messageThread.getHandler().sendMessage(msg);
    }

    //////////////////////////////调试信息///////////////////////////////////////
    public static void  showMessage(String msg){

        if (null != context && _Debug)
            SendManager.instance().sendMessage(msg);
    }
}
