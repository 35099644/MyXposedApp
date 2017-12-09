package com.example.chenn.myxposedapp;

import android.os.Bundle;
import android.os.Message;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chenn on 2017/9/2.
 */

public class CmdReceive {

    private Socket client;

    private OutputStream outStr = null;

    public boolean isstop = false;

    private InputStream inStr = null;

    private Thread tRecv = null;
    private Thread tKeep = null;

    private static volatile CmdReceive instance = null;

    public static CmdReceive getInstance(){

        if (null == instance) {

            synchronized (CmdReceive.class) {
                if (null == instance) {

                    instance = new CmdReceive();
                }
            }
        }

        return instance;
    }

    public static byte[] assembleSendXml(String type, String content, String amount) throws UnsupportedEncodingException {

        String xmlmsg = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
                "<package><body>"+
                "<type>"+type+"</type>"+
                "<content>"+content+"</content>"+
                "<terminal>9998</terminal>"+
                "<companyid>"+Const.getValues(Const.context, "companyid")+"</companyid>"+
                "<weixinno>"+Const.getValues(Const.context, "weixinno")+"</weixinno>"+
                "<imei>"+Const.getValues(Const.context, "imei")+"</imei>"+
                "<amount>"+amount+"</amount>"+
                "<terminaltype>2</terminaltype>"+
                "<orderno>0</orderno>"+
                "<sockettype>1</sockettype>"+
                "<monthamountlimit>0</monthamountlimit>"+
                "<dayamountlimit>0</dayamountlimit>"+
                "<transcountslimit>0</transcountslimit>"+
                "<paytimestamp>0</paytimestamp>"+
                "</body></package>";

        //将int类型转为网络大字节序的4个byte
        int xmlmsg_l = xmlmsg.getBytes("utf-8").length;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.asIntBuffer().put(xmlmsg_l);
        byte[] head = bb.array();

        //将发送信息转为byte[]
        return Const.bytesMerger(head, xmlmsg.getBytes());
    }

