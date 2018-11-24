package ymc.p2ptransmit.wifiManager;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ymc.p2ptransmit.R;
import ymc.p2ptransmit.random.RandomNumOrLetter;
import ymc.p2ptransmit.view.CircleProgressView;

public class OpenApActivity extends Activity{
    private Button open;
    private Handler handler;
    private String SSID;
    private TextView txtSSID, txtKey;
    private WifiManager wifiManager;
    @SuppressLint("HandlerLeak") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_ap_activity);
        txtSSID = (TextView) findViewById(R.id.SSID);
        txtKey = (TextView) findViewById(R.id.Key);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        SSID = bundle.getString("SSID");
        txtSSID.setText(SSID);
        //txtSSID.setText(((TextView) findViewById(R.id.Lan_SSID)).getText().toString());
        String key=bundle.getString("password");
        //key=((TextView) findViewById(R.id.Lan_password)).getText().toString();
        txtKey.setText(key);
        //wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager =(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        String message =  msg.obj.toString();
                        if(!message.equals("Success"))
                        {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

        };

        final CircleProgressView mCircleBar = (CircleProgressView)findViewById(R.id.circlebar_open_ap);
        mCircleBar.setMaxProgress(100);
        mCircleBar.setProgress(0);
        mCircleBar.setmTxtHint1("热点开启中");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while(i < 100)
                {
                    i ++;
                    mCircleBar.setProgressNotInUiThread(i);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message.obtain(handler, 0 , "热点开启成功，即将返回初界面").sendToTarget();
                //Toast.makeText(getApplicationContext(), "热点开启成功，即将返回初界面", Toast.LENGTH_SHORT).show();
            }
        }).start();

        Timer t = new Timer();            //延迟4秒后自动退出界面（配置热点需要一定时间）
        class MyTask extends TimerTask {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra("apname", txtSSID.getText().toString());
                intent.putExtra("presharedkey", txtKey.getText().toString());
                setResult(2, intent);
                finish();
            }
        }
        t.schedule(new MyTask(), 4000);
        setWifiApEnabled();
        wifiManager = null;
    }



    /*public boolean setWifiApEnabled() {
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
        { //上述2种状态分别是：wifi正在打开、wifi已经打开
            wifiManager.setWifiEnabled(false);
        }
        if(ApManager.isWifiApEnabled(wifiManager))
        {
            Log.i("关闭热点", ApManager.closeWifiAp(wifiManager));
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称
            apConfig.SSID = txtSSID.getText().toString();
            //配置热点的密码
            apConfig.preSharedKey = txtKey.getText().toString();
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            //热点的加密方式
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            //通过反射调用设置热点(android本身没有开放修改热点设置的api)
            //Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.class);//https://blog.csdn.net/guang_liang_/article/details/55224675

            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, true);
        } catch (Exception e) {
            return false;
        }
    }*/

    public boolean setWifiApEnabled() {
        Method method;
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
        { //上述2种状态分别是：wifi正在打开、wifi已经打开
            wifiManager.setWifiEnabled(false);
        }
        if(ApManager.isWifiApEnabled(wifiManager))
        {
            Log.i("关闭热点", ApManager.closeWifiAp(wifiManager));
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称
            apConfig.SSID = txtSSID.getText().toString();
            //配置热点的密码
            apConfig.preSharedKey = txtKey.getText().toString();
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            //热点的加密方式
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            //通过反射调用设置热点(android本身没有开放修改热点设置的api)
            //Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            if (Build.VERSION.SDK_INT >= 26) {
                Method configMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                boolean isConfigured = (Boolean) configMethod.invoke(wifiManager, apConfig);

                method = wifiManager.getClass().getMethod("startSoftAp", WifiConfiguration.class);
                //返回热点打开状态
                //return (Boolean) method.invoke(wifiManager, apConfig);
            } else {
                //通过反射调用设置热点
                method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);//https://blog.csdn.net/guang_liang_/article/details/55224675
                //返回热点打开状态
                //return (Boolean) method.invoke(wifiManager, apConfig, true);
            }

            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, true);
        } catch (Exception e) {
            return false;
        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



}
