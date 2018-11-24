package ymc.p2ptransmit.service;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import ymc.p2ptransmit.main.MainActivity;
import ymc.p2ptransmit.filemanager.BigFileHelper;
import ymc.p2ptransmit.filemanager.BigFileReceiver;
import ymc.p2ptransmit.filemanager.BigFileSender;
import ymc.p2ptransmit.filemanager.FileOpener;
import ymc.p2ptransmit.filemanager.FileResult;
import ymc.p2ptransmit.filemanager.TransmitReceiver;
import ymc.p2ptransmit.filemanager.TransmitSender;
import ymc.p2ptransmit.friendsmanager.FriendsManager;
import ymc.p2ptransmit.tcp.TcpHelper;
import ymc.p2ptransmit.transmitcontacts.TransmitContactsActivity;
import ymc.p2ptransmit.udp.UdpHelper;
import ymc.p2ptransmit.wifiManager.LanIPHelper;

public class MainService extends Service{
	private Map<String,Integer> transmitProgress;
	private TransmitReceiver transmitReceiver;    
	private String userName = "";                  //本机的文件传输码和用户名
	private String message = null;
	private UdpHelper uh;
	private TcpHelper th;
	private int port;                                                  //文件传输模块的TCP端口号
	private boolean isApState;                                         //判断当前设备是否是热点模式
	public FriendsManager friendsManager;
	public List<String> onlineUser = new ArrayList<String>();             //当前在线用户列表
	private ServerSocket server;
	private	boolean isRunning;
	private ArrayList<String> listSendTask = new ArrayList<>();
	private ArrayList<String> listSendingReceiver = new ArrayList<>();
	private ArrayList<String> bigFileTask = new ArrayList<>();
	private boolean bigFileSending = false;   //判断大文件是否正在发送


