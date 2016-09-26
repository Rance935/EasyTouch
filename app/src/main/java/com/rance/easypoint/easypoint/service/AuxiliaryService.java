package com.rance.easypoint.easypoint.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rance.easypoint.easypoint.common.Constants;
import com.rance.easypoint.easypoint.common.SharedPreferencesUtils;
import com.rance.easypoint.easypoint.view.EasyTouchView;

public class AuxiliaryService extends Service{
    private Intent mIntent;
    private EasyTouchView mEasyTouchView;

    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
  
    public void onCreate() {  
        super.onCreate();
        mEasyTouchView = new EasyTouchView(this);
        mEasyTouchView.initTouchViewEvent();
    }  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIntent = intent;
        return super.onStartCommand(intent, flags, startId);  
    }  

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEasyTouchView.quitTouchView();
        SharedPreferencesUtils.setParam(this, Constants.WINDOWSWITCH, false);
    }
}