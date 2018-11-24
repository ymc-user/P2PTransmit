package ymc.p2ptransmit.filemanager;

import java.io.File;

public class BigFileHelper {
	public static long[] getDividing(String filePath, int pieces)
	{
		File file = new File(filePath);
		System.out.println("Դ�ļ�����" + file.length());
		long length = file.length();
		long[] lens = new long[pieces + 1];
		lens[0] = 0;
		long pieceLength = length / pieces;
		lens[1] = pieceLength;
		for(int i = 2; i < pieces; i++)
		{
			lens[i] = lens[i-1] + pieceLength;
		}
		lens[pieces] = lens[pieces - 1] + (length - pieceLength * (pieces - 1));
		System.out.println("�ָ����ļ��ķֽ��Ϊ:");
		for(long asd:lens)
		{
			System.out.print(asd + " ");
		}
		System.out.println();
		return lens;
	}
}
