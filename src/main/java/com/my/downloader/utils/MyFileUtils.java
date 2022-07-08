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
