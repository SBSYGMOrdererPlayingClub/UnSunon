package sbsy.ordererplayingclub.unsunon;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.http.HttpInterceptor;
import com.github.megatronking.netbare.http.HttpInterceptorFactory;
import com.github.megatronking.netbare.http.HttpVirtualGatewayFactory;
import com.github.megatronking.netbare.ip.IpAddress;
import com.github.megatronking.netbare.ssl.JKS;

import java.util.Collection;
import java.util.Collections;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "unsunon_vpn_channel";
    public JKS jks;
    public NetBareConfig config;
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 NetBare，第二个参数是是否开启调试日志
        NetBare.get().attachApplication(this, BuildConfig.DEBUG);

        // 创建通知渠道 (Android 8.0+)
        createNotificationChannel();

        jks = new JKS(
                this,
                "myalias",                    // 证书别名，自定义
                "mypassword".toCharArray(),   // 密码，自定义
                "My Common Name",             // CN
                "My Organization",            // O
                "My Org Unit",                // OU
                "Cert Org",                   // 证书组织
                "Cert Org Unit"               // 证书组织单元
        );

        // 每个连接创建独立的 injector 实例，避免并发状态冲突
        HttpInterceptorFactory factory = new HttpInterceptorFactory() {
            @NonNull
            @Override
            public HttpInterceptor create() {
                return HttpInjectInterceptor.createFactory(new MyHttpInjector()).create();
            }
        };
        var gatewayFact = new HttpVirtualGatewayFactory(jks, Collections.singletonList(factory));
        config = new NetBareConfig.Builder()
                .setMtu(4096)
                .setAddress(new IpAddress("10.0.0.2", 32))
                .addRoute(new IpAddress("0.0.0.0", 0))
                .dumpUid(false)
                .setVirtualGatewayFactory(gatewayFact)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "UnSunon VPN",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("网络代理服务通知");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    public void turnOnService(){
        NetBare.get().start(this.config);
    }
    public void turnOffService(){
        NetBare.get().stop();
    }
}