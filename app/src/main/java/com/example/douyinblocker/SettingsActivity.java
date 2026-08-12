package com.example.douyinblocker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private EditText passwordInput;
    private CheckBox mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck;
    private CheckBox fridayCheck, saturdayCheck, sundayCheck;
    private Button saveButton;
    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        configManager = new ConfigManager(this);

        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);
        passwordInput = findViewById(R.id.passwordInput);
        mondayCheck = findViewById(R.id.mondayCheck);
        tuesdayCheck = findViewById(R.id.tuesdayCheck);
        wednesdayCheck = findViewById(R.id.wednesdayCheck);
        thursdayCheck = findViewById(R.id.thursdayCheck);
        fridayCheck = findViewById(R.id.fridayCheck);
        saturdayCheck = findViewById(R.id.saturdayCheck);
        sundayCheck = findViewById(R.id.sundayCheck);
        saveButton = findViewById(R.id.saveButton);

        // 设置24小时制
        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        // 加载当前配置
        loadCurrentConfig();

        saveButton.setOnClickListener(v -> saveConfig());
    }

    private void loadCurrentConfig() {
        // 加载时间
        startTimePicker.setHour(configManager.getStartHour());
        endTimePicker.setHour(configManager.getEndHour());

        // 加载白名单日期
        Set<Integer> whitelistDays = configManager.getWhitelistDays();
        mondayCheck.setChecked(whitelistDays.contains(1));
        tuesdayCheck.setChecked(whitelistDays.contains(2));
        wednesdayCheck.setChecked(whitelistDays.contains(3));
        thursdayCheck.setChecked(whitelistDays.contains(4));
        fridayCheck.setChecked(whitelistDays.contains(5));
        saturdayCheck.setChecked(whitelistDays.contains(6));
        sundayCheck.setChecked(whitelistDays.contains(7));

        // 显示当前密码状态（不显示实际密码）
        if (!configManager.getPassword().isEmpty()) {
            passwordInput.setHint("已设置密码（留空保持不变）");
        }
    }

    private void saveConfig() {
        // 保存时间
        int startHour = startTimePicker.getHour();
        int endHour = endTimePicker.getHour();
        configManager.setStartHour(startHour);
        configManager.setEndHour(endHour);

        // 保存白名单日期
        Set<Integer> whitelistDays = new HashSet<>();
        if (mondayCheck.isChecked()) whitelistDays.add(1);
        if (tuesdayCheck.isChecked()) whitelistDays.add(2);
        if (wednesdayCheck.isChecked()) whitelistDays.add(3);
        if (thursdayCheck.isChecked()) whitelistDays.add(4);
        if (fridayCheck.isChecked()) whitelistDays.add(5);
        if (saturdayCheck.isChecked()) whitelistDays.add(6);
        if (sundayCheck.isChecked()) whitelistDays.add(7);
        configManager.setWhitelistDays(whitelistDays);

        // 保存密码（如果输入了新密码）
        String newPassword = passwordInput.getText().toString().trim();
        if (!newPassword.isEmpty()) {
            configManager.setPassword(newPassword);
        }

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        finish();
    }
}
