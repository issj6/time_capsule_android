package com.example.timecapsule_2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Register extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        handler = new showInfoHandler();
    }

    public void click(View view) {

        EditText uet = findViewById(R.id.username);
        EditText pet = findViewById(R.id.password);
        EditText p2et = findViewById(R.id.password2);

        String u = uet.getText().toString();
        String p = pet.getText().toString();
        String p2 = p2et.getText().toString();


        if (view.getId() == R.id.goLogin) {
            toLogin();
        } else if (view.getId() == R.id.bt1) {
            if (u.isEmpty()) {
                showInfo("用户名不能为空");
                return;
            } else if (p.isEmpty()) {
                showInfo("请输入密码");
                return;
            } else if (p2.isEmpty()) {
                showInfo("请确认密码");
                return;
            } else if (!p.equals(p2)) {
                showInfo("两次输入的密码不相同");
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection conn = null;
                    conn = (Connection) DBOpenHelper.getConn();
                    String sql = "select username from user where username='" + u + "'";
                    Statement st;
                    try {
                        st = (Statement) conn.createStatement();
                        ResultSet rs = st.executeQuery(sql);
                        if (rs.next()) {
                            handler.sendEmptyMessage(3);
                            st.close();
                            conn.close();
                            rs.close();
                            return;
                        }
                        st.close();
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(2);
                        return;
                    }

                    String sql2 = "insert into user (username,password) values(?,?)";
                    PreparedStatement pst;
                    try {
                        int temp = 0;
                        pst = (PreparedStatement) conn.prepareStatement(sql2);
                        //将输入的edit框的值获取并插入到数据库中
                        pst.setString(1, u);
                        pst.setString(2, p);
                        temp = pst.executeUpdate();
                        pst.close();
                        conn.close();
                        handler.sendEmptyMessage(1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(2);
                    }
                }
            }).start();
        }
    }

    public void showInfo(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    private class showInfoHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showInfo("注册成功,请前往登录");
                    toLogin();
                    break;
                case 2:
                    showInfo("注册出现错误");
                    break;
                case 3:
                    showInfo("用户已存在");
                    break;
            }
        }
    }

    public void toLogin(){
        finish();
    }
}