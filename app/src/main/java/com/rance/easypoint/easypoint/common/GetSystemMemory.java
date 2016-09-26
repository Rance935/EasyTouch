package com.rance.easypoint.easypoint.common;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;

public class GetSystemMemory {

	public static long getMemory(ActivityManager am) {
		MemoryInfo mInfo = new MemoryInfo();
		am.getMemoryInfo(mInfo);
		return mInfo.availMem / (1024 * 1024);
	}
}
