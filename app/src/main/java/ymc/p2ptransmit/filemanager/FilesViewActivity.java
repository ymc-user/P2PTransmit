package ymc.p2ptransmit.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import ymc.p2ptransmit.R;

public class FilesViewActivity extends Activity {
	private SimpleAdapter adapter;
	ArrayList<Map<String, Object>> listItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_view);
		Toast.makeText(getApplicationContext(), "选择要发送的文件", Toast.LENGTH_SHORT).show();
		ListView fileView = (ListView)findViewById(R.id.fileView);
		listItem = new ArrayList<Map<String, Object>>();
		this.FilesListView(Environment.getExternalStorageDirectory().getPath());
		//this.FilesListView(Environment.getDataDirectory().getPath());
		adapter = new SimpleAdapter(
				getApplicationContext(),
				listItem,
				R.layout.item_view,
				new String[] {"image", "name", "path", "type", "parent", "info"},
				new int[]{R.id.image, R.id.file_name, R.id.file_path, R.id.file_type, R.id.file_parent, R.id.file_info}
		);
		fileView.setAdapter(adapter);
		fileView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView isDirectory = (TextView)view.findViewById(R.id.file_type); 
				TextView path = (TextView)view.findViewById(R.id.file_path);
				TextView name = (TextView)view.findViewById(R.id.file_name);
				
				if (Boolean.parseBoolean(isDirectory.getText().toString())){
					FilesListView(path.getText().toString());
					adapter.notifyDataSetChanged();
				}else{
					Intent intent = new Intent();
					intent.putExtra("FileName", name.getText().toString());
					intent.putExtra("FilePath", path.getText().toString());
					setResult(RESULT_OK, intent);
					finish();
				}
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
				if(file[i].isDirectory()){
					map.put("image", R.drawable.folder);
					map.put("info", FileHelper.convertFileTimeToString(file[i].lastModified()));
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
			if (selectedFile.getParent() != null){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("image", R.drawable.ret);
				map.put("name", "点此回到上一级目录");
				map.put("path", selectedFile.getParent());
				map.put("type", true);
				map.put("parent", selectedFile.getParent());
				listItem.add(0, map);
			}
		}else{
			Toast.makeText(getApplicationContext(), "该文件或文件夹无法读取", Toast.LENGTH_SHORT).show();
		}
	}

}
