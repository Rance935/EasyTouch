package com.rance.easypoint.easypoint.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.kyleduo.switchbutton.SwitchButton;
import com.rance.easypoint.easypoint.R;
import com.rance.easypoint.easypoint.common.Constants;
import com.rance.easypoint.easypoint.common.SharedPreferencesUtils;
import com.rance.easypoint.easypoint.service.AuxiliaryService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_open)
    SwitchButton mainOpen;
    @BindView(R.id.containerLayout)
    LinearLayout containerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private Intent intent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolBarAsHome("EasyTouch");
        initEvent();
    }

    private void initEvent() {
        mainOpen.setChecked((boolean) SharedPreferencesUtils.getParam(MainActivity.this, Constants.WINDOWSWITCH, false));
        intent = new Intent(MainActivity.this, AuxiliaryService.class);
        mainOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(intent);
                    SharedPreferencesUtils.setParam(MainActivity.this, Constants.WINDOWSWITCH, true);
                } else {
                    stopService(intent);
                    SharedPreferencesUtils.setParam(MainActivity.this, Constants.WINDOWSWITCH, false);
                }
            }
        });
    }

    public void AboutClick(View view) {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d("main:","onSaveInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("main:","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}
