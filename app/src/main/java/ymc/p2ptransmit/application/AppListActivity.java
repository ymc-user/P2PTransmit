package ymc.p2ptransmit.application;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ymc.p2ptransmit.R;

public class AppListActivity extends Activity {
    private List<AppInfo> installedAppList;
    private BrowseAppInfoAdapter browseAppInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        TextView txtTitle = (TextView)findViewById(R.id.txt_applist_titile);
        TextPaint textPaint = txtTitle.getPaint();
        textPaint.setFakeBoldText(true);

        ListView lvAppList = (ListView)findViewById(R.id.listview_app_list);
        installedAppList = AppInfoScanner.getAllInstalledAppInfo(getPackageManager(), AppInfoScanner.THIRD_PARTY_APP); //扫描app
        browseAppInfoAdapter = new BrowseAppInfoAdapter(getApplicationContext(), installedAppList);
        lvAppList.setAdapter(browseAppInfoAdapter);
        lvAppList.setOnItemClickListener(new AdapterView.OnItemClickListener() {   //给listview的每一行绑定点击事件
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView txtDetail = (TextView) view.findViewById(R.id.txt_applist_label);
                TextView txtSourcePath = (TextView)view.findViewById(R.id.txt_applist_sourcepath);
                Intent intent = new Intent();
                intent.putExtra("FileName", txtDetail.getText().toString() + ".apk");
                intent.putExtra("FilePath", txtSourcePath.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }



}
