package com.example.chenn.myxposedapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chenn.scan.android.*;

public class MainActivity extends AppCompatActivity {

    private EditText ed_gongsiid;
    private EditText ed_weixinhao;
    private EditText ed_direction;
    private TextView tv_imei;
    private EditText ed_address;
    private Button bt_ok;


    /**
     * 动态权限申请后回调
     */
    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {

        @Override
        public void onPermissionGranted(int requestCode) {

            Toast.makeText(MainActivity.this, "requestCode:"+requestCode, Toast.LENGTH_SHORT).show();
            switch (requestCode){

                case PermissionUtils.CODE_MULTI_PERMISSION:
                    TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                    @SuppressLint("MissingPermission") String szImei = TelephonyMgr.getDeviceId();
                    Const.putValues(getApplicationContext(), "imei", szImei);
                    tv_imei.setText(szImei);
                    startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class),Const.REQUEST_CODE_SCAN);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){

        ed_gongsiid = findViewById(R.id.ed_gongsiid);
        ed_weixinhao = findViewById(R.id.ed_zhifubao);
        ed_direction = findViewById(R.id.ed_direction);
        ed_address = findViewById(R.id.ed_address);

        bt_ok = findViewById(R.id.bt_ok);
        bt_ok.setEnabled(false);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Const.putValues(getApplicationContext(), "startup", "start");
                Const.putValues(getApplicationContext(), "companyid", ed_gongsiid.getText().toString().trim());
                Const.putValues(getApplicationContext(), "weixinno", ed_weixinhao.getText().toString().trim());
                Const.putValues(getApplicationContext(), "direction", ed_direction.getText().toString().trim());
                Const.putValues(getApplicationContext(), "address", ed_address.getText().toString().trim());
                MainActivity.this.moveTaskToBack(false);
            }
        });

        Button bt_scan = findViewById(R.id.bt_scan);
        bt_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                //    ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{Manifest.permission.CAMERA},1);
                //}else {
                //    startActivityForResult(new Intent(SettingsActivity.this, CaptureActivity.class),0);
                //}
                //scanerwema();
                if (Build.VERSION.SDK_INT < 23) {
                    TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                    @SuppressLint("MissingPermission") String szImei = TelephonyMgr.getDeviceId();
                    Const.putValues(getApplicationContext(), "imei", szImei);
                    tv_imei.setText(szImei);
                    startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class),Const.REQUEST_CODE_SCAN);
                    return;
                }
                requestPermission();

            }
        });

        tv_imei = findViewById(R.id.tv_imei);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Toast.makeText(getApplicationContext(),"解码结果：扫码返回\nrequestCode:"+requestCode+"\nresultCode:"+resultCode,Toast.LENGTH_SHORT).show();
        // 扫描二维码/条码回传
        if (requestCode == Const.REQUEST_CODE_SCAN && resultCode == Const.RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Const.DECODED_CONTENT_KEY);

                String[] params = content.split("-");
                if (params.length != 3){
                    Toast.makeText(getApplicationContext(),"参数必须为3\n解码结果： \n" + content,Toast.LENGTH_SHORT).show();
                    bt_ok.setEnabled(false);
                    return;
                }else{
                    bt_ok.setEnabled(true);
                }
                ed_gongsiid.setText(params[0]);
                ed_weixinhao.setText(params[1]);
                ed_address.setText((params[2]));

            }else{
                Toast.makeText(getApplicationContext(),"解码结果： 没有数据返回\n",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获取设备号，扫描二维码
     */
    private void requestPermission(){

        PermissionUtils.requestPermissions(this, new String[]{PermissionUtils.PERMISSION_CAMERA,PermissionUtils.PERMISSION_READ_PHONE_STATE}, mPermissionGrant);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        PermissionUtils.requestPermissionsResult(this, PermissionUtils.CODE_MULTI_PERMISSION, permissions, grantResults, mPermissionGrant);
    }
}
