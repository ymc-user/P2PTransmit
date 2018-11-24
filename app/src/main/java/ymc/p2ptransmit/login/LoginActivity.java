package ymc.p2ptransmit.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import ymc.p2ptransmit.main.MainActivity;
import ymc.p2ptransmit.R;
import ymc.p2ptransmit.wifiManager.ApManager;
import ymc.p2ptransmit.wifiManager.OpenApActivity;
import ymc.p2ptransmit.wifiManager.WifiAutoConnectManager;

public class LoginActivity extends Activity{
	private Handler handler;
	private String username,password;
	private final static int TOASTMESSAGE = 0;
	private final static int WIFILINKED = 1;
	private boolean isWifiLinked;
	private String ssid, wifiPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			checkPermission();
		}
		final ImageView imgScanQRCode = (ImageView)findViewById(R.id.img_scan_qrcode);
		imgScanQRCode.setImageResource(R.drawable.scan_qrcode);
		imgScanQRCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				new IntentIntegrator(LoginActivity.this).initiateScan();
			}
		});
		final EditText editUserName = (EditText)findViewById(R.id.edit_name);
        final EditText editPassword = (EditText)findViewById(R.id.edit_password);
		final ImageButton btnOpenHotSpot = (ImageButton) findViewById(R.id.imgbtn_open_hotspot);
		final ImageButton btnEnter = (ImageButton) findViewById(R.id.imgbtn_enter);
		File appDir = new File(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived");
		final ImageView imgWifiLinked = (ImageView)findViewById(R.id.img_wifi_link);
		imgWifiLinked.setImageResource(R.drawable.wifi_unlink);
		if(! appDir.exists())
		{
			appDir.mkdirs();
		}
		File configDir = new File(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived" + "/Config");
		final File configFile = new File(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived" + "/Config/init.conf");
		if(! configDir.exists())
		{
			configDir.mkdirs();
			if(!configFile.exists())
				try {
					configFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		else
		{
			FileReader fileReader;
			try {
				fileReader = new FileReader(configFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String lastUserName = bufferedReader.readLine();
				fileReader.close();
				bufferedReader.close();
				if(lastUserName != null)
				{
					editUserName.setText(lastUserName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
					case TOASTMESSAGE:
						Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
						break;
					case WIFILINKED:
						imgWifiLinked.setImageResource(R.drawable.wifi_link);
						break;
				}
			}
		};

		btnOpenHotSpot.setOnClickListener(new OnClickListener(){        //�����ȵ�
			@Override
			public void onClick(View v) {
				username = editUserName.getText().toString();

				if(username.equals(""))
				{
					Message.obtain(handler, TOASTMESSAGE, "请输入用户名和热点数据后再创建热点").sendToTarget();
				}
				else
				{
					try {
						FileWriter fileWriter = new FileWriter(configFile, false);
						fileWriter.write(username);
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Intent intent=new Intent(LoginActivity.this,OpenApActivity.class);
					intent.putExtra("SSID",editUserName.getText().toString());
					intent.putExtra("password",editPassword.getText().toString());
					startActivityForResult(intent,2);
				}
			}
		});

		btnEnter.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				username = editUserName.getText().toString();
				password = editPassword.getText().toString();
				if(username.equals(""))
				{
					Message.obtain(handler, TOASTMESSAGE, "用户名不能为空").sendToTarget();
				}
				else if(!isWifiLinked)
				{
					Message.obtain(handler, TOASTMESSAGE, "您尚未连接到由本APP创建的热点").sendToTarget();
				}
				else
				{
					try {
						FileWriter fileWriter = new FileWriter(configFile, false);
						fileWriter.write(username);
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Intent intent=new Intent(LoginActivity.this,MainActivity.class);
					intent.putExtra("username",username);
					intent.putExtra("ifap", "notap");
					intent.putExtra("apname", ssid);
					intent.putExtra("presharedkey", wifiPwd);
					startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 2) {
			String APName = data.getStringExtra("apname");
			String preSharedKey = data.getStringExtra("presharedkey");
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra("username", username);
			intent.putExtra("apname", APName);
			intent.putExtra("presharedkey", preSharedKey);
			intent.putExtra("ifap", "isap");
			Log.i("LogginActivity","创建热点成功");
			startActivity(intent);
		} else if (resultCode == Activity.RESULT_OK) {
			String contents = data.getStringExtra("SCAN_RESULT"); //图像内容
			Log.i("QRCodeScanResult", contents);
			final String[] apinfo = contents.split("/");
			if(apinfo[0].equals("APINFO"))
			{
				//apinfo[1]="Honor 9 Lite";
                //apinfo[1]=(EditText)findViewById(R.id.edit_password).getText().toString();
				connectWifi(apinfo[1], apinfo[2]);
			}
			else
			{
				Message.obtain(handler, TOASTMESSAGE, "只能扫描本应用程序创建的二维码").sendToTarget();
			}
		}
	}


	@Override
	public void onNewIntent(Intent intent) {
		Log.i("LoginActivity", "new Intent");
		setIntent(intent);
	}


	@Override
	public void onResume() {
		super.onResume();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}


	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
				NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		final String apinfo_all = new String(msg.getRecords()[0].getPayload());   //将收到的NFC消息转换为字符串
		final String[] apinfo = apinfo_all.split("/");
		if(apinfo[0].equals("APINFO"))
		{
			connectWifi(apinfo[1], apinfo[2]);
		}
	}



	private void connectWifi(final String apName, final String presharedKey)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				//WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiManager wifiManager=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				WifiAutoConnectManager wacm = new WifiAutoConnectManager(wifiManager);
				isWifiLinked = wacm.connect(apName, presharedKey, WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
				if(isWifiLinked)
				{
					Message.obtain(handler, TOASTMESSAGE, "已成功连接WIFI").sendToTarget();
					ssid = apName;
					wifiPwd = presharedKey;
					handler.sendEmptyMessage(WIFILINKED);
				}
				else
				{
					Message.obtain(handler, TOASTMESSAGE, "链接失败，请重新链接").sendToTarget();
				}
			}
		}.start();
	}


	@Override
	public void onDestroy()
	{
		//WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager wifiManager=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(ApManager.isWifiApEnabled(wifiManager))
		{
			ApManager.closeWifiAp(wifiManager);
		}
		super.onDestroy();
	}


	public void checkPermission()
	{
		boolean permitted = false;
		while(!permitted)
		{
			if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CHANGE_WIFI_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CHANGE_WIFI_STATE},
						0);
				continue;
			}
			/*else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CHANGE_CONFIGURATION)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CHANGE_CONFIGURATION},
						0);
				continue;
			}*/
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CHANGE_NETWORK_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CHANGE_NETWORK_STATE},
						0);
				continue;
			}
			else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.NFC)
					!= PackageManager.PERMISSION_GRANTED)
			{
				permitted = false;
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.NFC},
						0);
				continue;
			}
			permitted = true;
		}
	}
}
