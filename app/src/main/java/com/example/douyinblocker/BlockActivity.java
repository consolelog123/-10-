package com.example.douyinblocker;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlockActivity extends AppCompatActivity {

    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    private static final int UNLOCK_DURATION = 10 * 60 * 1000; // 10分钟

    private Handler handler;
    private Runnable checkRunnable;
    private TextView timeText;
    private TextView unlockHintText;
    private EditText passwordInput;
    private Button unlockButton;
    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        configManager = new ConfigManager(this);

        // 设置为全屏，覆盖所有内容
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        timeText = findViewById(R.id.timeText);
        unlockHintText = findViewById(R.id.unlockHintText);
        passwordInput = findViewById(R.id.passwordInput);
        unlockButton = findViewById(R.id.unlockButton);

        // 检查是否设置了密码
        if (configManager.getPassword().isEmpty()) {
            unlockHintText.setText("未设置解锁密码\n请在主界面设置中配置");
            passwordInput.setEnabled(false);
            unlockButton.setEnabled(false);
        } else {
            unlockHintText.setText("输入密码可临时解锁 10 分钟");
            unlockButton.setOnClickListener(v -> tryUnlock());
        }

        updateTime();

        // 持续检查抖音是否还在运行
        handler = new Handler();
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                if (!isDouyinRunning()) {
                    finish();
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.postDelayed(checkRunnable, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        // 禁用返回键
        killDouyin();
    }

    private void tryUnlock() {
        String inputPassword = passwordInput.getText().toString().trim();
        if (configManager.verifyPassword(inputPassword)) {
            // 密码正确，设置临时解锁
            long unlockUntil = System.currentTimeMillis() + UNLOCK_DURATION;
            configManager.setUnlockUntil(unlockUntil);
            Toast.makeText(this, "已解锁 10 分钟", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
        }
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        if (timeText != null) {
            timeText.setText("现在是 " + currentTime);
        }
    }

    private boolean isDouyinRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo process : activityManager.getRunningAppProcesses()) {
            if (process.processName.equals(DOUYIN_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    private void killDouyin() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.killBackgroundProcesses(DOUYIN_PACKAGE);
        }
        moveTaskToBack(true);
        finish();
    }
}
