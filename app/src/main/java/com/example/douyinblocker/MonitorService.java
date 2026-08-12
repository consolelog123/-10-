package com.example.douyinblocker;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import java.util.List;

public class MonitorService extends Service {

    private static final String CHANNEL_ID = "DouyinBlockerChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    private static final int CHECK_INTERVAL = 500; // 每500毫秒检查一次

    private Handler handler;
    private Runnable checkRunnable;
    private boolean isBlocking = false;
    private ConfigManager configManager;

    @Override
    public void onCreate() {
        super.onCreate();
        configManager = new ConfigManager(this);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        handler = new Handler(Looper.getMainLooper());
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkDouyinRunning();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(checkRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkDouyinRunning() {
        // 使用 ConfigManager 检查是否应该阻止
        if (!configManager.shouldBlock()) {
            isBlocking = false;
            return;
        }

        // 检查抖音是否在前台运行
        if (isDouyinInForeground()) {
            if (!isBlocking) {
                isBlocking = true;
                showBlockScreen();
            }
        } else {
            isBlocking = false;
        }
    }

    private boolean isBlockTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        // 晚上10点(22:00)之后到次日早上6点之前
        return hour >= 22 || hour < 6;
    }

    private boolean isDouyinInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo process : processes) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (process.processName.equals(DOUYIN_PACKAGE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showBlockScreen() {
        Intent intent = new Intent(this, BlockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                       Intent.FLAG_ACTIVITY_CLEAR_TOP |
                       Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "抖音监控服务",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("正在监控抖音使用情况");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("抖音限制助手")
            .setContentText("正在保护你的专注时间")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
}
