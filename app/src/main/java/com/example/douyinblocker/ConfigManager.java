package com.example.douyinblocker;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager {

    private static final String PREFS_NAME = "DouyinBlockerPrefs";
    private static final String KEY_START_HOUR = "start_hour";
    private static final String KEY_END_HOUR = "end_hour";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_WHITELIST_DAYS = "whitelist_days";
    private static final String KEY_UNLOCK_UNTIL = "unlock_until";

    private SharedPreferences prefs;

    public ConfigManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 获取限制开始时间（默认22点）
    public int getStartHour() {
        return prefs.getInt(KEY_START_HOUR, 22);
    }

    // 设置限制开始时间
    public void setStartHour(int hour) {
        prefs.edit().putInt(KEY_START_HOUR, hour).apply();
    }

    // 获取限制结束时间（默认6点）
    public int getEndHour() {
        return prefs.getInt(KEY_END_HOUR, 6);
    }

    // 设置限制结束时间
    public void setEndHour(int hour) {
        prefs.edit().putInt(KEY_END_HOUR, hour).apply();
    }

    // 获取密码（默认为空，表示未设置）
    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, "");
    }

    // 设置密码
    public void setPassword(String password) {
        prefs.edit().putString(KEY_PASSWORD, password).apply();
    }

    // 验证密码
    public boolean verifyPassword(String input) {
        String stored = getPassword();
        if (stored.isEmpty()) {
            return false; // 未设置密码
        }
        return stored.equals(input);
    }

    // 获取白名单日期（周几，1=周一，7=周日）
    public Set<Integer> getWhitelistDays() {
        Set<String> stringSet = prefs.getStringSet(KEY_WHITELIST_DAYS, new HashSet<>());
        Set<Integer> result = new HashSet<>();
        for (String s : stringSet) {
            try {
                result.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    // 设置白名单日期
    public void setWhitelistDays(Set<Integer> days) {
        Set<String> stringSet = new HashSet<>();
        for (Integer day : days) {
            stringSet.add(String.valueOf(day));
        }
        prefs.edit().putStringSet(KEY_WHITELIST_DAYS, stringSet).apply();
    }

    // 判断当前是否在白名单日期
    public boolean isWhitelistDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calendar的周日=1，周一=2...转换为我们的1=周一，7=周日
        int ourDay = (dayOfWeek == 1) ? 7 : (dayOfWeek - 1);
        return getWhitelistDays().contains(ourDay);
    }

    // 设置临时解锁到某个时间戳
    public void setUnlockUntil(long timestamp) {
        prefs.edit().putLong(KEY_UNLOCK_UNTIL, timestamp).apply();
    }

    // 获取解锁截止时间
    public long getUnlockUntil() {
        return prefs.getLong(KEY_UNLOCK_UNTIL, 0);
    }

    // 判断当前是否处于临时解锁状态
    public boolean isTemporarilyUnlocked() {
        long unlockUntil = getUnlockUntil();
        return unlockUntil > System.currentTimeMillis();
    }

    // 清除临时解锁
    public void clearUnlock() {
        prefs.edit().putLong(KEY_UNLOCK_UNTIL, 0).apply();
    }

    // 判断当前时间是否应该阻止
    public boolean shouldBlock() {
        // 如果在白名单日期，不阻止
        if (isWhitelistDay()) {
            return false;
        }

        // 如果临时解锁中，不阻止
        if (isTemporarilyUnlocked()) {
            return false;
        }

        // 检查时间段
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startHour = getStartHour();
        int endHour = getEndHour();

        if (startHour > endHour) {
            // 跨天情况，例如22:00到次日6:00
            return currentHour >= startHour || currentHour < endHour;
        } else {
            // 同一天，例如8:00到22:00
            return currentHour >= startHour && currentHour < endHour;
        }
    }
}
