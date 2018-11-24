package ymc.p2ptransmit.filemanager;

import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class BigFileReceiver extends Thread{
	private String targetIP;
	private ReceiverHelper[] receiverHelpers = new ReceiverHelper[5];
	private CountDownLatch countDownLatch;
	public final static String ROOTPATH = Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived/";
	private String[] tmpFilePath;
	private String fileName;

	public BigFileReceiver(String targetIP, int targetStartPort, String fileName)
	{
		this.fileName = fileName;
		countDownLatch = new CountDownLatch(5);
		this.targetIP = targetIP;
		tmpFilePath = new String[5];
		for(int i = 0; i < 5; i++)
		{
			tmpFilePath[i] = ROOTPATH + fileName + ".tmp" + i;
			receiverHelpers[i] = new ReceiverHelper(targetStartPort + i, tmpFilePath[i]);
		}
	}
	
	
	@Override
	public void run()
	{
		for(int i = 0; i < 5; i++)
		{
			receiverHelpers[i].start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("接收完成，开始合并");
		MergFile.mergFiles(ROOTPATH + fileName, tmpFilePath);
	}
	
	
	
	private class ReceiverHelper extends Thread
	{
		int port;
		String savePath;
		public ReceiverHelper(int port, String savePath)
		{
			this.port = port;
			this.savePath = savePath;
		}
		
		
		@Override
		public void run()
		{
			final Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(targetIP, port), 1000);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(socket.isConnected())
			{
				try {
					FileOutputStream fileOS = new FileOutputStream(savePath, false);
					InputStream inputStream = socket.getInputStream();
					byte[] buffer = new byte[65536];
					int size = -1;
					while ((size = inputStream.read(buffer)) != -1){
						fileOS.write(buffer, 0 ,size);
					}
					fileOS.close();
					inputStream.close();
					new Thread()     //�����̣߳��ж϶Է��Ƿ��Ѿ���ɷ��Ͳ��ҹر���socket
					{
						@Override
						public void run()
						{
							try {
								socket.sendUrgentData(0xFF);
							} catch (IOException e) {
								try {
									socket.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
						}
					}.start();
					socket.close();
					countDownLatch.countDown();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
}
