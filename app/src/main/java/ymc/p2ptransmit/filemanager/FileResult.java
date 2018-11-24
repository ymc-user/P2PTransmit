package ymc.p2ptransmit.filemanager;

/**
 * Created by Administrator on 2017/5/15.
 */

public class FileResult {
    private String fileName;
    private String sender;
    private String filePath;
    private String result;
    private boolean isApk;


    public FileResult(){isApk = false;}


    public void setFileName(String value)
    {
        this.fileName = value;
    }

    public void setSender(String value)
    {
        this.sender = value;
    }


    public void setFilePath(String value)
    {
        this.filePath = value;
        if(value.endsWith("apk"))
        {
            this.isApk = true;
        }
    }


    public void setResult(String value)
    {
        this.result = value;
    }


    public String getFileName(){return  this.fileName;}

    public String getSender(){return  this.sender;}

    public String getFilePath(){return this.filePath;}

    public String getResult(){return this.result;}

    public boolean isAPK()
    {
        return this.isApk;
    }

}
