package com.rance.easypoint.easypoint.common;

/**
 * Created by Administrator on 2016/9/20.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.rance.easypoint.easypoint.model.ApkModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取手机上apk文件信息类，主要是判断是否安装再手机上了，安装的版本比较现有apk版本信息
 *
 * @author Dylan
 */
public class ApkSearchUtils {

    public static void FindAllAPKFile(final Context mContext, final OnFindAllAPKClick mOnFindAllAPKClick) {
        final List<ApkModel> mApkModels = new ArrayList<ApkModel>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = Environment.getExternalStorageDirectory();
                FindAllAPKFile(mContext, file, mApkModels, mOnFindAllAPKClick);
            }
        }).start();
    }

    /**
     * 运用递归的思想，递归去找每个目录下面的apk文件
     */
    public static void FindAllAPKFile(Context mContext, File file, List<ApkModel> mApkModels, OnFindAllAPKClick mOnFindAllAPKClick) {
        // SD卡上的文件目录
        if (file.isFile()) {
            String name_s = file.getName();
            ApkModel mApkModel = new ApkModel();
            String apk_path = null;
            if (name_s.toLowerCase().endsWith(".apk")) {
                apk_path = file.getAbsolutePath();// apk文件的绝对路劲
                PackageManager pm = mContext.getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                /**获取apk的图标 */
                appInfo.sourceDir = apk_path;
                appInfo.publicSourceDir = apk_path;
                String appName = packageInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString();
                mApkModel.setAppName(appName);
                Drawable apk_icon = appInfo.loadIcon(pm);
                mApkModel.setApkIcon(apk_icon);
                /** 得到包名 */
                String packageName = packageInfo.packageName;
                mApkModel.setPackageName(packageName);
                /** apk的绝对路劲 */
                mApkModel.setFilePath(file.getAbsolutePath());
                /** apk的版本名称 String */
                String versionName = packageInfo.versionName;
                mApkModel.setVersionName(versionName);
                /** apk的版本号码 int */
                int versionCode = packageInfo.versionCode;
                mApkModel.setVersionCode(versionCode);
                /**安装处理类型*/
                int type = doInstallType(pm, packageName, versionCode);
                mApkModel.setInstalled(type);
                mApkModel.setType(Constants.PERSONAL_APPLICATION);
                mApkModels.add(mApkModel);
                mOnFindAllAPKClick.complete(mApkModels);
            }
        } else {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File file_str : files) {
                    FindAllAPKFile(mContext, file_str, mApkModels, mOnFindAllAPKClick);
                }
            }
        }
    }

    /*
     * 判断该应用是否在手机上已经安装过，有以下集中情况出现
     * 1.未安装，这个时候按钮应该是“安装”点击按钮进行安装
     * 2.已安装，按钮显示“已安装” 可以卸载该应用
     * 3.已安装，但是版本有更新，按钮显示“更新” 点击按钮就安装应用
     * @param pm          PackageManager
     * @param packageName 要判断应用的包名
     * @param versionCode 要判断应用的版本号
     */
    private static int doInstallType(PackageManager pm, String packageName, int versionCode) {
        List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            String pi_packageName = pi.packageName;
            int pi_versionCode = pi.versionCode;
            //如果这个包名在系统已经安装过的应用中存在
            if (packageName.endsWith(pi_packageName)) {
                if (versionCode == pi_versionCode) {
                    return Constants.INSTALLED;
                } else if (versionCode > pi_versionCode) {
                    return Constants.INSTALLED_UPDATE;
                }
            }
        }
        return Constants.UNINSTALLED;
    }

    public static List<ApkModel> getAllApplication(Context mContext) {
        List<ApkModel> mApkModels = new ArrayList<ApkModel>();
        List<PackageInfo> packages = mContext.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            ApkModel tmpInfo = new ApkModel();
            tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString());
            tmpInfo.setPackageName(packageInfo.packageName);
            tmpInfo.setVersionName(packageInfo.versionName);
            tmpInfo.setVersionCode(packageInfo.versionCode);
            tmpInfo.setApkIcon(packageInfo.applicationInfo.loadIcon(mContext.getPackageManager()));
            /**安装处理类型*/
            int type = doInstallType(mContext.getPackageManager(), packageInfo.packageName, packageInfo.versionCode);
            tmpInfo.setInstalled(type);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                tmpInfo.setType(Constants.SYSTEM_APPLICATION);
            } else {
                tmpInfo.setType(Constants.PERSONAL_APPLICATION);
            }
            mApkModels.add(tmpInfo);
        }
        return mApkModels;
    }

    public static boolean install(String apkPath, Context context) {
        // 先判断手机是否有root权限
        if (hasRootPerssion()) {
            // 有root权限，利用静默安装实现
            return clientInstall(apkPath);
        } else {
            // 没有root权限，利用意图进行安装
            File file = new File(apkPath);
            if (!file.exists())
                return false;
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            context.startActivity(intent);
            return true;
        }
    }

    public static boolean uninstall(Context context, String packageName) {
        if (hasRootPerssion()) {
            // 有root权限，利用静默卸载实现
            return clientUninstall(packageName);
        } else {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(uninstallIntent);
            return true;
        }
    }

    /**
     * 判断手机是否有root权限
     */
    private static boolean hasRootPerssion() {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 静默安装
     */
    private static boolean clientInstall(String apkPath) {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 " + apkPath);
            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            PrintWriter.println("pm install -r " + apkPath);
//          PrintWriter.println("exit");
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 静默卸载
     */
    private static boolean clientUninstall(String packageName) {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
            PrintWriter.println("pm uninstall " + packageName);
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    private static boolean returnResult(int value) {
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }

    /**
     * 跳转腾讯应用宝APK下载页面
     *
     * @param mContext
     * @param packageName
     */
    public static void UpdateApplication(Context mContext, String packageName) {
        Uri uri = Uri.parse(Constants.APPLICATION_URL + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }

    /**
     * 删除apk文件
     *
     * @param mContext
     * @param filePath
     */
    public static boolean deleteApplication(Context mContext, String filePath) {
        File file = new File(filePath);
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
                return true;
            }
        } else {
            Toast.makeText(mContext, "apk不存在", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

    public interface OnFindAllAPKClick {
        void complete(List<ApkModel> mApkModels);
    }

}