    public void stop(){

        isstop = true;
        instance = null;

        try{
            if (null!=tKeep && tKeep.isAlive())tKeep.join();
        } catch (InterruptedException e) {
            XposedBridge.log("cnj-->tKeep close:"+e.getMessage());
            disconnect();
        } finally {
            tKeep = null;
        }

        try {
            if (null!=tRecv && tRecv.isAlive())tRecv.join(1000);
        } catch (InterruptedException e) {
            XposedBridge.log("cnj-->tRecv close:"+e.getMessage());
        }finally {
            tRecv = null;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        XposedBridge.log("cnj-->连接关闭");

    }

    /**
     * 异常中断后重新连接
     */
    private void reconnectionAfterException(){

        XposedBridge.log("cnj-->异常中断重新连接");
        Const.showMessage("异常中断重新连接");
        Message msg = Const.messageThread.getHandler().obtainMessage();
        msg.what = Const.COMMAND_RE_CONNECT;
        Const.messageThread.getHandler().sendMessage(msg);
    }

    /**
     * 重启命令接收线程
     */
    public void reCmdRecv(){

        isstop = true;

        try{
            if (tKeep.isAlive())tKeep.join();
        } catch (InterruptedException e) {
            XposedBridge.log("cnj->tKeep close:"+e.getMessage());
            disconnect();
        } finally {
            tKeep = null;
        }

        try {
            if (tRecv.isAlive())tRecv.join(1000);
        } catch (InterruptedException e) {
            XposedBridge.log("cnj->tRecv close:"+e.getMessage());
        }finally {
            tRecv = null;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        XposedBridge.log("cnj->连接重置");
        Const.showMessage("连接重置");

        start();
    }

    private void processCommand(String info) {

        String amount = "0";
        String tixianid = "";   //提现id
        String type;
        String content;
        String errmsg = null;
        try {
            info = info.replaceAll("[\u0000-\u001f]", "");
            info = info.replaceAll("\n","");
            StringReader sr = new StringReader(info);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            type = doc.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
            if ("0000".equals(type)){

                amount = doc.getElementsByTagName("amount").item(0).getFirstChild().getNodeValue();
                XposedBridge.log("cnj->获取二维码");
                Const.showMessage("接收到获取二维码命令:金额\n"+amount);
                amount = amount.trim();

                //Message msg = Const.messageThread.getHandler().obtainMessage();
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("amount", amount);
                msg.what = Const.COMMAND_CLICK_SZJE;
                msg.setData(b);
                Const.messageThread.getHandler().sendMessage(msg);
            }else if ("0005".equals(type)){

                content = doc.getElementsByTagName("content").item(0).getFirstChild().getNodeValue();
                String[] arr = content.split("\\:");
                String address = arr[0];
                String port_str = arr[1];
                int port = Integer.valueOf(port_str);
                Const.SERVER_IP = address;
                Const.SERVER_PORT = port;

                Const.showMessage("接收到IP重置命令:\n"+address);
                XposedBridge.log("address="+address+";port="+port);

                reCmdRecv();
            }else if ("0013".equals(type)) {

                XposedBridge.log("cnj-->接收到自动提现消息");
                content = doc.getElementsByTagName("pwd").item(0).getFirstChild().getNodeValue().trim();
                amount = doc.getElementsByTagName("amount").item(0).getFirstChild().getNodeValue().trim();
                tixianid = doc.getElementsByTagName("tixianid").item(0).getFirstChild().getNodeValue().trim();
                Const.showMessage("接收到自动提现命令:金额\n" + amount);

                if (content.isEmpty() || amount.isEmpty() || (content.length() != 6)) {
                    XposedBridge.log("cnj-->content is empty or amount is empty or content length isnot 6");
                    Const.showMessage("提现命令异常：\ncontent is empty or amount is empty or content length isnot 6");
                    return;
                }

                stop();

                //Message msg = Const.messageThread.getHandler().obtainMessage();
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("content", content);
                b.putString("amount", amount);
                b.putString("tixianid", tixianid);

                msg.what = Const.COMMAND_AUTO_CASH;
                msg.setData(b);
                Const.messageThread.getHandler().sendMessage(msg);

            }
            sr.close();
        } catch (SAXException e) {
            XposedBridge.log("cnj->recvCommand error:"+e.getMessage());
            errmsg = e.getMessage();
        } catch (ParserConfigurationException e) {
            XposedBridge.log("cnj->recvCommand error:"+e.getMessage());
            errmsg = e.getMessage();
        } catch (IOException e) {
            XposedBridge.log("cnj->recvCommand error:"+e.getMessage());
            errmsg = e.getMessage();
        } catch (Exception e){
            XposedBridge.log("cnj->recvCommand error:"+e.getMessage());
            errmsg = e.getMessage();
        }

        if (null != errmsg)XposedBridge.log("接收命令解析异常：\n"+errmsg);
        //if (null != errmsg)Const.showMessage("接收命令解析异常：\n"+errmsg);
    }

    public void connect() throws IOException {

        String serverip = Const.getValues(Const.context, "address").trim();
        if (serverip == null || "".equals(serverip)){
            serverip = Const.SERVER_IP;
        }
        client = new Socket(serverip, Const.SERVER_PORT);
        outStr = client.getOutputStream();
        inStr = client.getInputStream();

        tKeep = new Thread(new KeepThread());
        tKeep.start();

        tRecv = new Thread(new RecvThread());
        tRecv.start();
    }

    public void disconnect() {
        try {
            if (null != client) {client.close();client=null;}
            if (null != inStr) {inStr.close();inStr=null;}
            if (null != outStr) {outStr.close();outStr=null;}
        } catch (IOException e) {

            XposedBridge.log("cnj->关闭接收线程异常:"+e.getMessage());
        }

    }
    private class KeepThread implements Runnable {

        public void run() {

            XposedBridge.log("cnj-->启动心跳线程");

            try{

                //第一次发送
                outStr.write(assembleSendXml("0002","0","0"));

                while(!isstop){

                    try {
                        Thread.sleep(5*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    outStr.write(assembleSendXml("0002","0","0"));
                }

            } catch (UnsupportedEncodingException e) {

                XposedBridge.log("cnj->心跳:"+e.getMessage());
            } catch (IOException e) {

                XposedBridge.log("cnj->心跳:"+e.getMessage());
            } finally {

                disconnect();
            }

            XposedBridge.log("cnj-->结束心跳线程");

            if (!isstop) {
                reconnectionAfterException();
            }

        }
    }

    private class RecvThread implements Runnable {
        public void run() {
            XposedBridge.log("cnj-->启动接收线程");
            try {

                while (!isstop){

                    byte[] head = new byte[4];
                    //读四位包头
                    int number = inStr.read(head);
                    if (-1 < number) {

                        //将四字节head转换为int类型
                        ByteBuffer bb = ByteBuffer.wrap(head);
                        int recvSize = bb.order(ByteOrder.BIG_ENDIAN).getInt();
                        byte[] info = new byte[recvSize];
                        number = inStr.read(info, 0, recvSize);

                        XposedBridge.log("cnj->receive head:"+recvSize);

                        if (-1 < number){

                            //转换网络字节为string
                            String xmlinfo = Const.getStringFromSocketBytes(info);
                            XposedBridge.log("cnj->receive body:"+xmlinfo);
                            processCommand(xmlinfo);
                        }
                    }

                }
            } catch (IOException e) {
                XposedBridge.log("cnj->接收线程"+e.getMessage());
            } catch (NullPointerException e){
                XposedBridge.log("cnj->接收线程"+e.getMessage());
            }

            XposedBridge.log("cnj-->结束接收线程");

        }
    }

    public void start(){

        try {
            if (tRecv == null || (!tRecv.isAlive())) {
                isstop = false;
                connect();
            }
        } catch (IOException e) {
            XposedBridge.log("cnj-->start CmdReceive failed:" + e);
            Const.showMessage("消息处理线程启动失败：\n"+e);
            reconnectionAfterException();
        }

    }
}
