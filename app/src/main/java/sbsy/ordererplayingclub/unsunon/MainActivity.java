package sbsy.ordererplayingclub.unsunon;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.github.megatronking.netbare.ssl.JKS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_VPN_PERMISSION = 1000;
    private boolean mPendingStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("MainActivity", "onCreate called");
        setContentView(R.layout.activity_main);

        MyApplication app = (MyApplication) this.getApplication();

        Button btnOk = findViewById(R.id.ok_btn);
        btnOk.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            EditText etIp = findViewById(R.id.ip);
            editor.putString("ip", etIp.getText().toString().trim());
            editor.apply();
        });

        EditText etIp = findViewById(R.id.ip);
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        etIp.setText(sp.getString("ip", ""));

        // 导出CA证书到下载目录，用户手动在设置里安装
        Button btnCert = findViewById(R.id.install_cert_btn);
        btnCert.setOnClickListener(v -> exportCertificate());

        Switch aSwitch = findViewById(R.id.switch_);
        aSwitch.setOnCheckedChangeListener((k, v) -> {
            if (v) {
                Intent prepareIntent = VpnService.prepare(this);
                if (prepareIntent != null) {
                    mPendingStart = true;
                    startActivityForResult(prepareIntent, REQUEST_VPN_PERMISSION);
                    aSwitch.setChecked(false);
                } else {
                    app.turnOnService();
                }
            } else {
                mPendingStart = false;
                app.turnOffService();
            }
        });
    }

    private void exportCertificate() {
        File pemFile = new File(getCacheDir(), "myalias.pem");
        if (!pemFile.exists()) {
            Toast.makeText(this, "证书文件尚未生成，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "UnSunon_CA.pem";
        boolean success = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: 用 MediaStore 写入 Downloads
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/x-pem-file");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (InputStream in = new FileInputStream(pemFile);
                     OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out != null) {
                        byte[] buf = new byte[4096];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        success = true;
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            // Android 9 及以下: 直接写 Downloads 目录
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dest = new File(downloads, fileName);
            try (InputStream in = new FileInputStream(pemFile);
                 OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                success = true;
            } catch (IOException e) {
                Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (success) {
            Toast.makeText(this,
                    "证书已导出到 Downloads/" + fileName + "\n"
                    + "请到 设置→安全→加密与凭据→安装证书 中选择该文件安装",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VPN_PERMISSION) {
            if (resultCode == RESULT_OK && mPendingStart) {
                mPendingStart = false;
                MyApplication app = (MyApplication) this.getApplication();
                app.turnOnService();
                Switch aSwitch = findViewById(R.id.switch_);
                aSwitch.setChecked(true);
            } else {
                mPendingStart = false;
                Toast.makeText(this, "需要VPN权限才能抓包", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
