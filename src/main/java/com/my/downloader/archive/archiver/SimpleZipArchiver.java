package com.my.downloader.archive.archiver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Slf4j
public class SimpleZipArchiver extends SimpleArchiver {

    private static final Logger logger = LoggerFactory.getLogger(SimpleZipArchiver.class);

    private static final String ZIP_FILE_POSTFIX = "zip";

    @Override
    public void makeArchiveWithSourceFileInfos(Collection<SourceFileInfo> sourceFileInfos, File targetDir, String archiveFileName) {
        if (null == sourceFileInfos) {
            return;
        }
        File targetFile = new File(targetDir, archiveFileName + "." + ZIP_FILE_POSTFIX);
        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(targetFile)) {
            sourceFileInfos.forEach(sourceFileInfo -> {
                if (null == sourceFileInfo) {
                    return;
                }
                File file = sourceFileInfo.getFile();
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, sourceFileInfo.getPostfixFileSubPath());
                try {
                    zaos.putArchiveEntry(zipArchiveEntry);
                } catch (IOException e) {
                    logger.warn("failed to putArchiveEntry.", e);
                }
                long copied = 0;
                if (file.isFile()) {
                    try {
                        copied = FileUtils.copyFile(file, zaos);
                    } catch (IOException e) {
                        copied = -1;
                        logger.warn("failed to writer file:{} to zip file:{}", file, targetFile);
                    }
                }
                logger.debug("add file: {}, copied = {}", file, copied);
                try {
                    zaos.closeArchiveEntry();
                } catch (IOException e) {
                    logger.warn("failed to closeArchiveEntry for file: {}", file);
                }
            });
        } catch (IOException e) {
            boolean deleted = targetFile.delete();
            logger.warn("failed to create zip file :{}, so delete it: {}", targetFile, deleted);
        }
    }

    public static void main(String[] args) {
        SimpleArchiver simpleArchiver = new SimpleZipArchiver();
        String driverSourceDirPath = "D:\\tmp";
        String driverTargetDirPath = "D:\\";
        String driverTargetFileName = "targetZip";
        simpleArchiver.makeArchive(new File(driverSourceDirPath), new File(driverTargetDirPath), driverTargetFileName);
    }
}
