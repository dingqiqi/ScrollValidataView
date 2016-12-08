package com.dingqiqi.testscrollvalidate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ScrollValidataView mValidataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mValidataView = (ScrollValidataView) findViewById(R.id.validateView);
        //设置背景
        mValidataView.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.a));

        //设置验证快圆圈方向
        mValidataView.setCircleOrientation(0, 0, 1, 0);
        mValidataView.setCallBack(new ScrollValidataView.ValidateCallBack() {
            @Override
            public void doBack(boolean flag) {
                String str = flag ? "好厉害，验证成功！" : "这都能验证失败！";
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 点击事件
     * @param v
     */
    public void onClick(View v) {
        mValidataView.refreshView();
    }

}
