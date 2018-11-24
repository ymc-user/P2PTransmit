package ymc.p2ptransmit.filemanager;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/20.
 */

public class FileHelper {
    public static String convertFileLengthToString(long len)
    {
        if(len < 1024)
        {
            return len + "B";
        }
        else if (len >= 1024 && len < 1048576)
        {
            BigDecimal bigDecimal1 = new BigDecimal(len);
            BigDecimal bigDecimal2 = new BigDecimal(1024);
            return  (bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_HALF_UP)).toString() + "KB";
        }
        else if (len >= 1048576 && len <1073741824)
        {
            BigDecimal bigDecimal1 = new BigDecimal(len);
            BigDecimal bigDecimal2 = new BigDecimal(1048576);
            return  (bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_HALF_UP)).toString() + "MB";
        }
        else
        {
            BigDecimal bigDecimal1 = new BigDecimal(len);
            BigDecimal bigDecimal2 = new BigDecimal(1073741824);
            return  (bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_HALF_UP)).toString() + "GB";
        }
    }


    public static String convertFileTimeToString(long time)
    {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return format.format(date);
    }
}
