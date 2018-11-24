package ymc.p2ptransmit.filemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ymc.p2ptransmit.R;

public class ReceiverFilesBroswerActivity extends Activity {
    private ListView lvReceivedFiles;
    private SimpleAdapter adapter;
    ArrayList<Map<String, Object>> listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_files_broswer);
        lvReceivedFiles = (ListView)findViewById(R.id.listview_received_file);
        listItem = new ArrayList<Map<String, Object>>();
        this.FilesListView(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived");
        adapter = new SimpleAdapter(
                getApplicationContext(),
                listItem,
                R.layout.item_view,
                new String[] {"image", "name", "path", "type", "parent", "info"},
                new int[]{R.id.image, R.id.file_name, R.id.file_path, R.id.file_type, R.id.file_parent, R.id.file_info}
        );
        lvReceivedFiles.setAdapter(adapter);
        lvReceivedFiles.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView path = (TextView)view.findViewById(R.id.file_path);
                FileOpener.openfile(getApplicationContext(), new File(path.getText().toString()));
            }
        });
        lvReceivedFiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TextView path = (TextView)view.findViewById(R.id.file_path);
                TextView name = (TextView)view.findViewById(R.id.file_name);
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiverFilesBroswerActivity.this);
                builder.setTitle("注意");
                builder.setMessage("是否删除" + name.getText().toString() + " ?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(path.getText().toString());
                        file.delete();
                        FilesListView(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived");
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();
                return true;
            }
        });

    }

    private void FilesListView(String selectedPath){
        File selectedFile = new File(selectedPath);
        if (selectedFile.canRead()){
            File[] file = selectedFile.listFiles();
            listItem.clear();
            for (int i = 0; i < file.length; i++){
                HashMap<String, Object> map = new HashMap<String, Object>();
                if(file[i].isDirectory() || "null".equals(file[i].getName())){
                    continue;
                }
                else{
                    String date = FileHelper.convertFileTimeToString(file[i].lastModified());
                    String length = FileHelper.convertFileLengthToString(file[i].length());
                    map.put("info", date + "  " + length);
                    String fileType = file[i].getName().toLowerCase();
                    if(fileType.endsWith(".mp3")||fileType.endsWith(".wav")||fileType.endsWith(".wma")
                            ||fileType.endsWith(".ape"))
                        map.put("image", R.drawable.music);
                    else if(fileType.endsWith(".mp4")||fileType.endsWith(".mov")||fileType.endsWith(".avi")
                            ||fileType.endsWith(".rmvb")||fileType.endsWith(".mkv"))
                        map.put("image", R.drawable.video);
                    else if(fileType.endsWith(".bmp")||fileType.endsWith(".jpg")||fileType.endsWith(".jpeg")
                            ||fileType.endsWith(".png")||fileType.endsWith(".bmp"))
                        map.put("image", R.drawable.picture);
                    else if(fileType.endsWith(".apk"))
                        map.put("image", R.drawable.apk);
                    else
                        map.put("image", R.drawable.file);
                }
                map.put("name", file[i].getName());
                map.put("path", file[i].getPath());
                map.put("type", file[i].isDirectory());
                map.put("parent", file[i].getParent());
                listItem.add(map);
            }
            //判断有无父目录，增加返回上一级目录菜单
            /*if (selectedFile.getParent() != null){
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("image", R.drawable.ret);
                map.put("name", "点此回到上一级目录");
                map.put("path", selectedFile.getParent());
                map.put("type", true);
                map.put("parent", selectedFile.getParent());
                listItem.add(0, map);
            }*/
        }else{
            Toast.makeText(getApplicationContext(), "该文件或文件夹无法读取", Toast.LENGTH_SHORT).show();
        }
    }
}
