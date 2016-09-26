package com.rance.easypoint.easypoint.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.rance.easypoint.easypoint.R;
import com.rance.easypoint.easypoint.common.Constants;
import com.rance.easypoint.easypoint.common.SharedPreferencesUtils;
import com.rance.easypoint.easypoint.common.MUIUtils;
import com.rance.easypoint.easypoint.service.AuxiliaryService;
import com.rance.easypoint.easypoint.view.MyScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_header)
    LinearLayout mainHeader;
    @BindView(R.id.main_title)
    TextView mainTitle;
    @BindView(R.id.main_open)
    SwitchButton mainOpen;
    @BindView(R.id.main_scrollview)
    MyScrollView mainScrollview;
    @BindView(R.id.containerLayout)
    FrameLayout containerLayout;
    private Intent intent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initEvent();
    }

    private void initEvent() {
        mainScrollview.setOnScrollListener(new MyScrollView.OnScrollListener() {
            @Override
            public void onScroll(int scrollY) {
                if (scrollY >= 200) {
                    mainTitle.setVisibility(View.VISIBLE);
                } else {
                    mainTitle.setVisibility(View.GONE);
                }
            }
        });
        mainOpen.setChecked((boolean) SharedPreferencesUtils.getParam(MainActivity.this, Constants.WINDOWSWITCH, false));
        intent = new Intent(MainActivity.this, AuxiliaryService.class);
        mainOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (MUIUtils.isMIUI() && !MUIUtils.isMiuiFloatWindowOpAllowed(MainActivity.this)) {
                        MUIUtils.openMiuiPermissionActivity(MainActivity.this);
                    }
                    startService(intent);
                    SharedPreferencesUtils.setParam(MainActivity.this, Constants.WINDOWSWITCH, true);
                } else {
                    stopService(intent);
                    SharedPreferencesUtils.setParam(MainActivity.this, Constants.WINDOWSWITCH, false);
                }
            }
        });
    }
}
