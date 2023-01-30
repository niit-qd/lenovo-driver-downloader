package com.my.downloader.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author
 */
public class MyFileUtils {
    /**
     * 获取相对路径
     *
     * @param baseFile            如果是不是目录，则返回其父目录进行比较
     * @param file
     * @param forceBaseFileFolder 强制认为baseFile是目录。
     * @return
     */
    public static String getRelativePath(File baseFile, File file, boolean forceBaseFileFolder) {
        if (null == baseFile || null == file) {
            return null;
        }

        String baseFilePath = (forceBaseFileFolder || baseFile.isDirectory()) ? baseFile.getAbsolutePath() : baseFile.getParentFile().getAbsolutePath();
        String filePath = file.getAbsolutePath();

//        // file 在 baseFile 的父目录中
//        int index = filePath.indexOf(baseFilePath);
//        if (index != -1) {
//            return filePath.substring(baseFilePath.length() +1);
//        }

        // baseFile 在 file 的父目录中
        String splitter = (OsUtils.isOsWindows() ? "\\" : "") + File.separator;
        String[] baseFilePathSegments = baseFilePath.split(splitter);
        String[] filePathSegments = filePath.split(splitter);
        int index = 0;
        while (index < baseFilePathSegments.length && index < filePathSegments.length) {
            if (StringUtils.equals(baseFilePathSegments[index], filePathSegments[index])) {
                index++;
            } else {
                break;
            }
        }
        int count = index;
        if (count == 0) {
            return file.getAbsolutePath();
        }
        StringBuilder sb = new StringBuilder();

        int times = baseFilePathSegments.length - count;
        if (times > 0) {
            for (int i = 0; i < times; i++) {
                sb.append("..").append(File.separator);
            }
        }
        sb.append(filePathSegments[count]);
        for (int i = count + 1; i < filePathSegments.length; i++) {
            sb.append(File.separator).append(filePathSegments[i]);
        }
        return sb.toString();
    }

    /**
     * 替换文件路径子段上的非法字符：\/:*?"<>|。替换符是单个的空字符串" "。
     *
     * @param filePath
     * @return
     */
    public static String fixIllegalCharactersInFilePathName(String filePath) {
        return fixIllegalCharactersInFilePathName(filePath, " ");
    }

    /**
     * 替换文件路径子段上的非法字符：\/:*?"<>|
     *
     * @param filePath
     * @param illegalCharacterReplacement 如果是null，则使用空字符""代替。
     * @return
     */
    public static String fixIllegalCharactersInFilePathName(String filePath, String illegalCharacterReplacement) {
        if (null == filePath) {
            return null;
        }
        filePath = filePath.trim();
        String splitRegex = File.separator;
        // Windows下的“\”在regex表达式中需要转义为“\\”，即"\\\\"。
        if ("\\".equals(File.separator)) {
            splitRegex = "\\\\";
        }
        String[] filePathSegments = filePath.split(splitRegex);
        StringBuilder sb = new StringBuilder();

        // 如果是绝对路径的第一段，先追加盘符。
        int startIndex = 0;
        if (filePathSegments.length > 0) {
            String seg0 = filePathSegments[0];
            // 1. Windows系统路径。根，例如：C:/
            if (seg0.contains(":") && seg0.indexOf(":") == seg0.lastIndexOf(":")) {
                int indexOfColon = seg0.indexOf(":");
                sb.append(seg0.substring(0, indexOfColon)).append(File.separator);
                // 后面的部分要重新追加
                filePathSegments[0] = seg0.substring(indexOfColon + 1);
                startIndex = 0;
            }
            // 2. Linux 路径。根：/
            else if ("".equals(seg0)) {
                sb.append(File.separator);
                startIndex = 1;
            }
        }

        // 替换
        if (null == illegalCharacterReplacement) {
            illegalCharacterReplacement = "";
        }
        for (int i = startIndex; i < filePathSegments.length; i++) {
            String seg = filePathSegments[i];
            seg = seg.replaceAll("[/:*?\"<>|]+", illegalCharacterReplacement); // 非法字符：\/:*?"<>|，又要上面已经使用/进行split，所以，可以忽略/。
            sb.append(seg);
            if (i < filePathSegments.length - 1) {
                sb.append(File.separator);
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        File f1 = new File("C:/a/b/c");
        File f2 = new File("C:/a/d/m");
        f1 = new File("/a/b/c");
        f2 = new File("/a/d/m");
        System.out.println(f1);
        System.out.println(f2);
        System.out.println("rel = " + getRelativePath(f1, f2, false));
    }
}
