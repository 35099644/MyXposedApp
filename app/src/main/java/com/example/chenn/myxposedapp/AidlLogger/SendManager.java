package com.example.chenn.myxposedapp.AidlLogger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.chenn.myaidllogger.IMyLogger;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chenn on 2017/11/30.
 */

public class SendManager {

    private volatile IMyLogger logger;

    private static SendManager sendManager = new SendManager();

    private ServiceConnection conn;

    private SendManager(){}

    public void init(Context context){

        conn  = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                logger = IMyLogger.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                logger = null;
            }
        };

        Intent intent = new Intent();
        intent.setAction("net.chennj.hook.aidl.logservice");
        intent.setPackage("com.example.chenn.myaidllogger");
        context.bindService(intent,conn,Context.BIND_AUTO_CREATE);
        context.startService(intent);
    }

    public void sendMessage(String message){

        try {
            logger.show_message(message);
            XposedBridge.log("cnj->远程传送消息："+message);
        } catch (RemoteException e) {
            XposedBridge.log("cnj->远程传送消息异常："+e.getMessage());
        }
    }
    public static SendManager instance(){
        return sendManager;
    }

}
