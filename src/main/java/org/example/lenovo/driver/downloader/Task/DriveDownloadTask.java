package org.example.lenovo.driver.downloader.Task;

import com.alibaba.fastjson.JSON;
import com.my.downloader.utils.DownloadUtils;
import com.my.downloader.utils.MyFileUtils;
import com.my.downloader.utils.MyUrlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.example.lenovo.driver.downloader.config.DownloadConfiguration;
import org.example.lenovo.driver.downloader.model.DriverListResult;
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
 * @author hw
 */
public class DriveDownloadTask {

    private static final String DRIVE_LIST_NEW_URL_PARAMETER_KEY_SEARCH_KEY = "searchKey";
    private static final String DRIVE_LIST_NEW_URL_PARAMETER_KEY_SYS_ID = "sysid";

    private static final Logger logger = LoggerFactory.getLogger(DriveDownloadTask.class);


    public static void downloadDriveList(DownloadConfiguration downloadConfiguration) {
        // get or download the driver list file
        File driveListResultFile = obtainDriveListFile(downloadConfiguration);
        if (null == driveListResultFile) {
            logger.warn("driveListResultFile = null");
            return;
        }

        // parse driver list file
        DriverListResult driverListResult = parseDriveListFromFile(driveListResultFile);

        // copy(save) driver list file
        File driverListFolder = getDriverListFolder(downloadConfiguration, driverListResult);
        if (null != driverListFolder &&
                !StringUtils.equals(driveListResultFile.getParentFile().getAbsolutePath(), driverListFolder.getAbsolutePath())) {
            try {
                FileUtils.copyFileToDirectory(driveListResultFile, driverListFolder);
            } catch (IOException e) {
                logger.error("failed copy drive list file. catch exception:", e);
            }
        }

        // download drivers
        try {
            downloadDriveListByDriveListResult(downloadConfiguration, driverListResult);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    private static DriverListResult parseDriveListFromFile(File driveListResultFile) {
        if (null == driveListResultFile || !driveListResultFile.isFile()) {
            return null;
        }

        String driveListJSONString;
        try {
            driveListJSONString = FileUtils.readFileToString(driveListResultFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("read {} failed. catch exception:", driveListResultFile, e);
            return null;
        }
        return JSON.parseObject(driveListJSONString, DriverListResult.class);
    }

    private static File obtainDriveListFile(DownloadConfiguration downloadConfiguration) {

        if (null == downloadConfiguration) {
            logger.warn("downloadConfiguration = null");
            return null;
        }

        DownloadConfiguration.SourceType sourceType = downloadConfiguration.getSourceType();
        if (null == sourceType) {
            logger.warn("sourceType == null");
            return null;
        }

        if (downloadConfiguration.getUseDateAsSubFolder()) {
            downloadConfiguration.setDownloadDate(new Date());
        }

        File driveListFile = null;
        if (sourceType == DownloadConfiguration.SourceType.DriveListFile) {
            driveListFile = new File(downloadConfiguration.getSourceDriveListFilePath());
        } else if (sourceType == DownloadConfiguration.SourceType.URL) {
            String driveListFileUrl = downloadConfiguration.getDriverListNewUrlPathBase();
            String searchKey = downloadConfiguration.getParameterSearchKey();
            String sysId = downloadConfiguration.getParameterSysId();
            if (StringUtils.isBlank(searchKey)) {
                logger.warn("searchKey = {}", searchKey);
                return null;
            } else {
                driveListFileUrl = MyUrlUtils.addParameterPariToUrl(driveListFileUrl, DRIVE_LIST_NEW_URL_PARAMETER_KEY_SEARCH_KEY, searchKey);
            }
            if (StringUtils.isNotBlank(sysId)) {
                logger.warn("searchKey = {}", searchKey);
            } else {
                driveListFileUrl = MyUrlUtils.addParameterPariToUrl(sysId, DRIVE_LIST_NEW_URL_PARAMETER_KEY_SYS_ID, searchKey);
            }
            File driveListFileSaveFolder = FileUtils.getTempDirectory();
            try {
                driveListFile = DownloadUtils.download(driveListFileUrl, driveListFileSaveFolder);
            } catch (IOException | URISyntaxException e) {
                logger.error("download drive list file failed. catch exception:", e);
            }
        }
        if (null == driveListFile) {
            logger.warn("can not get the drive list file.");
            return null;
        }
        return driveListFile;
    }

    private static String getSubDirectoryPathForDriverListFile(DownloadConfiguration downloadConfiguration, DriverListResult driveListResult) {
        DriverListResult.DriveData data = driveListResult.getData();
        if (null == data) {
            logger.warn("data = null.");
            return null;
        }

        StringBuilder subDirPathSb = new StringBuilder();
        try {
            String nodeCode = data.getDriverSeriouses().get(0).getNodeCode();
            subDirPathSb.append(nodeCode).append(File.separator);
        } catch (Exception e) {
            logger.warn("cannot get the product host name, so use the default folder. ignore the exception below:", e);
        }
        String osName = null;
        String sysId = null == downloadConfiguration ? null : downloadConfiguration.getParameterSysId();
        if (StringUtils.isBlank(sysId)) {
            try {
                osName = data.getDefaultOSes().get(0).getNAME();
            } catch (Exception e) {
                logger.warn("cannot get the OS name, so use the default folder. ignore the exception below:", e);
            }
        } else {
            sysId = sysId.trim();
            List<DriverListResult.DriveOS> osList = data.getOsList();
            if (null != osList && !osList.isEmpty()) {
                for (DriverListResult.DriveOS os : osList) {
                    if (null == os) {
                        continue;
                    }
                    String osId = os.getOSID();
                    if (StringUtils.equals(sysId, osId)) {
                        osName = os.getOSName();
                    }
                }
            }
        }
        if (StringUtils.isBlank(osName)) {
            osName = "UnknownOS";
        }
        subDirPathSb.append(osName);

        return subDirPathSb.toString();
    }

    private static File getDriverListFolder(DownloadConfiguration downloadConfiguration, DriverListResult driveListResult) {
        String subDirectoryPathForDriverListFile = getSubDirectoryPathForDriverListFile(downloadConfiguration, driveListResult);
        File drivesFolder = null == downloadConfiguration ? null :
                new File(downloadConfiguration.getTargetBaseFolder(), downloadConfiguration.getRealDrivesFolderName());
        if (StringUtils.isBlank(subDirectoryPathForDriverListFile)) {
            return drivesFolder;
        } else {
            return new File(drivesFolder, subDirectoryPathForDriverListFile);
        }
    }


    /**
     * @param downloadConfiguration downloadConfiguration
     * @param driveListResult       driveListResult
     */
    private static void downloadDriveListByDriveListResult(DownloadConfiguration downloadConfiguration, DriverListResult driveListResult)
            throws Exception {
        if (null == downloadConfiguration) {
            logger.warn("downloadConfiguration = null");
            return;
        }

        if (null == driveListResult) {
            logger.warn("parse DriveListResult failed, driveListResult = null");
            return;
        }

        DriverListResult.DriveData data = driveListResult.getData();
        if (null == data) {
            logger.warn("data = null.");
            return;
        }
        File driverListFolder = getDriverListFolder(downloadConfiguration, driveListResult);
        List<DriverListResult.DrivePart> partList = data.getPartList();
        if (CollectionUtils.isEmpty(partList)) {
            logger.warn("no available drives");
            return;
        }
        for (DriverListResult.DrivePart drivePart : partList) {
            if (null == drivePart) {
                continue;
            }
            String partName = drivePart.getPartName();
            if (StringUtils.isBlank(partName)) {
                continue;
            }
            partName = StringEscapeUtils.unescapeJava(partName);
            File drivePartFolder = new File(driverListFolder, partName);
            if (!drivePartFolder.isDirectory()) {
                if (!drivePartFolder.mkdirs()) {
                    logger.warn("failed to make directory: {}", drivePartFolder);
                }
            }

            List<DriverListResult.Drive> driveList = drivePart.getDrivelist();
            if (CollectionUtils.isEmpty(driveList)) {
                return;
            }
            for (DriverListResult.Drive drive : driveList) {
                if (null == drive) {
                    continue;
                }
                String driverName = drive.getDriverName();
                driverName = driverName.replace("/", "_");
                driverName = StringEscapeUtils.unescapeJava(driverName);
                File driveFolder = new File(drivePartFolder, driverName);
                driveFolder = new File(MyFileUtils.fixIllegalCharactersInFilePathName(driveFolder.getPath()));
                if (!driveFolder.isDirectory()) {
                    if (!driveFolder.mkdirs()) {
                        logger.warn("failed to make directory: {}", driveFolder);
                    }
                }
                String filePath = drive.getFilePath();
                logger.debug("down: {}, folder: {}", filePath, driveFolder);
                DownloadUtils.download(filePath, driveFolder);
            }
        }

    }


}
