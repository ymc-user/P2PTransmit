package ymc.p2ptransmit.qrcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import ymc.p2ptransmit.R;

public class ShowQRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qrcode);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        //username=findViewById(R.id.Lan_SSID).getContext().toString();
        //username="Honor 9 Lite";
        String apName = intent.getStringExtra("apname");
        String presharedkey = intent.getStringExtra("presharedkey");
        //presharedkey=findViewById(R.id.Lan_password).getContext().toString();
        //presharedkey="123456789";//偷偷改成这个
        final TextView txtTitle = (TextView)findViewById(R.id.txt_qrcode_hint);
        final ImageView img_qrcode = (ImageView)findViewById(R.id.img_qrcode);
        txtTitle.setText("用本应用扫描此二维码，加入" + username + "的局域网");
        img_qrcode.setImageBitmap(QRCodeHelper.generateQRCode("APINFO/" + apName + "/" + presharedkey));
    }
}
