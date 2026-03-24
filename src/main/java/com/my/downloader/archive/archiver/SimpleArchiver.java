package com.my.downloader.archive.archiver;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public abstract class SimpleArchiver {
    private static final Logger logger = LoggerFactory.getLogger(SimpleArchiver.class);

    @Getter
    public static class SourceFileInfo {
        private String prefixDirPath;
        private String postfixFileSubPath;

        public void setPrefixDirPath(String prefixDirPath) {
            this.prefixDirPath = prefixDirPath;
            fixPrefixDir();
        }

        public void setPostfixFileSubPath(String postfixFileSubPath) {
            this.postfixFileSubPath = postfixFileSubPath;
            fixPostfixFileSubPath();
        }

        public SourceFileInfo() {
        }

        public SourceFileInfo(@NonNull String prefixDirPath, @NonNull String postfixFileSubPath) {
            this.prefixDirPath = prefixDirPath;
            this.postfixFileSubPath = postfixFileSubPath;
            fixPrefixDir();
            fixPostfixFileSubPath();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            SourceFileInfo that = (SourceFileInfo) o;
            String thisFilePath = new File(prefixDirPath, postfixFileSubPath).getAbsolutePath();
            String thatFilePath = new File(that.prefixDirPath, that.postfixFileSubPath).getAbsolutePath();
            return Objects.equals(thisFilePath, thatFilePath);
        }

        @Override
        public int hashCode() {
            return new File(prefixDirPath, postfixFileSubPath).getAbsolutePath().hashCode();
        }

        @Override
        public String toString() {
            return "SourceFileInfo{" +
                    "prefixDirPath='" + prefixDirPath + '\'' +
                    ", postfixFileSubPath='" + postfixFileSubPath + '\'' +
                    '}';
        }

        public File getFile() {
            return new File(prefixDirPath, postfixFileSubPath);
        }

        private void fixPrefixDir() {
            if (null == prefixDirPath) {
                return;
            }
            prefixDirPath = prefixDirPath.trim();
            if (prefixDirPath.endsWith(File.separator)) {
                prefixDirPath = prefixDirPath.substring(0, prefixDirPath.length() - 1);
            }
        }

        private void fixPostfixFileSubPath() {
            if (null == postfixFileSubPath) {
                return;
            }
            postfixFileSubPath = postfixFileSubPath.trim();
            if (postfixFileSubPath.startsWith(File.separator)) {
                postfixFileSubPath = postfixFileSubPath.substring(File.separator.length());
            }
        }
    }

    public static Set<SourceFileInfo> listSourceFileInfos(File file) {
        if (null == file) {
            return null;
        }
        String prefixDirPath = file.getParentFile().getAbsolutePath();
        Set<SourceFileInfo> sourceFileInfos = new HashSet<>();
        if (file.isFile()) {
            sourceFileInfos.add(new SourceFileInfo(prefixDirPath, file.getName()));
        }
        if (file.isDirectory()) {
            Collection<File> files = FileUtils.listFilesAndDirs(file, FileFilterUtils.fileFileFilter(), FileFilterUtils.directoryFileFilter());
            // FileUtils.listFilesAndDirs会将被查找的目录自身，所以也需要移除。
            files.remove(file);
            int prefixDirPathLength = prefixDirPath.length();
            files.forEach(file0 -> {
                String filePath = file0.getAbsolutePath();
                if (!filePath.startsWith(prefixDirPath)) {
                    logger.warn("current file:{} isn't in dir:{}", filePath, prefixDirPath);
                    return;
                }
                String postfixFileSubPath = filePath.substring(prefixDirPathLength);
                if (postfixFileSubPath.startsWith(File.separator)) {
                    postfixFileSubPath = postfixFileSubPath.substring(File.separator.length());
                }
                sourceFileInfos.add(new SourceFileInfo(prefixDirPath, postfixFileSubPath));
            });
        }
        return sourceFileInfos;
    }

    public void makeArchive(File file, File targetDir, String archiveFileName) {
        if (null == file || !file.exists()) {
            logger.warn("source:{} doesn't exist.", file);
            return;
        }
        if (null == targetDir || !targetDir.exists()) {
            logger.warn("targetDir{} doesn't exist.", targetDir);
            return;
        }
        if (null == archiveFileName) {
            logger.warn("archiveFileName is null, reset it as \"\".");
            archiveFileName = "";
        } else if (archiveFileName.trim().isEmpty()) {
            logger.warn("archiveFileName:{} is blank.", archiveFileName);
        }

        makeArchiveWithSourceFileInfos(listSourceFileInfos(file), targetDir, archiveFileName);
    }

    public void makeArchive(Collection<File> files, File targetDir, String archiveFileName) {
        if (null == files) {
            logger.warn("files is null.");
            return;
        }
        if (null == targetDir || !targetDir.exists()) {
            logger.warn("targetDir{} doesn't exist.", targetDir);
            return;
        }
        if (null == archiveFileName) {
            logger.warn("archiveFileName is null, reset it as \"\".");
            archiveFileName = "";
        } else if (archiveFileName.trim().isEmpty()) {
            logger.warn("archiveFileName:{} is blank.", archiveFileName);
        }

        Collection<SourceFileInfo> sourceFileInfos = new HashSet<>();
        files.forEach(file -> {
            Collection<SourceFileInfo> childSourceFileInfos = listSourceFileInfos(file);
            if (null == childSourceFileInfos) {
                return;
            }
            sourceFileInfos.addAll(childSourceFileInfos);
        });
        makeArchiveWithSourceFileInfos(sourceFileInfos, targetDir, archiveFileName);
    }

    public abstract void makeArchiveWithSourceFileInfos(Collection<SourceFileInfo> sourceFileInfos, File targetDir, String archiveFileName);

}
