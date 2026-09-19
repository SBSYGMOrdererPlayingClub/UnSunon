package sbsy.ordererplayingclub.unsunon;
import android.app.Notification;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.github.megatronking.netbare.NetBareService;


public class MyVPNService extends NetBareService {
    @Override
    public int notificationId(){
        return 1;
    }
    @Override
    public Notification createNotification(){
        return new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID).setContentTitle("UnSunon").setContentText("网络代理正在运行").setSmallIcon(R.mipmap.ic_launcher).build();
    }
}