	@SuppressLint("HandlerLeak") @Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		if(intent.getStringExtra("ifap").equals("isap")){
			isApState = true;
		}
		else{
			isApState = false;
		}
        isRunning = true;
		userName = intent.getStringExtra("username");
        onlineUser.add("请选择");
		uh = new UdpHelper(LanIPHelper.getLocalIpAddress(isApState, MainService.this));
		th = new TcpHelper(9876);
		friendsManager = new FriendsManager();
		Thread fileListener = new Thread(new Runnable() {
			@Override
			public void run() {
				port = 9999;       //绑定端口
				while (port > 9000) {
					try {
						server = new ServerSocket(port);
						if (server != null)
							break;
					} catch (Exception e) {
						port--;
					}
				}
				if (server != null) {
					transmitReceiver = new TransmitReceiver(server);
					transmitProgress = new HashMap<String, Integer>();
					while (MainService.this.isRunning)
                    {//接收文件
						transmitReceiver.ReceiveName(transmitProgress);
						//通知activity显示文件正在接收的图标
						Intent intentReceiving = new Intent();
						intentReceiving.setAction(MainActivity.ACTION_FILERECEIVEING);
						sendBroadcast(intentReceiving);
						FileResult result = transmitReceiver.ReceiveData(transmitProgress);
						Intent intentReceived = new Intent();
						intentReceived.setAction(MainActivity.ACTION_FILERECEIVED);
						intentReceived.putExtra("msg", result.getResult());
						sendBroadcast(intentReceived);
						//通知activity让文件正在接收的图标消失
						if (transmitProgress.size() == 0) {
							//mhandler.sendEmptyMessage(4);
						}
						if(result.isAPK())
						{
							FileOpener.openfile(getApplicationContext(), new File(result.getFilePath()));
						}
					}
				} else {
					//Message.obtain(handler, 1, "未能绑定端口").sendToTarget();
				}
			}
		});
		fileListener.start();                                        //接收文件
		SetUdpListenerThread udpListener = new SetUdpListenerThread();  //启动线程，监听局域网内的UDP广播
		udpListener.start();
		SetTcpListenerThread tcpListener = new SetTcpListenerThread();  //启动线程，接收TCP消息
		tcpListener.start();
		BroadcastInfo bci = new BroadcastInfo();                     //定时在局域网内广播本机的IP以及用户名
		bci.start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				send();
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				bigFileSend();
			}
		}).start();
		return START_REDELIVER_INTENT;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		//Toast.makeText(this, "Service 解除绑定", Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}
	

	@Override
	public void onDestroy()
	{
		//Toast.makeText(this, "Service 销毁", Toast.LENGTH_LONG).show();
        MainService.this.isRunning = false;
        try {
            server.close();
			th.close();
			uh.close();
        } catch (IOException e) {
            Log.e("关闭ServerSocket:", e.toString());
            e.printStackTrace();
        }
        super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		//Toast.makeText(this, "绑定Service", Toast.LENGTH_LONG).show();
		return new MsgBinder();
	}
	
	
	public class MsgBinder extends Binder{
		public MainService getService()
		{
			return MainService.this;
		}
	}
	
	
	private class SetUdpListenerThread extends Thread{
		@Override
		public void run(){
			while(MainService.this.isRunning)
			{
				message = uh.ReceiveMessage();
				if(message.startsWith("T&I"))
				{
					final String[] msg = message.split("/");
					if(!friendsManager.getOnlineNames().contains(msg[1]))
					{
						friendsManager.addFriend(msg[1], msg[2] + "/" + msg[3]);
						onlineUser.add(msg[1]);
					}
				}
			}
		}
	}//end SetUdpListenerThread
	
	
	private class SetTcpListenerThread extends Thread{
		@Override
		public void run(){
			while(MainService.this.isRunning)
			{
				final String tcpMessage = th.Receive();
				Log.i("log", tcpMessage);
				if(tcpMessage.startsWith("Response"))
				{	
					final String[] temp = tcpMessage.split("/");
					friendsManager.addFriend(temp[1], temp[2] + "/" + temp[3] + "/" + temp[4]);
				}
				else if(tcpMessage.startsWith("PrepareTransmitContacts"))
				{
					Log.i("Mainservice", "收到准备接收通讯录消息的提示");
					Intent intent=new Intent(MainService.this, TransmitContactsActivity.class);
					intent.putExtra("recFromIP", tcpMessage.split("/")[1]);
					intent.putExtra("recFromName", tcpMessage.split("/")[2]);
					intent.putExtra("command","receive");
					intent.putExtra("num", tcpMessage.split("/")[3]);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
				else if(tcpMessage.startsWith("bigfile"))
				{
					//格式为   "bigfile/发送方ip/发送方端口起始端口/文件名/发送方姓名"
					//temp[1]为发送方ip 2为发送方起始端口 3为文件名 4为文件发送者的姓名
					final String[] temp = tcpMessage.split("/");
					new Thread(new Runnable() {
						@Override
						public void run() {
							Intent intentReceiving = new Intent();
							intentReceiving.setAction(MainActivity.ACTION_FILERECEIVEING);
							sendBroadcast(intentReceiving);
							BigFileReceiver bigFileReceiver = new BigFileReceiver(temp[1], Integer.valueOf(temp[2]), temp[3]);
							bigFileReceiver.start();
							try {
								bigFileReceiver.join();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							String result = "来自" + temp[4] + "的文件" + temp[3] + "接收完成";
							Intent intent = new Intent();
							intent.setAction(MainActivity.ACTION_FILERECEIVED);
							intent.putExtra("msg", result);
							sendBroadcast(intent);
							if(temp[3].endsWith(".APK") || temp[3].endsWith(".apk"))
							{
								FileOpener.openfile(getApplicationContext(), new File(BigFileReceiver.ROOTPATH + temp[3]));
							}
						}
					}).start();
				}
			}
		}
	}//end SetTcpListenerThread
	
	
	private class BroadcastInfo extends Thread{
		@Override
		public void run()
		{
			Timer t = new Timer();
			class MyTask extends TimerTask {
				@Override
				public void run() {
					uh.SendMessage("T&I/" + userName + "/" + LanIPHelper.getLocalIpAddress(isApState, MainService.this) + "/" + String.valueOf(port));
				}
			}
			t.schedule(new MyTask(), 100, 2000);
		}
	}// end Broadcast
	
	
	public void sendFile(String sendname, String sendpath, String curreceiverinfo) //发送文件的外部接口
	{
		/*final String sendName = sendname;
		final String sendPath = sendpath;
		final String curReceiverInfo = curreceiverinfo;
		Thread sendThread = new Thread(new Runnable(){
			@Override
			public void run() {
				String targetIP = curReceiverInfo.split("/")[0];
				Log.i("Receiver IP", targetIP);
				int targetPort =  Integer.valueOf(curReceiverInfo.split("/")[1]).intValue();
				TransmitSender transmitSender = new TransmitSender();
				listSendingReceiver.add(curReceiverInfo);
				String result = transmitSender.sendFile(sendName, sendPath, targetIP, targetPort, userName);
				listSendingReceiver.remove(curReceiverInfo);
				Intent intent = new Intent();
				intent.setAction(MainActivity.ACTION_FILESEND);
				intent.putExtra("msg", result);
				sendBroadcast(intent);
			}
		});
		sendThread.start();*/
		File file = new File(sendpath);
		System.out.println(file.length());
		Log.i("MainServiceReceverInfo", curreceiverinfo);
		bigFileTask.add(sendname + "&&&&" + sendpath + "&&&&" + curreceiverinfo);
		/*if(file.length() > 20971520)  //文件大于20M
		{
			bigFileTask.add(sendname + "&&&&" + sendpath + "&&&&" + curreceiverinfo);
		}
		else
		{
			listSendTask.add(sendname + "&&&&" + sendpath + "&&&&" + curreceiverinfo);
		}*/
	}


	public void send()    //不停查询任务列表，看是否有新的发送任务
	{
		while(isRunning)
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int i = 0;
			while(listSendTask.size() != 0)
			{
				String[] cmd = listSendTask.get(i).split("&&&&");
				final String sendName = cmd[0];
				final String sendPath = cmd[1];
				final String curReceiverInfo = cmd[2];
				boolean isThisReceiverReceiving = false;         //判断该接受者是否正在接收文件
				for(int j = 0; j < listSendingReceiver.size(); j++)
				{
					if(curReceiverInfo.equals(listSendingReceiver.get(j)))
					{
						isThisReceiverReceiving = true;
						break;
					}
				}
				if(!isThisReceiverReceiving)
				{
					listSendTask.remove(i);
					Thread sendThread = new Thread(new Runnable(){
						@Override
						public void run() {
							String targetIP = curReceiverInfo.split("/")[0];
							int targetPort =  Integer.valueOf(curReceiverInfo.split("/")[1]).intValue();
							TransmitSender transmitSender = new TransmitSender();
							listSendingReceiver.add(curReceiverInfo);
							String result = transmitSender.sendFile(sendName, sendPath, targetIP, targetPort, userName);
							listSendingReceiver.remove(curReceiverInfo);
							Intent intent = new Intent();
							intent.setAction(MainActivity.ACTION_FILESEND);
							intent.putExtra("msg", result);
							sendBroadcast(intent);
						}
					});
					sendThread.start();
					break;
				}
				else
				{
					i++;
				}
			}
		}
	}


	public void bigFileSend()  //判断大文件的任务列表是否为空，如果不为空并且没有大文件正在发送，则发送
	{
		while(isRunning)
		{
			if(!bigFileSending)
			{
				if(bigFileTask.size() != 0)
				{
					bigFileSending = true;
					String temp = bigFileTask.remove(0);
					String[] cmds = temp.split("&&&&");
					final String sendName = cmds[0];
					final String sendPath = cmds[1];
					final String curReceiverInfo = cmds[2];
					String targetIP = curReceiverInfo.split("/")[0];
					int targetPort =  Integer.valueOf(curReceiverInfo.split("/")[1]).intValue();
					//"bigfile/发送方ip/发送方端口起始端口/文件名/发送方姓名"
					String myIp = LanIPHelper.getLocalIpAddress(isApState, MainService.this);
					Log.i("myIp", myIp);
					long[] dividings = BigFileHelper.getDividing(sendPath, 5);
					BigFileSender bigFileSender = new BigFileSender(6000, sendPath, dividings);
					bigFileSender.start();
					th.Send(targetIP, "bigfile/" + myIp + "/" + 6000 + "/" + sendName + "/" + userName);
					try {
						bigFileSender.join();             //将bigFileSender加入当前线程
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Intent intent = new Intent();
					intent.setAction(MainActivity.ACTION_FILESEND);
					intent.putExtra("msg", sendName + "发送完成");
					sendBroadcast(intent);
					bigFileSending = false;
				}
			}
			else
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
}
