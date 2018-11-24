package ymc.p2ptransmit.qrcode;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/6/23.
 */

public class QRCodeHelper {
    public static Bitmap generateQRCode(String qrCodeString){
        Bitmap bmp = null; //二维码图片
        QRCodeWriter writer = new QRCodeWriter();
        try{
            //BitMatrix bitMatrix = writer.encode(qrCodeString, BarcodeFormat.QR_CODE, 512, 512);//参数分别表示为: 条码文本内容，条码格式，宽，高
            //设置成UTF-8，以便支持中文
            BitMatrix bitMatrix = new MultiFormatWriter().encode(new String(qrCodeString.getBytes("UTF-8"),"ISO-8859-1"),
                    BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            //绘制每个像素
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++){
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bmp;
    }
}
