package com.rance.easypoint.easypoint.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.rance.easypoint.easypoint.R;
import com.rance.easypoint.easypoint.activity.ApplicationActivity;
import com.rance.easypoint.easypoint.activity.LockScreenActivity;
import com.rance.easypoint.easypoint.activity.MainActivity;
import com.rance.easypoint.easypoint.service.WeChatRedService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EasyTouchView extends View {
    private Context mContext;
    private WindowManager mWManager;
    private WindowManager.LayoutParams mViewEventMParams;
    private WindowManager.LayoutParams mRocketMParams;
    private View mTouchView;

    private ImageView mIconImageView = null;
    private PopupWindow mPopuWin;
    private View mSettingTable;
    private TextView home;
    private TextView application;
    private TextView collection;
    private TextView lock;
    private TextView red;
    private TextView setting;
    private TextView speed;
    private TextView torch;
    private Drawable open;
    private Drawable close;
    private Drawable redOpen;
    private Drawable redClose;

    private int mTag = 0;
    private int midX;
    private int midY;
    private int mOldOffsetX;
    private int mOldOffsetY;

    private ImageView mRocketImageView = null;

    private Timer mTimer = null;
    private TimerTask mTask = null;
    private Camera camera;

    public EasyTouchView(Context context) {
        super(context);
        mContext = context;
    }

    public void initTouchViewEvent() {
        initEasyTouchViewEvent();
        initEasyTouchRocket();
        initSettingTableView();
    }

    private void initEasyTouchViewEvent() {
        // 设置载入view WindowManager参数
        mWManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        midX = mWManager.getDefaultDisplay().getWidth() / 2;
        midY = mWManager.getDefaultDisplay().getHeight() / 2;
        mTouchView = LayoutInflater.from(mContext).inflate(R.layout.easy_touch_view, null);
        mIconImageView = (ImageView) mTouchView.findViewById(R.id.easy_touch_view_imageview);
        mTouchView.setBackgroundColor(Color.TRANSPARENT);

        mTouchView.setOnTouchListener(mTouchListener);
        WindowManager wm = mWManager;
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        mViewEventMParams = wmParams;
        //适配小米、魅族等手机需要悬浮框权限的问题
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
            wmParams.type = LayoutParams.TYPE_PHONE;
        } else {
            wmParams.type = LayoutParams.TYPE_TOAST;
        }
        /**
         *这里的flags也很关键
         *代码实际是wmParams.flags |= FLAG_NOT_FOCUSABLE;
         *40的由来是wmParams的默认属性（32）+ FLAG_NOT_FOCUSABLE（8）
         */
        wmParams.flags=40;
        wmParams.width = 100;
        wmParams.height = 100;
        wmParams.format = -3; // 透明
        wm.addView(mTouchView, wmParams);
    }

    /**
     * 初始化小火箭
     */
    private void initEasyTouchRocket() {
        // 设置载入view WindowManager参数
        mRocketImageView = new ImageView(mContext);
        mRocketImageView.setImageResource(R.mipmap.rocket_launch_1);
        WindowManager wm = mWManager;
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        mRocketMParams = wmParams;
        wmParams.type = LayoutParams.TYPE_PHONE; // 这里的2002表示系统级窗口，你也可以试试2003。
        wmParams.flags = 40; // 设置桌面可控
        wmParams.width = LayoutParams.WRAP_CONTENT;
        wmParams.height = LayoutParams.WRAP_CONTENT;
        wmParams.format = -3; // 透明
        wmParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    }

    protected void sendRocket() {
        //设置火箭居中
        mRocketMParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mWManager.addView(mRocketImageView, mRocketMParams);
        new Thread() {
            public void run() {
                for (int i = 0; i <= 50; i++) {
                    //等待一段时间再更新位置，用于控制火箭速度
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int y = 25 * i;
                    Message msg = Message.obtain();
                    msg.arg1 = y;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
        clearMemory();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int y = msg.arg1;
            mRocketMParams.y = y;
            mWManager.updateViewLayout(mRocketImageView, mRocketMParams);
            if(y >= 1250){
                mWManager.removeView(mRocketImageView);
            }
        }
    };

    private void initSettingTableView() {
        mSettingTable = LayoutInflater.from(mContext).inflate(R.layout.show_setting_table, null);
        home = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_home);
        application = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_application);
        collection = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_collection);
        lock = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_lock);
        red = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_red);
        setting = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_setting);
        speed = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_speed);
        torch = (TextView) mSettingTable.findViewById(R.id.show_setting_table_item_torch);
        home.setOnClickListener(mClickListener);
        application.setOnClickListener(mClickListener);
        collection.setOnClickListener(mClickListener);
        lock.setOnClickListener(mClickListener);
        red.setOnClickListener(mClickListener);
        setting.setOnClickListener(mClickListener);
        speed.setOnClickListener(mClickListener);
        torch.setOnClickListener(mClickListener);

        open = getResources().getDrawable(R.mipmap.icon_torch_open);
        open.setBounds(0, 0, open.getMinimumWidth(), open.getMinimumHeight());
        close = getResources().getDrawable(R.mipmap.icon_torch_close);
        close.setBounds(0, 0, close.getMinimumWidth(), close.getMinimumHeight());

        redOpen = getResources().getDrawable(R.mipmap.icon_red_open);
        redOpen.setBounds(0, 0, redOpen.getMinimumWidth(), redOpen.getMinimumHeight());
        redClose = getResources().getDrawable(R.mipmap.icon_red_close);
        redClose.setBounds(0, 0, redClose.getMinimumWidth(), redClose.getMinimumHeight());

        updateSettingTableView();
    }

    /**
     * 更新设置页面数据
     */
    private void updateSettingTableView()
    {
        if (WeChatRedService.isRunning()) {
            red.setCompoundDrawables(null, redOpen, null, null);
        } else {
            red.setCompoundDrawables(null, redClose, null, null);
        }

        if (isFlashlightOn()) {
            torch.setCompoundDrawables(null, open, null, null);
        } else {
            torch.setCompoundDrawables(null, close, null, null);
        }
    }
    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.show_setting_table_item_home:
                    HomeBack();
                    break;
                case R.id.show_setting_table_item_application:
                    Application();
                    break;
                case R.id.show_setting_table_item_collection:
                    hideSettingTable();
                    break;
                case R.id.show_setting_table_item_lock:
                    LockScreen();
                    break;
                case R.id.show_setting_table_item_red:
                    openHongBao();
                    break;
                case R.id.show_setting_table_item_setting:
                    openApplication();
                    break;
                case R.id.show_setting_table_item_speed:
                    sendRocket();
                    hideSettingTable();
                    break;
                case R.id.show_setting_table_item_torch:
                    flashlightUtils();
                    break;

            }

        }
    };

    /**
     * 回到主界面
     */
    private void HomeBack() {
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mContext.startActivity(mHomeIntent);
        hideSettingTable();
    }

    /**
     * 一键锁屏
     */
    private void LockScreen() {
        Intent intent = new Intent(mContext, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        hideSettingTable();
    }

    /**
     * 是否开启了闪光灯
     *
     * @return
     */
    public boolean isFlashlightOn() {
        if (camera == null) {
            camera = Camera.open();
        }
        Camera.Parameters parameters = camera.getParameters();
        String flashMode = parameters.getFlashMode();
        if (null != flashMode && flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 闪光灯开关
     */
    public void flashlightUtils() {
        if (camera == null) {
            camera = Camera.open();
        }
        Camera.Parameters parameters = camera.getParameters();
        if (isFlashlightOn()) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);// 关闭
            camera.setParameters(parameters);
            camera.release();
            camera = null;
            torch.setCompoundDrawables(null, close, null, null);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 开启
            camera.setParameters(parameters);
            torch.setCompoundDrawables(null, open, null, null);
        }

    }

    /**
     * 打开应用
     */
    private void openApplication() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        hideSettingTable();
    }

    /**
     * 应用管理
     */
    private void Application() {
        Intent intent = new Intent(mContext, ApplicationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        hideSettingTable();
    }

    /**
     * 红包开关
     */
    private void openHongBao() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            if (WeChatRedService.isRunning())
                Toast.makeText(mContext, "找到EasyTouch抢红包，然后关闭服务即可", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(mContext, "找到EasyTouch抢红包，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hideSettingTable();
    }

    /**
     * 清理内存
     */
    public void clearMemory() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);

        long beforeMem = getAvailMemory(mContext);
        int count = 0;
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                ActivityManager.RunningAppProcessInfo appProcessInfo = infoList.get(i);
                //importance 该进程的重要程度  分为几个级别，数值越低就越重要。
                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
                if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; ++j) {//pkgList 得到该进程下运行的包名
                        if (!pkgList[j].equals(mContext.getPackageName())) {
                            am.killBackgroundProcesses(pkgList[j]);
                            count++;
                        }
                    }
                }

            }
        }
        long afterMem = getAvailMemory(mContext);
        Toast.makeText(mContext, "清理了" + count + " 个进程, 共计"
                + (afterMem - beforeMem) + "M", Toast.LENGTH_LONG).show();
    }

    /**
     * 获取可用内存大小
     *
     * @param context
     * @return
     */
    private long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

    public void quitTouchView() {
        mWManager.removeView(mTouchView);
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        clearTimerThead();
        hideSettingTable();
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        float lastX, lastY;
        int paramX, paramY;

        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();

            float x = event.getRawX();
            float y = event.getRawY();

            if (mTag == 0) {
                mOldOffsetX = mViewEventMParams.x; // 偏移量
                mOldOffsetY = mViewEventMParams.y; // 偏移量
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    motionActionDownEvent(x, y);
                    break;

                case MotionEvent.ACTION_MOVE:
                    motionActionMoveEvent(x, y);
                    break;

                case MotionEvent.ACTION_UP:
                    motionActionUpEvent(x, y);
                    break;

                default:
                    break;
            }

            return true;
        }

        private void motionActionDownEvent(float x, float y) {
            lastX = x;
            lastY = y;
            paramX = mViewEventMParams.x;
            paramY = mViewEventMParams.y;
        }

        private void motionActionMoveEvent(float x, float y) {
            int dx = (int) (x - lastX);
            int dy = (int) (y - lastY);
            mViewEventMParams.x = paramX + dx;
            mViewEventMParams.y = paramY + dy;
            mTag = 1;

            // 更新悬浮窗位置
            mWManager.updateViewLayout(mTouchView, mViewEventMParams);
        }

        private void motionActionUpEvent(float x, float y) {
            int newOffsetX = mViewEventMParams.x;
            int newOffsetY = mViewEventMParams.y;
            if (mOldOffsetX == newOffsetX && mOldOffsetY == newOffsetY) {
                updateSettingTableView();
                mPopuWin = new PopupWindow(mSettingTable, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mPopuWin.setTouchInterceptor(new OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                            hideSettingTable();
                            return true;
                        }
                        return false;
                    }
                });

                mPopuWin.setBackgroundDrawable(new BitmapDrawable());
                mPopuWin.setTouchable(true);
                mPopuWin.setFocusable(true);
                mPopuWin.setOutsideTouchable(true);
                mPopuWin.setContentView(mSettingTable);

                if (Math.abs(mOldOffsetX) > midX) {
                    if (mOldOffsetX > 0) {
                        mOldOffsetX = midX;
                    } else {
                        mOldOffsetX = -midX;
                    }
                }

                if (Math.abs(mOldOffsetY) > midY) {
                    if (mOldOffsetY > 0) {
                        mOldOffsetY = midY;
                    } else {
                        mOldOffsetY = -midY;
                    }
                }

                mPopuWin.setAnimationStyle(R.style.AnimationPreview);
                mPopuWin.setFocusable(true);
                mPopuWin.update();
                mPopuWin.showAtLocation(mTouchView, Gravity.CENTER, -mOldOffsetX, -mOldOffsetY);

                // TODO
                mIconImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));

                catchSettingTableDismiss();
            } else {
                mTag = 0;
            }
        }
    };

    private void catchSettingTableDismiss() {
        mTimer = new Timer();
        mTask = new TimerTask() {

            @Override
            public void run() {
                if (mPopuWin == null || !mPopuWin.isShowing()) {
                    handler.sendEmptyMessage(0x0);
                }
            }
        };

        mTimer.schedule(mTask, 0, 100);
    }

    private void clearTimerThead() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            mIconImageView.setBackgroundDrawable(getResources().getDrawable(R.mipmap.touch_ic));
        }

        ;
    };

    private void hideSettingTable() {
        if (null != mPopuWin) {
            mPopuWin.dismiss();
        }
    }
}