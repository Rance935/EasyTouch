package com.rance.easypoint.easypoint.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rance.easypoint.easypoint.R;
import com.rance.easypoint.easypoint.adapter.ApplicationAdapter;
import com.rance.easypoint.easypoint.common.ApkSearchUtils;
import com.rance.easypoint.easypoint.common.Constants;
import com.rance.easypoint.easypoint.model.ApkModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/21.
 */
public class ApplicationListActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.application_tab_personal_line)
    View applicationTabPersonalLine;
    @BindView(R.id.application_tab_system_line)
    View applicationTabSystemLine;
    @BindView(R.id.application_tab)
    LinearLayout applicationTab;
    @BindView(R.id.application_bottom_txt)
    TextView applicationBottomTxt;
    @BindView(R.id.application_bottom)
    CardView applicationBottom;
    private LinearLayoutManager mLayoutManager;
    private ApplicationAdapter mApplicationAdapter;
    private int index;
    private List<ApkModel> mApkModels;
    private List<ApkModel> mTempApkModels;
    private List<ApkModel> mPersonalApkModels;
    private List<ApkModel> mSystemApkModels;
    private boolean isPersonal = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mApkModels.size() > 0) {
                        mApplicationAdapter.setDate(mApkModels);
                    } else {
                        applicationBottom.setVisibility(View.GONE);
                        Toast.makeText(ApplicationListActivity.this, "没有扫描到任何应用", Toast.LENGTH_SHORT).show();
                    }
                    dismissProgressDialog();
                    break;
                case 1:
                    mApplicationAdapter.setDate(mPersonalApkModels);
                    dismissProgressDialog();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_list);
        ButterKnife.bind(this);
        initDate();
    }

    /**
     * 初始化数据
     */
    private void initDate() {
        index = getIntent().getIntExtra("index", 0);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mApplicationAdapter = new ApplicationAdapter(this);
        mRecyclerView.setAdapter(mApplicationAdapter);
        mApkModels = new ArrayList<>();
        mTempApkModels = new ArrayList<>();
        showProgressDialog("软件获取中...");
        switch (index) {
            case Constants.UPDATE:
                initToolBar("软件更新");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<ApkModel> apkModels = ApkSearchUtils.getAllApplication(ApplicationListActivity.this);
                        for (ApkModel apkModel : apkModels) {
                            if (apkModel.getInstalled() == Constants.INSTALLED_UPDATE) {
                                mApkModels.add(apkModel);
                            }
                        }
                        mHandler.sendEmptyMessage(0);
                    }
                }).start();
                applicationBottomTxt.setText("全部更新");
                break;
            case Constants.UNINSTALL:
                initToolBar("软件卸载");
                applicationTab.setVisibility(View.VISIBLE);
                applicationBottomTxt.setText("全部卸载");
                mPersonalApkModels = new ArrayList<>();
                mSystemApkModels = new ArrayList<>();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mApkModels = ApkSearchUtils.getAllApplication(ApplicationListActivity.this);
                        for (ApkModel apkModel : mApkModels) {
                            if (apkModel.getType() == Constants.PERSONAL_APPLICATION) {
                                mPersonalApkModels.add(apkModel);
                            } else {
                                mSystemApkModels.add(apkModel);
                            }
                        }
                        mHandler.sendEmptyMessage(1);
                    }
                }).start();
                break;
            case Constants.INSTALLATION_PACKAGE:
                initToolBar("安装包管理");
                ApkSearchUtils.FindAllAPKFile(this, new ApkSearchUtils.OnFindAllAPKClick() {
                    @Override
                    public void complete(List<ApkModel> apkModels) {
                        mApkModels = apkModels;
                        mHandler.sendEmptyMessage(0);
                    }
                });
                applicationBottomTxt.setText("全部删除");
                break;
        }
    }

    /**
     * 个人应用
     *
     * @param view
     */
    public void PersonalClick(View view) {
        isPersonal = true;
        mApplicationAdapter.setDate(mPersonalApkModels);
        applicationTabPersonalLine.setVisibility(View.VISIBLE);
        applicationTabSystemLine.setVisibility(View.GONE);
    }

    /**
     * 系统应用
     *
     * @param view
     */
    public void SystemClick(View view) {
        isPersonal = false;
        mApplicationAdapter.setDate(mSystemApkModels);
        applicationTabPersonalLine.setVisibility(View.GONE);
        applicationTabSystemLine.setVisibility(View.VISIBLE);
    }

    /**
     * 底部按钮监听事件
     *
     * @param view
     */
    public void ApplicationBottomClick(View view) {
        switch (index) {
            case Constants.UPDATE:
                mTempApkModels.clear();
                mApkModels = mApplicationAdapter.getDate();
                for (ApkModel apkModel : mApkModels) {
                    if (apkModel.isChecked()) {
                        ApkSearchUtils.UpdateApplication(ApplicationListActivity.this, apkModel.getPackageName());
                        mTempApkModels.add(apkModel);
                    }
                }
                mApkModels.removeAll(mTempApkModels);
                mApplicationAdapter.setDate(mApkModels);
                break;
            case Constants.UNINSTALL:
                mTempApkModels.clear();
                if (isPersonal) {
                    mPersonalApkModels = mApplicationAdapter.getDate();
                    for (ApkModel apkModel : mPersonalApkModels) {
                        if (apkModel.isChecked()) {
                            boolean uninstall = ApkSearchUtils.uninstall(ApplicationListActivity.this, apkModel.getPackageName());
                            if (uninstall)
                                mTempApkModels.add(apkModel);
                        }
                    }
                    mPersonalApkModels.removeAll(mTempApkModels);
                    mApplicationAdapter.setDate(mPersonalApkModels);
                } else {
                    mSystemApkModels = mApplicationAdapter.getDate();
                    for (ApkModel apkModel : mSystemApkModels) {
                        if (apkModel.isChecked()) {
                            boolean uninstall = ApkSearchUtils.uninstall(ApplicationListActivity.this, apkModel.getPackageName());
                            if (uninstall)
                                mTempApkModels.add(apkModel);
                        }
                    }
                    mSystemApkModels.removeAll(mTempApkModels);
                    mApplicationAdapter.setDate(mSystemApkModels);
                }
                break;
            case Constants.INSTALLATION_PACKAGE:
                mTempApkModels.clear();
                mApkModels = mApplicationAdapter.getDate();
                for (ApkModel apkModel : mApkModels) {
                    if (apkModel.isChecked()) {
                        boolean delete = ApkSearchUtils.deleteApplication(ApplicationListActivity.this, apkModel.getFilePath());
                        if (delete)
                            mTempApkModels.add(apkModel);
                    }
                }
                mApkModels.removeAll(mTempApkModels);
                mApplicationAdapter.setDate(mApkModels);
                break;
        }
    }
}
