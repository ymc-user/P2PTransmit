package ymc.p2ptransmit.wifiManager;

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAutoConnectManager {
    WifiManager wifiManager;

    // 定义几种加密方式，一种是WEP，一种是WPA，一种是无密码的情况
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    // 构造函数
    public WifiAutoConnectManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    // 提供一个外部接口，传入要连接的无线网
    public boolean connect(String ssid, String password, WifiCipherType type) {
        Connecter connecter = new Connecter(ssid, password, type);
        return connecter.link();
    }

    // 查看以前是否也配置过这个网络
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        else if (Type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        else if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此处需要修改否则不能自动重联
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } 
        else {
            return null;
        }
        return config;
    }

    // 打开wifi功能
    private boolean openWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    class Connecter {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public Connecter(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }


        public boolean link() {
            // 打开wifi
            if(!wifiManager.isWifiEnabled())
            {
                openWifi();
            }
            // 开启wifi功能需要一段时间(手机上测试一般需要1-3秒左右)，所以要等到wifi
            // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
            while (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                try {
                    // 为了避免程序一直while循环，100毫秒后检测……
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                	
                }
            }
            WifiConfiguration tempConfig = isExsits(ssid);
            if (tempConfig != null) {
                Log.i("exsitNetWorkId", tempConfig.networkId + "");
                wifiManager.removeNetwork(tempConfig.networkId);
            }
            int maxPriority = 0;
            for(WifiConfiguration conf : wifiManager.getConfiguredNetworks())
            {
                if(conf.priority > maxPriority)
                {
                    maxPriority = conf.priority + 1;
                    Log.i("priority", ""+conf.priority);
                }
            }
            if(maxPriority >= 1000000)
            {
                maxPriority = 999999;
            }
            Log.i("maxpr", ""+maxPriority);
            int i = 5;
            WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
            if (wifiConfig == null) {
                return false;
            }
            wifiConfig.priority = maxPriority;
            Log.i("idnumber", wifiManager.getConfiguredNetworks().size() + "");
            int netID = wifiManager.addNetwork(wifiConfig);
            for(int k=0;k<wifiManager.getConfiguredNetworks().size();k++){
                if(wifiManager.getConfiguredNetworks().get(k).networkId==netID){
                    netID=k;
                    break;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while((i--) > 0)
            {
                Log.i("netID", ""+netID);
                wifiManager.enableNetwork(netID, true);
                Log.i("idnumber", wifiManager.getConfiguredNetworks().size() + "");

                int status = wifiManager.getConfiguredNetworks().get(netID).status;

                int j = 100;
                while(status != 0 && j > 0)
                {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    status = wifiManager.getConfiguredNetworks().get(netID).status;
                    j--;
                    Log.i("wifi", "尚未连接");
                }
                if(status == 0)
                {
                    return true;
                }
            }
            return false;
        }
    }
}
