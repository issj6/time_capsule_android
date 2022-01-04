package com.example.timecapsule_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddPage extends AppCompatActivity {

    Handler handler;

    private TextView txtDate;
    EditText mainText;
    Calendar calendar = Calendar.getInstance(Locale.CHINA);
    AlertDialog.Builder builder = null;

    String email, username;
    static int year, mon, day, hour, min;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_page);
        txtDate = findViewById(R.id.textView2);
        mainText = findViewById(R.id.edt_order_note_text);
        year = -1;
        handler = new showInfoHandler();

        Bundle bundle = this.getIntent().getExtras();
        username = bundle.getString("username");
    }


    /**
     * 日期选择
     **/
    @SuppressLint("SetTextI18n")
    public static void showDatePickerDialog(Activity activity, int themeResId, Calendar calendar, TextView tv) {
        new DatePickerDialog(activity, themeResId, (view, n_year, monthOfYear, dayOfMonth) -> {
            // 此处得到选择的时间，可以进行你想要的操作
            year = n_year;
            mon = monthOfYear+1;
            day = dayOfMonth;
            tv.setText("开启日期：" + n_year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日");
            showTimePickerDialog(activity, themeResId, calendar, tv);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 时间选择
     **/
    @SuppressLint("SetTextI18n")
    public static void showTimePickerDialog(Activity activity, int themeResId, Calendar calendar, final TextView tv) {
        new TimePickerDialog(activity, themeResId, (view, hourOfDay, minute) -> {
            hour = hourOfDay;
            min = minute;
            tv.setText(tv.getText().toString() + " " + hourOfDay + "时" + minute + "分");
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    public void setEmail() {
        final EditText inputServer = new EditText(AddPage.this);
        builder = new AlertDialog.Builder(AddPage.this);
        @SuppressLint("DefaultLocale") AlertDialog dialog = builder.setTitle("用于接受邮件提醒的邮箱").setView(inputServer).setPositiveButton("确定", (d, which) -> {
            //点击确认后执行
            email = inputServer.getText().toString();
            //设置邮箱后提交
            String content = mainText.getText().toString();
            JSONObject jsonObject = new JSONObject();
            HashMap<String, Object> paramMap = new HashMap<>();

            paramMap.put("username", username);
            paramMap.put("email", email);
            paramMap.put("endTime", String.format("%02d-%02d-%02d %02d:%02d", year, mon, day, hour, min));

            Date date = new Date();//获取当前的日期
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
            String nowTime = df.format(date);
            paramMap.put("startTime", nowTime);
            paramMap.put("content", content);
            post("https://tc.ruut.cn:8100/submit", paramMap);
        }).create();
        dialog.show();

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.width = 900;
        dialog.getWindow().setAttributes(layoutParams);
    }


    @SuppressLint("NonConstantResourceId")
    public void click(View view) throws JSONException {
        switch (view.getId()) {
            case R.id.textView2:
                showDatePickerDialog(this, 0, calendar, txtDate);
                break;
            case R.id.submit:
                if (year == -1) {
                    showInfo("请先选择日期");
                    break;
                }
                setEmail();
                break;
            case R.id.textView3:
                finish();
                break;
        }
    }

    public void showInfo(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void post(String url, HashMap<String, Object> paramMap) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder body = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            body.add(entry.getKey(), (String) entry.getValue());
        }

        Request request = new Request.Builder()
                .url(url)
                .post(body.build())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    //与主线程通信
                    if (result.equals("success")) {
                        handler.sendEmptyMessage(1);
                    } else {
                        handler.sendEmptyMessage(2);
                    }
                }
            }
        });
    }



    //    线程通信显示
    private class showInfoHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showInfo("胶囊已成功埋下，记得来打开哦");
                    finish();
                    break;
                case 2:
                    showInfo("您的胶囊出错了，没有成功埋下，请重试");
                    break;
            }
        }
    }
}