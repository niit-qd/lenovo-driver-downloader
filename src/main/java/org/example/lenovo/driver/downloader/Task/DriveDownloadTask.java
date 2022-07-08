package org.example.lenovo.driver.downloader.Task;

import com.alibaba.fastjson.JSON;
import com.my.downloader.utils.DownloadUtils;
import com.my.downloader.utils.MyUrlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.example.lenovo.driver.downloader.config.DownloadConfiguration;
import org.example.lenovo.driver.downloader.model.DriveListResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * @author
 */
public class DriveDownloadTask {

    private static final String DRIVE_LIST_NEW_URL_PATH_BASE = "https://newsupport.lenovo.com.cn/api/drive/drive_listnew";
    private static final String DRIVE_LIST_NEW_URL_PARAMETER_KEY_SEARCHKEY = "searchKey";
    private static final String DRIVE_LIST_NEW_URL_PARAMETER_KEY_SYSID = "sysid";

    private static final Logger logger = LoggerFactory.getLogger(DriveDownloadTask.class);


    public static void downloadDriveList(DownloadConfiguration downloadConfiguration) {


        //
        DriveListResult driveListResult = parseDriveListFromFile(downloadConfiguration, true);
        if (null == driveListResult) {
            logger.warn("driveListResult = null");
            return;
        }

        //
        try {
            downloadDriveListByDriveListResult(downloadConfiguration, driveListResult);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    private static DriveListResult parseDriveListFromFile(DownloadConfiguration downloadConfiguration, boolean copyDriveListFileToTarget) {

        if (null == downloadConfiguration) {
            logger.warn("downloadConfiguration = null");
            return null;
        }

        DownloadConfiguration.SourceType sourceType = downloadConfiguration.getSourceType();
        if (null == sourceType) {
            logger.warn("sourceType == {}", sourceType);
            return null;
        }

        if (downloadConfiguration.getUseDateAsSubFolder()) {
            downloadConfiguration.setDownloadDate(new Date());
        }

        File driveListFile = null;
        if (sourceType == DownloadConfiguration.SourceType.URL) {
            String searchKey = downloadConfiguration.getParameterSearchKey();
            String sysId = downloadConfiguration.getParameterSysId();
            if (StringUtils.isBlank(searchKey)) {
                logger.warn("searchKey = {}", searchKey);
                return null;
            }
            if (StringUtils.isNotBlank(sysId)) {
                logger.warn("searchKey = {}", searchKey);
            }
            String driveListFileUrl = DRIVE_LIST_NEW_URL_PATH_BASE;
            driveListFileUrl = MyUrlUtils.addParameterPariToUrl(driveListFileUrl, DRIVE_LIST_NEW_URL_PARAMETER_KEY_SEARCHKEY, searchKey);
            driveListFileUrl = MyUrlUtils.addParameterPariToUrl(driveListFileUrl, DRIVE_LIST_NEW_URL_PARAMETER_KEY_SEARCHKEY, searchKey);
            File driveListFileSaveFolder = new File(downloadConfiguration.getTargetBaseFolder(), downloadConfiguration.getRealDrivesFolderName());
            try {
                driveListFile = DownloadUtils.download(driveListFileUrl, driveListFileSaveFolder);
            } catch (IOException | URISyntaxException e) {
                logger.error("download drive list file failed. catch exception:", e);
            }
        } else if (sourceType == DownloadConfiguration.SourceType.DriveListFile) {
            File sourceDriveListFile = new File(downloadConfiguration.getSourceDriveListFilePath());
            if (copyDriveListFileToTarget) {
                File driveListFileSaveFolder = new File(downloadConfiguration.getTargetBaseFolder(), downloadConfiguration.getRealDrivesFolderName());
                if (StringUtils.equals(sourceDriveListFile.getParentFile().getAbsolutePath(), driveListFileSaveFolder.getAbsolutePath())) {
                    driveListFile = sourceDriveListFile;
                } else {
                    try {
                        FileUtils.copyFileToDirectory(sourceDriveListFile, driveListFileSaveFolder);
                        driveListFile = new File(driveListFileSaveFolder, sourceDriveListFile.getName());
                    } catch (IOException e) {
                        logger.error("failed copy drive list file. catch exception:", e);
                    }
                }
            } else {
                driveListFile = sourceDriveListFile;
            }
        }
        if (null == driveListFile) {
            logger.warn("can not get the drive list file.");
            return null;
        }

        String driveListJSONString = null;
        try {
            driveListJSONString = FileUtils.readFileToString(driveListFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("read {} failed. catch exception:", driveListFile, e);
            return null;
        }
        DriveListResult driveListResult = JSON.parseObject(driveListJSONString, DriveListResult.class);
        return driveListResult;
    }


    /**
     * @param downloadConfiguration
     * @param driveListResult
     */
    private static void downloadDriveListByDriveListResult(DownloadConfiguration downloadConfiguration, DriveListResult driveListResult)
            throws Exception {
        if (null == downloadConfiguration) {
            logger.warn("downloadConfiguration = null");
            return;
        }

        if (null == driveListResult) {
            logger.warn("parse DriveListResult failed, driveListResult = null");
            return;
        }

        //
        File drivesFolder = new File(downloadConfiguration.getTargetBaseFolder(), downloadConfiguration.getRealDrivesFolderName());

        DriveListResult.DriveData data = driveListResult.getData();
        if (null == data) {
            logger.warn("data = null.");
            return;
        }

        try {
            String nodeCode = data.getDriverSeriouses().get(0).getNodeCode();
            drivesFolder = new File(drivesFolder, nodeCode);
        } catch (Exception e) {
            logger.warn("cannot get the product host name, so use the default folder. ignore the exception below:", e);
        }
        try {
            String nodeCode = data.getDriverMTList().get(0).getNodeCode();
            drivesFolder = new File(drivesFolder, nodeCode);
        } catch (Exception e) {
            logger.warn("cannot get the product type, so use the default folder. ignore the exception below:", e);
        }
        try {
            String osName = data.getDefaultOSes().get(0).getNAME();
            drivesFolder = new File(drivesFolder, osName);
        } catch (Exception e) {
            logger.warn("cannot get the OS name, so use the default folder. ignore the exception below:", e);
        }
        List<DriveListResult.DrivePart> partList = data.getPartList();
        if (CollectionUtils.isEmpty(partList)) {
            logger.warn("no available drives");
            return;
        }

        for (DriveListResult.DrivePart drivePart : partList) {
            if (null == drivePart) {
                continue;
            }
            String partName = drivePart.getPartName();
            if (StringUtils.isBlank(partName)) {
                continue;
            }
            partName = StringEscapeUtils.unescapeJava(partName);
            File drivePartFolder = new File(drivesFolder, drivePart.getPartName());
            if (!drivePartFolder.isDirectory()) {
                drivePartFolder.mkdirs();
            }

            List<DriveListResult.Drive> driveList = drivePart.getDrivelist();
            if (CollectionUtils.isEmpty(driveList)) {
                return;
            }
            for (DriveListResult.Drive drive : driveList) {
                if (null == drive) {
                    continue;
                }
                String driverName = drive.getDriverName();
                driverName = driverName.replace("/", "_");
                driverName = StringEscapeUtils.unescapeJava(driverName);
                File driveFolder = new File(drivePartFolder, driverName);
                if (!driveFolder.isDirectory()) {
                    driveFolder.mkdirs();
                }
                String filePath = drive.getFilePath();
                logger.debug("down: {}, folder: {}", filePath, driveFolder);
                DownloadUtils.download(filePath, driveFolder);
            }
        }

    }


}
