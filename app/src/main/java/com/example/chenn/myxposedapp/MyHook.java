package com.example.chenn.myxposedapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chenn.scan.erweima.ParserErwma;

import org.json.JSONArray;
import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by chenn on 2017/11/14.
 */

public class MyHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(Const.ZHIFUBAO_PACKAGENAME)){
            return;
        }

        HookUtil.hook_method(Application.class, "attach", Context.class, new XC_MethodHook(){

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                //全局加载器
                Const.context = (Context)param.args[0];
                Const.classLoader = Const.context.getClassLoader();

                /**
                 * 启动消息处理线程
                 */
                HookUtil.hook_method(Const.LAUNCHER_UI, Const.classLoader, "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->主界面完成");

                        Const.startMessageThread(Const.context);

                        //Toast.makeText((Activity)param.thisObject, "主界面完成", Toast.LENGTH_SHORT).show();
                    }
                });


                /**
                 * 停止消息处理线程
                 */
                HookUtil.hook_method(Const.LAUNCHER_UI, Const.classLoader, "onDestroy", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->主界面退出");

                        Const.stopMessageThread();
                    }
                });


                HookUtil.hook_method(Const.SHOUQIAN_BUTTON_VIEW, Const.classLoader, "jumpToCollectionMoney", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        View view = (View)param.thisObject;
                        Context context = view.getContext();
                        Activity activity = null;
                        if (context instanceof Activity){
                            activity = (Activity)context;
                        }
                        XposedBridge.log("cnj-->点击收钱,activity:"+activity.getClass().getName());
                        Const.startCmdRecv();
                        //Toast.makeText(context, "点击收钱", Toast.LENGTH_SHORT).show();
                    }
                });

                HookUtil.hook_method(Const.GERENSHOUQIAN_UI, Const.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->个人收钱界面");

                        Const.ACTIVITY_GERENSHOUQIAN = (Activity)param.thisObject;
                        Const.view_szje = (TextView)XposedHelpers.getObjectField(param.thisObject, "ah");
                        //Const.sendMessage(Const.COMMAND_CLICK_SZJE,null);

                    }
                });

                /**
                 * 获取个人收钱页面父类对象
                 */
                HookUtil.hook_method(Const.PAYEEQR_UI, Const.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->个人收钱父类");

                        Const.ACTIVITY_GERENSHOUQIAN_BASE = (Activity)param.thisObject;
                    }
                });

                HookUtil.hook_method(Const.SHEZHIJINE_UI, Const.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->设置金额界面");

                        Const.ACTIVITY_SHEZHIJINE = (Activity)param.thisObject;
                        Object obj = XposedHelpers.getObjectField(param.thisObject, "b");
                        Const.view_szje_input = (EditText)XposedHelpers.getObjectField(obj, "d");
                        Const.btn_szje_ok = (Button)XposedHelpers.getObjectField(param.thisObject, "e");
                        Const.sendMessage(Const.COMMAND_INPUT_SZJE,null);

                    }
                });

                HookUtil.hook_method(Const.SHEZHIJINE_UI, Const.classLoader, "a", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->设置金额确定按钮点击之后");

                    }
                });

                /**
                 * 设置金额后返回信息
                 */
                HookUtil.hook_method(Const.PAYEEQR_UI, Const.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->设置金额后返回\n");

                        Intent it = (Intent)param.args[2];

                        String str = "qr_money:"+it.getStringExtra("qr_money")+"\n"
                                + "beiZhu:"+it.getStringExtra("beiZhu")+"\n"
                                + "qrCodeUrl:"+it.getStringExtra("qrCodeUrl")+"\n"
                                + "qrCodeUrlOffline:"+it.getStringExtra("qrCodeUrlOffline");

                        XposedBridge.log("cnj-->返回信息:"+str);

                        Bundle b = new Bundle();
                        b.putString("amount", it.getStringExtra("qr_money"));
                        b.putString("erwma", it.getStringExtra("qrCodeUrl"));
                        Const.sendMessage(Const.COMMAND_RETURN_ERWMA,b);
                        /*
                        ImageView image = (ImageView)XposedHelpers.getObjectField(param.thisObject, "x");
                        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();

                        Toast toast = Toast.makeText(Const.context, "返回的二维码：\n", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout toastView = (LinearLayout)toast.getView();
                        ImageView imageView = new ImageView(Const.context);
                        imageView.setImageBitmap(bitmap);
                        toastView.addView(imageView,0);
                        toast.show();*/
                    }
                });

                final Class<?> clszz_QrCallback = XposedHelpers.findClass("com.alipay.mobile.payee.ui.BasePayeeQRActivity$QrcodeGenerateCallback", Const.classLoader);
                HookUtil.hook_method(Const.PAYEEQR_UI, Const.classLoader, "a", Bitmap.class, String.class, clszz_QrCallback, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        String qrurl = (String)XposedHelpers.getObjectField(Const.ACTIVITY_GERENSHOUQIAN_BASE, "k");
                        //if ("".equals(qrurl) || !qrurl.equals(param.args[1]))return;

                        //ImageView image = (ImageView)XposedHelpers.getObjectField(Const.ACTIVITY_GERENSHOUQIAN_BASE, "x");
                        //Bitmap erweima_bitmap = Const.getBitmapFromImageView(image);

                        //String result = ParserErwma.decodeBitmapToString(erweima_bitmap);

                        //Const.saveBitmapToCamera(erweima_bitmap);

                        //XposedBridge.log("cnj-->返回测试\n" +
                        //        "a(Bitmap paramBitmap, String paramString, BasePayeeQRActivity.QrcodeGenerateCallback paramQrcodeGenerateCallback)\n" +
                        //        param.args[1] + "\n" +
                        //        result);

                        //new Exception("cnj debug").printStackTrace();
                        //XposedBridge.log(new Exception("cnj debug"));

                        //Toast toast = Toast.makeText(Const.context, "图片：\n"+param.args[1]+"\n", Toast.LENGTH_LONG);
                        //toast.setGravity(Gravity.CENTER, 0, 0);
                        //LinearLayout toastView = (LinearLayout)toast.getView();
                        //ImageView imageView = new ImageView(Const.context);
                        //imageView.setImageBitmap(erweima_bitmap);
                        //toastView.addView(imageView,0);
                        //toast.show();

                    }
                });

                final Class<?> clszz_sm = XposedHelpers.findClass("com.alipay.mobile.rome.longlinkservice.syncmodel.SyncMessage",Const.classLoader);
                HookUtil.hook_method(Const.PAYEEQR_UI, Const.classLoader, "onReceiveMessage", clszz_sm, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log("cnj-->返回测试\nonReceiveMessage(SyncMessage paramSyncMessage)\n" +
                                param.args[0]);

                        String msgData = (String)XposedHelpers.getObjectField(param.args[0],"msgData");
                        JSONArray jsonArray = new JSONArray(msgData);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String js_pl = jsonObject.getString("pl");
                        JSONObject jo_pl = new JSONObject(js_pl);
                        int state = jo_pl.getInt("state");

                        if (1 == state){
                            //支付成功
                            String amount = jo_pl.getString("amount");
                            String paymsgtimestamp = jsonObject.getString("mct");
                            Bundle b = new Bundle();
                            b.putString("payinfo", msgData);
                            b.putString("pay_amount", amount);
                            b.putString("pay_time",paymsgtimestamp);
                            Const.sendMessage(Const.COMMAND_PAY_SUCCESS,b);
                        }


                    }
                });

                /////////////////////////////////////二维码回调////////////////////////////////////////////////
