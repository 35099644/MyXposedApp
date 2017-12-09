package com.example.chenn.myxposedapp;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chenn on 2017/9/13.
 */

public class CmdAnswer {

    private Socket client;

    private OutputStream outStr = null;

    private InputStream inStr = null;

    private Thread tAnswer = null;

    private byte[] sndBytes;

    public CmdAnswer(byte[] sndBytes){

        this.sndBytes = sndBytes;
    }

    private void processReInfo(String info){

        String type = "";
        if (null == info){
            //接收超时
            type = "9999";
        }else {
            try {
                info = info.replaceAll("[\u0000-\u001f]", "");
                info = info.replaceAll("\n", "");
                StringReader sr = new StringReader(info);
                InputSource is = new InputSource(sr);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(is);
                type = doc.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
                type = type.trim();
            } catch (SAXException e) {
                XposedBridge.log("cnj->sendCommand error:" + e.getMessage());
            } catch (ParserConfigurationException e) {
                XposedBridge.log("cnj->sendCommand error:" + e.getMessage());
            } catch (IOException e) {
                XposedBridge.log("cnj->sendCommand error:" + e.getMessage());
            }
        }
        if ("9999".equals(type)){

            //处理传送失败
            XposedBridge.log("cnj->传送失败");
        }else{
            XposedBridge.log("cnj->传送成功");
        }
    }

    public void connect() throws IOException {

        String serverip = Const.getValues(Const.context, "address").trim();
        if (serverip == null || "".equals(serverip)){
            serverip = Const.SERVER_IP;
        }
        client = new Socket(serverip, Const.SERVER_PORT);
        outStr = client.getOutputStream();
        inStr = client.getInputStream();
    }

    private class AnswerThread implements Runnable{

        @Override
        public void run() {

            try{

                //发送报文
                outStr.write(sndBytes);
                outStr.flush();

                XposedBridge.log("cnj->cmdAnswer:"+String.valueOf(sndBytes));

                client.setSoTimeout(3000);

                byte[] head = new byte[4];
                //读四位包头
                int number = inStr.read(head);
                if (-1 < number){
                    //将四字节head转换为int类型
                    ByteBuffer bb = ByteBuffer.wrap(head);
                    int recvSize = bb.order(ByteOrder.BIG_ENDIAN).getInt();
                    byte[] info = new byte[recvSize];
                    number = inStr.read(info);

                    XposedBridge.log("cnj->cmdAnswer return size:"+recvSize);

                    if (-1 < number){
                        //转换网络字节为string
                        String xmlinfo = Const.getStringFromSocketBytes(info);
                        XposedBridge.log("cnj->cmdAnswer return info:"+xmlinfo);
                        processReInfo(xmlinfo);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start(){

        try {
            connect();
            tAnswer = new Thread(new AnswerThread());
            tAnswer.start();
        } catch (Exception e) {
            XposedBridge.log("cnj-->回复线程启动错误" + e);
        }
    }

}
