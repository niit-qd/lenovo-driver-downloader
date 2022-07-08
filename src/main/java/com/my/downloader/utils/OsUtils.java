package com.my.downloader.utils;

import java.util.Locale;

/**
 * @author hw
 */
public class OsUtils {

    /**
     * 判断是否是Windows系统
     *
     * @return whether is Windows OperationSystem
     */
    public static boolean isOsWindows() {
        String os = System.getProperty("os.name");
        String windowsTag = "windows";
        return os.toLowerCase(Locale.ROOT).startsWith(windowsTag);
    }

}
