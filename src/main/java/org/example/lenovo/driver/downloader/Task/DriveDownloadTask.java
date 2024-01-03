package org.example.lenovo.driver.downloader.Task;

import com.alibaba.fastjson.JSON;
import com.my.downloader.utils.DownloadUtils;
import com.my.downloader.utils.MyFileUtils;
import com.my.downloader.utils.MyUrlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.example.lenovo.driver.downloader.config.DownloadConfiguration;
import org.example.lenovo.driver.downloader.model.LenovoDriverListResult;
import org.example.lenovo.driver.downloader.url.DriverListUrlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author hw
 */
public class DriveDownloadTask {

    private static final Logger logger = LoggerFactory.getLogger(DriveDownloadTask.class);


    public static void downloadDriveList(DownloadConfiguration downloadConfiguration) {
        // get or download the driver list file
        File driveListResultFile = obtainDriveListFile(downloadConfiguration);
        if (null == driveListResultFile) {
            logger.warn("driveListResultFile = null");
            return;
        }

        if (downloadConfiguration.getDriverSiteType() == DownloadConfiguration.DriverSiteType.Lenovo) {
            // parse driver list file
            LenovoDriverListResult lenovoDriverListResult = parseLenovoDriveListFromFile(driveListResultFile);

            // copy(save) driver list file
            File lenovoDriverListFolder = getDriverListFolder(downloadConfiguration, lenovoDriverListResult);
            if (null != lenovoDriverListFolder &&
                    !StringUtils.equals(driveListResultFile.getParentFile().getAbsolutePath(), lenovoDriverListFolder.getAbsolutePath())) {
                try {
                    FileUtils.copyFileToDirectory(driveListResultFile, lenovoDriverListFolder);
                } catch (IOException e) {
                    logger.error("failed copy drive list file. catch exception:", e);
                }
            }

            // download drivers
            try {
                downloadDriveListByLenovoDriveListResult(downloadConfiguration, lenovoDriverListResult);
            } catch (Exception e) {
                logger.error("", e);
            }
        } else if (downloadConfiguration.getDriverSiteType() == DownloadConfiguration.DriverSiteType.ThinkPad) {
            logger.warn("haven't implement the DriverSiteType: {}", downloadConfiguration.getDriverSiteType());
            return;
        } else {
            return;
        }

    }

