package ymc.p2ptransmit.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ymc.p2ptransmit.R;
import ymc.p2ptransmit.service.MainService;
import ymc.p2ptransmit.view.RadarView;

public class ScanActivity extends Activity {
    private RadarView mRadarView;
    ArrayList<Map<String, Object>> listItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Intent intent = getIntent();
        final ArrayList<String> listUsers = intent.getStringArrayListExtra("users");
        mRadarView = (RadarView)findViewById(R.id.radarView);
        ListView lvScanedUser = (ListView) findViewById(R.id.listview_user_scan);
        listItem = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(
                getApplicationContext(),
                listItem,
                R.layout.item_scan_user,
                new String[]{"username"},
                new int[]{R.id.txt_user_scan_username}
        );
        lvScanedUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView txtUsername = (TextView)view.findViewById(R.id.txt_user_scan_username);

                Intent intent = new Intent();
                intent.putExtra("curReceiver", txtUsername.getText().toString());
                Log.i("选中的用户", txtUsername.getText().toString());
                setResult(2, intent);
                finish();
            }
        });
        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case 0:
                        mRadarView.addPoint();
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        };

        lvScanedUser.setAdapter(adapter);
        mRadarView.setSearching(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 1; i < listUsers.size(); i++)
                {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("username", listUsers.get(i));
                    listItem.add(map);
                    handler.sendEmptyMessage(0);
                }
            }
        }).start();

    }

}
