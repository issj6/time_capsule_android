package com.example.timecapsule_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {


    Handler handler;
    CheckBox saveU;
    CheckBox saveP;
    EditText uet;
    EditText pet;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch bgm;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new showInfoHandler();
        saveU = findViewById(R.id.saveUsername);
        saveP = findViewById(R.id.savePassword);
        uet = findViewById(R.id.username);
        pet = findViewById(R.id.password);
        bgm = findViewById(R.id.bgm);
        Intent bgmIntent = new Intent(this, BackGroundMusic.class);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        //将背景音乐活动添加至全局变量
        BgmData.setBgmS(bgmIntent);

        //将该活动添加至全局变量以便退出应用
        ActivityData.getInstance().addActivity(this);


        //设置背景音乐默认开启播放
        bgm.setChecked(true);
        BgmData.setBgmState(true);
        startService(BgmData.getBgmS());
        editor.putBoolean("bgmSet",true);


        saveP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveU.setChecked(true);
                }
            }
        });

        //是否保存账号密码判断并做出相关设置
        if (preferences.getBoolean("isSaveU", false)) {
            String username = preferences.getString("username", "");
            uet.setText(username);
            saveU.setChecked(true);
            //保存用户名的前提下判断保存密码
            if (preferences.getBoolean("isSaveP", false)) {
                String password = preferences.getString("password", "");
                pet.setText(password);
                saveP.setChecked(true);
            }
        }

        //开关背景音乐监听
        bgm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    showInfo("1");
                    startService(BgmData.getBgmS());
                    BgmData.setBgmState(true);
                    editor.putBoolean("bgmSet",true);
                } else {
//                    showInfo("2");
                    stopService(BgmData.getBgmS());
                    BgmData.setBgmState(false);
                    editor.putBoolean("bgmSet",false);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bgm.setChecked(BgmData.isBgmState());

    }

    @SuppressLint("NonConstantResourceId")
    public void click(View view) {
        if (view.getId() == R.id.register) {
            jump(2);
        } else if (view.getId() == R.id.login) {

            String u = uet.getText().toString();
            String p = pet.getText().toString();

            if (u.isEmpty()) {
                showInfo("用户名不能为空");
                return;
            } else if (p.isEmpty()) {
                showInfo("请输入密码");
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection conn = null;
                    conn = (Connection) DBOpenHelper.getConn();
                    String sql = "select username,password from user where username='" + u + "'";
                    Statement st;
                    try {
                        st = (Statement) conn.createStatement();
                        ResultSet rs = st.executeQuery(sql);
                        if (rs.next()) {
                            if (rs.getString(1).equals(u) && rs.getString(2).equals(p)) {
                                handler.sendEmptyMessage(1);
                            } else {
                                handler.sendEmptyMessage(2);
                            }
                        }
                        st.close();
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }
                }
            }).start();
        } else if (view.getId() == R.id.clearSave) {
            editor = preferences.edit();
            editor.clear();
            editor.apply();
            saveU.setChecked(false);
            saveP.setChecked(false);
            uet.setText("");
            pet.setText("");
        }
    }

    private class showInfoHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String u = uet.getText().toString();
                    String p = pet.getText().toString();
                    if (saveU.isChecked()) {
                        editor.putString("username", u);
                        editor.putBoolean("isSaveU", true);
                    } else {
                        editor.putBoolean("isSaveU", false);
                    }

                    if (saveP.isChecked()) {
                        editor.putString("password", p);
                        editor.putBoolean("isSaveP", true);
                    } else {
                        editor.putBoolean("isSaveP", false);
                    }
                    editor.apply();
                    jump(1);
                    break;
                case 2:
                    showInfo("密码错误");
                    break;
                case 3:
                    showInfo("登录出错");
                    break;
            }
        }
    }


    public void showInfo(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void jump(int flag) {
        if (flag == 1) {
            Intent intent = new Intent(this, MainPage.class);
            intent.putExtra("username", uet.getText().toString());
            startActivity(intent);
        } else if (flag == 2) {
            Intent intent = new Intent(this, Register.class);
            startActivity(intent);
        }
    }
}