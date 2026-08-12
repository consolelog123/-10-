package com.example.douyinblocker;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USAGE_STATS = 1001;
    private static final int REQUEST_OVERLAY = 1002;

    private TextView statusText;
    private Button startButton;
    private Button permissionButton;
    private Button settingsButton;
    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configManager = new ConfigManager(this);

        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);
        permissionButton = findViewById(R.id.permissionButton);
        settingsButton = findViewById(R.id.settingsButton);

        startButton.setOnClickListener(v -> startMonitorService());
        permissionButton.setOnClickListener(v -> requestPermissions());
        settingsButton.setOnClickListener(v -> openSettings());

        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        boolean usagePermission = hasUsageStatsPermission();
        boolean overlayPermission = hasOverlayPermission();

        if (usagePermission && overlayPermission) {
            int startHour = configManager.getStartHour();
            int endHour = configManager.getEndHour();
            String timeRange = String.format("%02d:00 - %02d:00", startHour, endHour);

            statusText.setText("✓ 所有权限已授予\n\n服务运行中\n限制时段：" + timeRange);
            startButton.setEnabled(true);
            permissionButton.setVisibility(View.GONE);
            settingsButton.setEnabled(true);
        } else {
            StringBuilder sb = new StringBuilder("需要授予以下权限：\n\n");
            if (!usagePermission) {
                sb.append("✗ 使用情况访问权限\n");
            }
            if (!overlayPermission) {
                sb.append("✗ 显示在其他应用上层权限\n");
            }
            statusText.setText(sb.toString());
            startButton.setEnabled(false);
            permissionButton.setVisibility(View.VISIBLE);
            settingsButton.setEnabled(false);
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestPermissions() {
        if (!hasUsageStatsPermission()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityForResult(intent, REQUEST_USAGE_STATS);
        } else if (!hasOverlayPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            }
        }
    }

    private void startMonitorService() {
        Intent serviceIntent = new Intent(this, MonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        int startHour = configManager.getStartHour();
        int endHour = configManager.getEndHour();
        String timeRange = String.format("%02d:00 - %02d:00", startHour, endHour);
        statusText.setText("✓ 监控服务已启动\n\n限制时段：" + timeRange);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateStatus();
    }
}
