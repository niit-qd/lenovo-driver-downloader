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
                boolean result = false;
                if (file.isFile()) {
                    try {
                        FileUtils.copyFile(file, zaos);
                    } catch (IOException e) {
                        logger.warn("failed to writer file:{} to zip file:{}", file, targetFile);
                    }
                }
                logger.debug("add file: {}, result = {}", file, result ? "successful" : "failed");
            });
        } catch (IOException e) {
            logger.warn("failed to create ZipArchiveOutputStream:{}", targetDir);
        }
    }

    public static void main(String[] args) {
        SimpleArchiver simpleArchiver = new SimpleZipArchiver();
        String driverSourceDirPath = "D:\\demo\\dir";
        String driverTargetDirPath = "D:\\demo";
        String driverTargetFileName = "targetZip";
        simpleArchiver.makeArchive(new File(driverSourceDirPath), new File(driverTargetDirPath), driverTargetFileName);
    }
}