//                HookUtil.hook_method(Const.PAYCALLBACKIMPL, Const.classLoader, "a", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                        XposedBridge.log("cnj-->二维码回调\na()");
//
//                        ImageView imageView = (ImageView)XposedHelpers.getObjectField(param.thisObject,"b");
//                        Toast toast = Toast.makeText(Const.context, "返回的二维码：\n", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        LinearLayout toastView = (LinearLayout)toast.getView();
//                        toastView.addView(imageView,0);
//                        toast.show();
//
//                    }
//                });

                ///////////////////////////////////转账//////////////////////////////////////////////////////
                /**
                 * 点击转账
                 */
//                HookUtil.hook_method(Const.ZHUANZHANG_CLICK, Const.classLoader, "wrapClickListener", View.OnClickListener.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                        XposedBridge.log("cnj-->点击转账\nwrapClickListener(View.OnClickListener paramOnClickListener)\n" +
//                                param.args[0].getClass().getName());
//
//                        //new Exception("cnj debug").printStackTrace();
//                        //XposedBridge.log(new Exception("cnj debug"));
//                    }
//                });

//                HookUtil.hook_method(Const.TRANSPAY_UI, Const.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                        XposedBridge.log("cnj-->转账页面\nonCreate(Bundle.class)\n" +
//                                param.args[0]);
//                    }
//                });
            }
        });

    }
}