    private static LenovoDriverListResult parseLenovoDriveListFromFile(File lenovoDriveListResultFile) {
        if (null == lenovoDriveListResultFile || !lenovoDriveListResultFile.isFile()) {
            return null;
        }

        String driveListJSONString;
        try {
            driveListJSONString = FileUtils.readFileToString(lenovoDriveListResultFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("read {} failed. catch exception:", lenovoDriveListResultFile, e);
            return null;
        }
        return JSON.parseObject(driveListJSONString, LenovoDriverListResult.class);
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
            String requestUrlParameterKeySearchKey = null;
            String requestUrlParameterKeySystemId = null;
            if (downloadConfiguration.getDriverSiteType() == DownloadConfiguration.DriverSiteType.Lenovo) {
                requestUrlParameterKeySearchKey = DriverListUrlConstants.LenovoUrl.PARAMETER_KEY_SEARCH_KEY;
                requestUrlParameterKeySystemId = DriverListUrlConstants.LenovoUrl.PARAMETER_KEY_SYS_ID;
                if (StringUtils.isBlank(driveListFileUrl)) {
                    driveListFileUrl = DriverListUrlConstants.LenovoUrl.REQUEST_URL_BASE;
                }
            } else if (downloadConfiguration.getDriverSiteType() == DownloadConfiguration.DriverSiteType.ThinkPad) {
                requestUrlParameterKeySearchKey = DriverListUrlConstants.ThinkPadUrl.PARAMETER_KEY_SEARCH_KEY;
                requestUrlParameterKeySystemId = DriverListUrlConstants.ThinkPadUrl.PARAMETER_KEY_SYS_ID;
                if (StringUtils.isBlank(driveListFileUrl)) {
                    driveListFileUrl = DriverListUrlConstants.ThinkPadUrl.REQUEST_URL_BASE;
                }
            } else {
                return null;
            }
            if (StringUtils.isBlank(searchKey)) {
                logger.warn("searchKey = {}", searchKey);
                return null;
            } else {
                driveListFileUrl = MyUrlUtils.addParameterPariToUrl(driveListFileUrl, requestUrlParameterKeySearchKey, searchKey);
            }
            if (StringUtils.isBlank(sysId)) {
                logger.warn("sysId = {}", sysId);
            } else {
                driveListFileUrl = MyUrlUtils.addParameterPariToUrl(driveListFileUrl, requestUrlParameterKeySystemId, sysId);
            }
            logger.info("driveListFileUrl = {}", driveListFileUrl);
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

    private static String getSubDirectoryPathForDriverListFile(DownloadConfiguration downloadConfiguration, LenovoDriverListResult driveListResult) {
        LenovoDriverListResult.DriveData data = driveListResult.getData();
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
            List<LenovoDriverListResult.DriveOS> osList = data.getOsList();
            if (null != osList && !osList.isEmpty()) {
                for (LenovoDriverListResult.DriveOS os : osList) {
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

    private static File getDriverListFolder(DownloadConfiguration downloadConfiguration, LenovoDriverListResult driveListResult) {
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
    private static void downloadDriveListByLenovoDriveListResult(DownloadConfiguration downloadConfiguration, LenovoDriverListResult driveListResult)
            throws Exception {
        if (null == downloadConfiguration) {
            logger.warn("downloadConfiguration = null");
            return;
        }

        if (null == driveListResult) {
            logger.warn("parse DriveListResult failed, driveListResult = null");
            return;
        }

        LenovoDriverListResult.DriveData data = driveListResult.getData();
        if (null == data) {
            logger.warn("data = null.");
            return;
        }
        File driverListFolder = getDriverListFolder(downloadConfiguration, driveListResult);
        List<LenovoDriverListResult.DrivePart> partList = data.getPartList();
        if (CollectionUtils.isEmpty(partList)) {
            logger.warn("no available drives");
            return;
        }
        for (LenovoDriverListResult.DrivePart drivePart : partList) {
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

            List<LenovoDriverListResult.Drive> driveList = drivePart.getDrivelist();
            if (CollectionUtils.isEmpty(driveList)) {
                return;
            }

            List<LenovoDriverListResult.Drive> downloadFailedDrives = new ArrayList<>();
            for (LenovoDriverListResult.Drive drive : driveList) {
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
                logger.info("will down, name = {}, url = {}, folder = {}", drive.getDriverName(), filePath, driveFolder);
                int retryTime = 0;
                while (true) {
                    try {
                        DownloadUtils.download(filePath, driveFolder);
                        break;
                    } catch (Exception e) {
                        if (downloadConfiguration.getRetryTimesWhenFail() < 0) {
                            logger.error("failed to download and retry to download driver: {}", drive, e);
                        } else {
                            if (retryTime < downloadConfiguration.getRetryTimesWhenFail()) {
                                retryTime++;
                                logger.error("failed to download and retry({}/{}) to download driver: {}",
                                        retryTime, downloadConfiguration.getRetryTimesWhenFail(), drive, e);
                            } else {
                                logger.error("failed to download and has up to max retry time {}/{}, so give up to download driver: {}",
                                        retryTime, downloadConfiguration.getRetryTimesWhenFail(), drive, e);
                                downloadFailedDrives.add(drive);
                                break;
                            }
                        }
                    }
                }
            }

            // report
            logger.info("--------------------------------------------------------------------------------------------");
            if (downloadFailedDrives.isEmpty()) {
                logger.info("all drivers are downloaded successful.");
            } else {
                logger.warn("{} drivers are downloaded failed: {}", downloadFailedDrives.size(),
                        downloadFailedDrives.stream().filter(Objects::nonNull)
                                .map(drive -> drive.getDriverName() + ":" + drive.getVersion() + ":" + drive.getFilePath())
                                .collect(Collectors.toList())
                );
            }
            logger.info("--------------------------------------------------------------------------------------------");
        }

    }


}
