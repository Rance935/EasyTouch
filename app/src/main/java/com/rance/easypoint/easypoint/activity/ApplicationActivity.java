package com.rance.easypoint.easypoint.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.rance.easypoint.easypoint.R;
import com.rance.easypoint.easypoint.common.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/21.
 */
public class ApplicationActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);
        ButterKnife.bind(this);
        initToolBar("应用管理");
    }

    /**
     * 软件更新
     *
     * @param view
     */
    public void UpdateClick(View view) {
        Intent intent = new Intent(this, ApplicationListActivity.class);
        intent.putExtra("index", Constants.UPDATE);
        startActivity(intent);
    }

    /**
     * 软件卸载
     *
     * @param view
     */
    public void UninstallClick(View view) {
        Intent intent = new Intent(this, ApplicationListActivity.class);
        intent.putExtra("index", Constants.UNINSTALL);
        startActivity(intent);
    }

    /**
     * 安装包管理
     *
     * @param view
     */
    public void InstallationPackageClick(View view) {
        Intent intent = new Intent(this, ApplicationListActivity.class);
        intent.putExtra("index", Constants.INSTALLATION_PACKAGE);
        startActivity(intent);
    }
}
