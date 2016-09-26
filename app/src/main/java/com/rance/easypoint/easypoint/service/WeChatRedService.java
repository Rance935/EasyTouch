package com.rance.easypoint.easypoint.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.rance.easypoint.easypoint.common.AccessibilityHelper;
import com.rance.easypoint.easypoint.common.Constants;

import java.util.Iterator;
import java.util.List;

/**
 * Created by rance on 2016/9/26.
 * 微信抢红包服务
 */
public class WeChatRedService extends AccessibilityService {

    private static WeChatRedService service;
    private PackageInfo mWechatPackageInfo = null;
    private static final String BUTTON_CLASS_NAME = "android.widget.Button";
    /**
     * 微信的包名
     */
    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";

    private int HongBaoSize = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        updatePackageInfo();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            //第一步：监听通知栏消息
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.i("demo", "text:" + content);
                        if (content.contains("[微信红包]")) {
                            //模拟打开通知栏消息
                            if (event.getParcelableData() != null
                                    &&
                                    event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            //第二步：监听是否进入微信红包消息界面
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    //开始抢红包
                    getPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    //开始打开红包
                    openPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    AccessibilityHelper.performBack(service);
                }
                break;
        }
    }

    /**
     * 查找到
     */
    @SuppressLint("NewApi")
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        AccessibilityNodeInfo targetNode = null;
        int wechatVersion = getWechatVersion();
        if (wechatVersion < 700) {
            targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "拆红包");
        } else {
            String buttonId = "com.tencent.mm:id/b43";
            if (wechatVersion == 700) {
                buttonId = "com.tencent.mm:id/b2c";
            }
            if (buttonId != null) {
                targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, buttonId);
            }

            if (targetNode == null) {
                //分别对应固定金额的红包 拼手气红包
                AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, "发了一个红包", "给你发了一个红包", "发了一个红包，金额随机");

                if (textNode != null) {
                    for (int i = 0; i < textNode.getChildCount(); i++) {
                        AccessibilityNodeInfo node = textNode.getChild(i);
                        if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
                            targetNode = node;
                            break;
                        }
                    }
                }
            }

            if (targetNode == null) { //通过组件查找
                targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
            }
        }

        if (targetNode != null) {
            AccessibilityHelper.performClick(targetNode);
        }
    }

    @SuppressLint("NewApi")
    private void getPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(Constants.TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list != null && HongBaoSize == list.size()){
            return;
        }
        if (list != null && list.isEmpty()) {
            // 从消息列表查找红包
            AccessibilityNodeInfo node = AccessibilityHelper.findNodeInfosByText(nodeInfo, "[微信红包]");
            if (node != null) {
                AccessibilityHelper.performClick(nodeInfo);
            }
        } else if (list != null) {
            HongBaoSize = list.size();
            AccessibilityNodeInfo node = list.get(list.size() - 1);
            AccessibilityHelper.performClick(node);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = this;
        Toast.makeText(this, "已连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service = null;
    }

    /**
     * 判断当前服务是否正在运行
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isRunning() {
        if (service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if (info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if (!isConnect) {
            return false;
        }
        return true;
    }

    /**
     * 获取微信的版本
     */
    private int getWechatVersion() {
        if (mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /**
     * 更新微信包信息
     */
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getPackageManager().getPackageInfo(WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}