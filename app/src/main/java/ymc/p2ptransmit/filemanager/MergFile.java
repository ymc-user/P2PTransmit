package ymc.p2ptransmit.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MergFile {
	private static final int BUFSIZE = 1024 * 1024;
	public static void  mergFiles(String outFile, String[] files)
	{
		FileChannel outChannel = null;
		FileOutputStream fos = null;
		System.out.println("Merge" + Arrays.toString(files) + "into " + outFile);
		try
		{
			fos = new FileOutputStream(outFile);
			outChannel = fos.getChannel();
			for(String f : files)
			{
				FileInputStream fis = new FileInputStream(f);
				FileChannel fc = fis.getChannel();
				ByteBuffer byteBuffer = ByteBuffer.allocate(BUFSIZE);
				while(fc.read(byteBuffer) != -1)
				{
					byteBuffer.flip();
					outChannel.write(byteBuffer);
					byteBuffer.clear();
				}
				fc.close();
				fis.close();
				File file = new File(f);
				file.delete();
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		try {
			outChannel.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("合并后文件大小为" + new File(outFile).length());
	}
}
