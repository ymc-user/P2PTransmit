package ymc.p2ptransmit.filemanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class BigFileSender extends Thread{
	private String filePath;
	private SendHelper[] sendHelpers = new SendHelper[5];
	private CountDownLatch countDownLatch;
	
	public BigFileSender(int startPort, String filePath, long dividings[])
	{
		countDownLatch = new CountDownLatch(5);
		this.filePath = filePath;
		for(int i = 0; i < 5; i++)
		{
			sendHelpers[i] = new SendHelper(startPort + i, dividings[i], dividings[i + 1]);
		}
	}
	
	
	@Override
	public void run()
	{
		for(int i = 0; i < 5; i++)
		{
			sendHelpers[i].start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("发送完成");
	}
	
	
	
	private class SendHelper extends Thread
	{
		long start, end;
		private ServerSocket server;
		public SendHelper(int port, long start, long end)
		{
			this.start = start;
			this.end = end;
			try {
				server = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		@Override
		public void run()
		{
			try {
				Socket socket = server.accept();
				OutputStream outputStream = socket.getOutputStream();
				RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r");
				raf.seek(start);
				int readSize = -1;
				long totalSize = end - start;
				byte[] buffer = new byte[65536];
				while(totalSize > 0)
				{
					if(totalSize > 65536)
					{
						readSize = raf.read(buffer, 0, 65536);
						totalSize -= readSize;
					}
					else
					{
						readSize = raf.read(buffer, 0, (int)totalSize);
						totalSize = 0;
					}
					if(readSize == -1)
						break;
					else
						outputStream.write(buffer, 0, readSize);
				}
				outputStream.close();
				socket.close();
				raf.close();
				server.close();
				countDownLatch.countDown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
