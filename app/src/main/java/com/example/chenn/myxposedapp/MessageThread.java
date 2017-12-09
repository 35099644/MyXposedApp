package com.example.chenn.myxposedapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chenn on 2017/8/28.
 */

public class MessageThread extends Thread{

    private boolean isShutdown = false;
    private Handler handler;
    private WeakReference<Context> contextWeakReference;

    /**
     *
     * @param type      命令类型
     * @param content   命令内容
     * @param amount    金额
     * @param timestamp 时间戳
     * @param orderno   订单号
     * @param tixianid  自动提现id
     * @return
     * @throws UnsupportedEncodingException
     */
    public byte[] assembleSendXml(String type, String content, String amount, String timestamp, String orderno, String tixianid) throws UnsupportedEncodingException {

        String xmlmsg = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
                "<package><body>"+
                "<type>"+type+"</type>"+
                "<content>"+content+"</content>"+
                "<terminal>9998</terminal>"+
                "<companyid>"+Const.getValues(Const.context, "companyid")+"</companyid>"+
                "<weixinno>"+Const.getValues(Const.context, "weixinno")+"</weixinno>"+
                "<imei>"+Const.getValues(Const.context, "imei")+"</imei>"+
                "<amount>"+amount+"</amount>"+
                "<orderno>"+orderno+"</orderno>"+
                "<tixianid>"+tixianid+"</tixianid>"+
                "<terminaltype>2</terminaltype>"+
                "<sockettype>0</sockettype>"+
                "<status>1</status>"+
                "<monthamountlimit>0</monthamountlimit>"+
                "<dayamountlimit>0</dayamountlimit>"+
                "<transcountslimit>0</transcountslimit>"+
                "<paytimestamp>"+timestamp+"</paytimestamp>"+
                "</body></package>";

        XposedBridge.log("cnj->发送的信息:"+xmlmsg);

        //将int类型转为网络大字节序的4个byte
        int xmlmsg_l = xmlmsg.getBytes("utf-8").length;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.asIntBuffer().put(xmlmsg_l);
        byte[] head = bb.array();

        //将发送信息转为byte[]
        return Const.bytesMerger(head, xmlmsg.getBytes());
    }

    public MessageThread(Context context){

        XposedBridge.log("cnj-->hook start messagethread success");

        this.contextWeakReference = new WeakReference<Context>(context);

    }

    public Handler getHandler(){return handler;}

    @Override
    public void run(){

        Const.showMessage("消息处理线程开始!");

        Looper.prepare();
        handler = new MessageHandler();
        Looper.loop();
        XposedBridge.log("cnj-->消息线程退出");

        Const.showMessage("消息处理线程退出!");

        if(isShutdown)Looper.getMainLooper().quit();
    }

    class MessageHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Const.COMMAND_START_COMMANDTHREAD: {
                    Const.showMessage("命令接收处理线程开始!");
                    CmdReceive.getInstance().start();
                }
                break;

                //进入设置金额页面
                case Const.COMMAND_CLICK_SZJE:{

                    Bundle b = msg.getData();
                    final String amount = b.getString("amount");
                    Const.putValues(contextWeakReference.get(), "erwma_amount", amount);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String str = Const.view_szje.getText().toString();
                    if (!"".equals(str) && "清除金额".equals(str)){

                        Const.ACTIVITY_GERENSHOUQIAN.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Const.view_szje.performClick();
                            }
                        });

                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Const.ACTIVITY_GERENSHOUQIAN.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Const.view_szje.performClick();
                        }
                    });
                }
                break;

                //输入金额
                case Const.COMMAND_INPUT_SZJE:{

                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final String amount;
                    String tmp = Const.getValues(contextWeakReference.get(), "erwma_amount");
                    if ("none".equals(tmp)){
                        amount = "";
                    }else{
                        amount = tmp;
                    }

                    Const.ACTIVITY_SHEZHIJINE.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Const.view_szje_input.setText(amount);
                        }
                    });

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Const.ACTIVITY_SHEZHIJINE.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Const.btn_szje_ok.performClick();
                        }
                    });
                }
                break;

                //返回二维码
                case Const.COMMAND_RETURN_ERWMA:{

                    Bundle b = msg.getData();
                    String amount = b.getString("amount");
                    String content = b.getString("erwma");

                    try {
                        byte[] info = assembleSendXml("0001", content, amount, "0", "0", "0");
                        new CmdAnswer(info).start();
                    } catch (UnsupportedEncodingException e) {
                        XposedBridge.log("cnj-->传送二维码消息异常:"+e.getMessage());
                    }
                }
                break;

                //支付成功
                case Const.COMMAND_PAY_SUCCESS:{

                    Bundle b = msg.getData();
                    String content = b.getString("payinfo");
                    String pay_amount = b.getString("pay_amount");
                    String pay_time = b.getString("pay_time");

                    try {
                        //解析xml，组装content
                        byte[] info = assembleSendXml("0004", content, pay_amount, pay_time, "0", "0");
                        new CmdAnswer(info).start();
                    } catch (UnsupportedEncodingException e) {
                        XposedBridge.log("cnj->发送支付成功消息异常:"+e.getMessage());
                    }
                }
                break;

                //支付失败
                case Const.COMMAND_PAY_FAILED:{

                    Bundle b = msg.getData();
                    String content = b.getString("payinfo");
                    String pay_amount = b.getString("pay_amount");
                    String pay_time = b.getString("pay_time");

                    try {
                        //解析xml，组装content
                        byte[] info = assembleSendXml("0011", content, pay_amount, pay_time, "0", "0");
                        new CmdAnswer(info).start();
                    } catch (UnsupportedEncodingException e) {
                        XposedBridge.log("cnj->发送支付失败消息异常:"+e.getMessage());
                    }
                }
                break;

                case Const.COMMAND_RE_CONNECT:{

                    XposedBridge.log("cnj-->收到重连命令");
                    try {
                        sleep(1000*10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    CmdReceive.getInstance().start();

                }
                break;

                case Const.COMMAND_STOP_MESSAGETHREAD: {
                    CmdReceive.getInstance().stop();
                    XposedBridge.log("cnj-->消息线程停止");
                    Looper.myLooper().quit();
                }
                break;

                default:
                    break;
            }
        }
    }
}
