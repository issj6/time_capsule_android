package com.example.timecapsule_2;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainPage extends AppCompatActivity {

    String username;
    List<Contents> contentsList = new ArrayList<>();
    MyAdapter mMyAdapter;
    RecyclerView mRecyclerView;
    Handler handler;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    private SharedPreferences.Editor editor;
    FloatingActionsMenu fam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Bundle bundle = this.getIntent().getExtras();
        username = bundle.getString("username");
        mRecyclerView = findViewById(R.id.recyclerview);
        //创建handler，用于线程通信
        handler = new uiHandler();
        //以下用于创建循环视图
        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainPage.this);
        mRecyclerView.setLayoutManager(layoutManager);
        fam = findViewById(R.id.multiple_actions_up);
        ActivityData.getInstance().addActivity(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

//        addPage.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showInfo("123");
//            }
//        });


        //监听添加按钮
        final FloatingActionButton editAction = findViewById(R.id.edit);
        editAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToAdd();
                fam.collapse();
            }
        });

        //监听设置按钮
        final FloatingActionButton setAction = findViewById(R.id.set);
        setAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, SettingsActivity.class);
                startActivity(intent);
                fam.collapse();
            }
        });

        //监听退出按钮
        final FloatingActionButton exitAction = findViewById(R.id.exit);
        exitAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityData.getInstance().exit();
            }
        });

    }

    public void closeFBT(View v){
        fam.collapse();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //监听设置背景音乐
        boolean bgmSet = preferences.getBoolean("bgmSet", BgmData.isBgmState());
        if (bgmSet && !BgmData.isBgmState()) {
            BgmData.setBgmState(true);
            startService(BgmData.getBgmS());
        } else if (!bgmSet) {
            BgmData.setBgmState(false);
            stopService(BgmData.getBgmS());
        }



        //监听设置邮件提醒
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                conn = (Connection) DBOpenHelper.getConn();
                String sqlQ = "select emailRemind from user where username='" + username + "'";
                try {
                    Statement st = (Statement) conn.createStatement();
                    ResultSet rs = st.executeQuery(sqlQ);
                    boolean nowState = false;
                    boolean setEmailState = false;
                    if (rs.next()) {
                        System.out.println(rs.getInt(1));
//                        Log.i("现在邮箱提醒状态：", "" + rs.getInt(1));
                        nowState = rs.getInt(1) == 1;
//                        Log.i("测试点001：", "" + (rs.getInt(1) == 1));
                        setEmailState=preferences.getBoolean("emailRemind", nowState);
                        editor.putBoolean("emailRemind", nowState);
                    }

                    if (setEmailState && !nowState) {
                        String sqlU = "update user set emailRemind=1 where username='" + username + "'";
                        st.executeUpdate(sqlU);
                    } else if (!setEmailState && nowState) {
                        String sqlU = "update user set emailRemind=0 where username='" + username + "'";
                        st.executeUpdate(sqlU);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        //每次进入主界面则加载内容
        showProgressDialog("提示信息", "正在获取数据,请稍后...");
        getData("https://tc.ruut.cn:8100/get?username=" + username);
    }

    /*** 循环视图 ***/
    class MyAdapter extends RecyclerView.Adapter<MyViewHoder> {

        @NonNull
        @Override
        public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(MainPage.this, R.layout.item_list, null);
            MyViewHoder myViewHoder = new MyViewHoder(view);
            return myViewHoder;
        }


        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull MyViewHoder holder, @SuppressLint("RecyclerView") int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fam.collapse();
                }
            });
            Contents c = contentsList.get(position);
            Log.i("测试点001:", position+" "+c.state+" "+c.content);
            holder.vOpen.setEnabled(false);

            holder.vContent.setText(c.showContent);
            holder.vEndTime.setText(c.endTime);
            if (c.state) {
                holder.vOpen.setBackground(getResources().getDrawable(R.drawable.open_bt_style));
                holder.vOpen.setEnabled(true);
//                holder.vOpen.setText("允许");
            }


            holder.vOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击循环视图中的按钮后执行
                    new XPopup.Builder(MainPage.this)
                            .asConfirm("创建时间：" + contentsList.get(position).startTime, contentsList.get(position).content,
                                    null, null, null
                                    , null, false, R.layout.content_dialog)//绑定已有布局
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return contentsList.size();
        }


    }

    class MyViewHoder extends RecyclerView.ViewHolder {
        TextView vContent;
        TextView vEndTime;
        Button vOpen;

        public MyViewHoder(@NonNull View itemView) {
            super(itemView);
            vContent = itemView.findViewById(R.id.content);
            vEndTime = itemView.findViewById(R.id.endTime);
            vOpen = itemView.findViewById(R.id.open);
        }
    }

    //    异步请求获取添加过的数据
    public void getData(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(20_000, TimeUnit.MILLISECONDS)
                .connectTimeout(20_000, TimeUnit.MILLISECONDS)
                .readTimeout(20_000, TimeUnit.MILLISECONDS)
                .writeTimeout(20_000, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常显示
                handler.sendEmptyMessage(3);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    try {
                        contentsList.clear();
                        Log.i("长度测试:", "onResponse: " + contentsList.size());
                        if (result.equals("()")) {
                            handler.sendEmptyMessage(2);
                        } else {
                            JSONArray jsonArray = new JSONArray(result);
                            Date date = new Date();//获取当前的日期
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
                            String nowTime = df.format(date);
                            Log.i("当前时间：", "onResponse: " + nowTime);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String content = jsonObject.getString("content");
                                String startTime = jsonObject.getString("startTime");
                                String endTime = jsonObject.getString("endTime");

                                Contents item = new Contents();

                                if (nowTime.compareTo(endTime) < 0) {
                                    if (content.length() < 5) {
                                        item.showContent = content + "......\n\n" + "此胶囊还不能打开，请耐心等待哦...";
                                    } else {
                                        item.showContent = content.substring(0, 5) + "......\n\n" + "此胶囊还不能打开，请耐心等待哦......";
                                    }
                                    item.state = false;
                                } else {
                                    item.showContent = content;
                                    item.state = true;
                                }
                                item.content = content;
                                item.startTime = startTime;
                                item.endTime = endTime;
                                contentsList.add(item);
                            }
                            //通知ui更新循环视图内容
                            handler.sendEmptyMessage(1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //处理UI需要切换到UI线程处理
                }
            }
        });
    }

    //    线程通信显示
    private class uiHandler extends Handler {
        @SuppressLint("NotifyDataSetChanged")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //更新循环试图内容
                    mMyAdapter.notifyItemRangeChanged(0, mMyAdapter.getItemCount() + 1);
                    mMyAdapter.notifyDataSetChanged();
                    hideProgressDialog();
                    break;
                case 2:
                    mMyAdapter.notifyDataSetChanged();
                    hideProgressDialog();
                    showInfo("无数据");
                    break;
                case 3:
                    showInfo("数据异常,请重新登录或稍后再试");
                    hideProgressDialog();
                    break;
            }
        }
    }

    /*
     * 提示加载
     */
    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {

            progressDialog = ProgressDialog.show(this,
                    title, message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }

        progressDialog.show();

    }

    /*
     * 隐藏提示加载
     */
    public void hideProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }


    public void showInfo(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void jumpToAdd() {
        Intent intent = new Intent(this, AddPage.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}