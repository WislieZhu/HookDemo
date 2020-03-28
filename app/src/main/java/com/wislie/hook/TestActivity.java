package com.wislie.hook;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : Wislie
 * e-mail : 254457234@qq.comn
 * date   : 2020/3/27 8:20 PM
 * desc   : 已登录后的Activity
 * version: 1.0
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setText("已登录");
        setContentView(tv);
    }
}
