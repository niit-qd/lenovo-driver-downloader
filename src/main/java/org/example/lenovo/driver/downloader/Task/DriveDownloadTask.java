package org.example.lenovo.driver.downloader.Task;

import com.alibaba.fastjson.JSON;
import com.my.downloader.utils.DownloadUtils;
import com.my.downloader.utils.MyFileUtils;
import com.my.downloader.utils.MyUrlUtils;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
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
import java.net.MalformedURLException;
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
        List<DownloadConfiguration.UrlParameterConfig> urlParameterConfigs = downloadConfiguration.getUrlParameterConfigs();
        if (null == urlParameterConfigs || urlParameterConfigs.isEmpty()) {
            logger.warn("urlParameterConfigs is blank.");
            return;
        }
        int urlParameterConfigsSize = urlParameterConfigs.size();

        // get or download the driver list file
        File baseDrivesFolder = new File(downloadConfiguration.getTargetBaseFolder(), downloadConfiguration.getRealDrivesFolderName());
        for (int i = 0; i < urlParameterConfigsSize; i++) {
            DownloadConfiguration.UrlParameterConfig urlParameterConfig = urlParameterConfigs.get(i);
            logger.info("driver list index = {}/{}: urlParameterConfig = {}", i + 1, urlParameterConfigsSize, urlParameterConfig);
            File driveListResultFile = obtainDriveListFile(downloadConfiguration, i);
            if (null == driveListResultFile) {
                continue;
            }
            String sysId = urlParameterConfig.getParameterSysId();

            if (downloadConfiguration.getDriverSiteType() == DownloadConfiguration.DriverSiteType.Lenovo) {

                // parse driver list file
                LenovoDriverListResult lenovoDriverListResult = parseLenovoDriveListFromFile(driveListResultFile);

                // re-config the base download folder
                if (downloadConfiguration.getUseSameDateForEachUrlParameterConfig()) {
                    baseDrivesFolder = new File(downloadConfiguration.getTargetBaseFolder(), downloadConfiguration.getRealDrivesFolderName());
                }
                File lenovoDriverListFolder = getDriverListFolder(baseDrivesFolder, lenovoDriverListResult, sysId);

                // copy(save) driver list file
                logger.info("lenovoDriverListFolder = {}", lenovoDriverListFolder);
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
                    downloadDriveListByLenovoDriveListResult(lenovoDriverListResult, lenovoDriverListFolder, downloadConfiguration.getRetryTimesWhenFail());
                } catch (Exception e) {
                    logger.error("", e);
                }

            } else if (downloadConfiguration.getDriverSiteType() == DownloadConfiguration.DriverSiteType.ThinkPad) {
                logger.warn("haven't implement the DriverSiteType: {}", downloadConfiguration.getDriverSiteType());
            }
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

    /**
     * 获取指定的下载列表文件
     *
     * @param downloadConfiguration 下载配置
     * @param index                 驱动列表索引 有效范围是[0, {@linkplain DownloadConfiguration#getUrlParameterConfigs()}.size]。
     * @return 驱动下载文件
     */
    private static File obtainDriveListFile(DownloadConfiguration downloadConfiguration, int index) {

        if (null == downloadConfiguration) {
            logger.warn("downloadConfiguration = null");
            return null;
        }

        List<DownloadConfiguration.UrlParameterConfig> urlParameterConfigs = downloadConfiguration.getUrlParameterConfigs();
        if (null == urlParameterConfigs || index >= urlParameterConfigs.size()) {
            logger.warn("index:{} is not in the range of [0, {}].", index, null == urlParameterConfigs ? 0 : urlParameterConfigs.size());
            return null;
        }
        DownloadConfiguration.UrlParameterConfig urlParameterConfig = urlParameterConfigs.get(index);

        DownloadConfiguration.SourceType sourceType = urlParameterConfig.getSourceType();
        if (null == sourceType) {
            logger.warn("sourceType == null");
            return null;
        }

        if (downloadConfiguration.getUseDateAsFolderPathSegment()) {
            downloadConfiguration.setDownloadDate(new Date());
        }

        File driveListFile = null;
        if (sourceType == DownloadConfiguration.SourceType.DriveListFile) {
            driveListFile = new File(urlParameterConfig.getSourceDriveListFilePath());
        } else if (sourceType == DownloadConfiguration.SourceType.URL) {
            String driveListFileUrl = downloadConfiguration.getDriverListNewUrlPathBase();
            String searchKey = urlParameterConfig.getParameterSearchKey();
            String sysId = urlParameterConfig.getParameterSysId();
            String requestUrlParameterKeySearchKey;
            String requestUrlParameterKeySystemId;
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
                driveListFile = DownloadUtils.download(driveListFileUrl, driveListFileSaveFolder, null);
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

    private static String getSubDirectoryPathForDriverListFile(LenovoDriverListResult driveListResult, String sysId) {
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

    private static File getDriverListFolder(File baseDrivesFolder, LenovoDriverListResult driveListResult, String sysId) {
        String subDirectoryPathForDriverListFile = getSubDirectoryPathForDriverListFile(driveListResult, sysId);
        if (StringUtils.isBlank(subDirectoryPathForDriverListFile)) {
            return baseDrivesFolder;
        } else {
            return new File(baseDrivesFolder, subDirectoryPathForDriverListFile);
        }
    }


    /**
     * @param driveListResult        driveListResult
     * @param lenovoDriverListResult 驱动保持保存跟
     */
    private static void downloadDriveListByLenovoDriveListResult(LenovoDriverListResult driveListResult, File lenovoDriverListResult, int retryTimesWhenFail) {

        if (null == driveListResult) {
            logger.warn("parse DriveListResult failed, driveListResult = null");
            return;
        }

        LenovoDriverListResult.DriveData data = driveListResult.getData();
        if (null == data) {
            logger.warn("data = null.");
            return;
        }
        List<LenovoDriverListResult.DrivePart> partList = data.getPartList();
        if (CollectionUtils.isEmpty(partList)) {
            logger.warn("no available drives");
            return;
        }

        List<LenovoDriverListResult.Drive> downloadFailedDrives = new ArrayList<>();

        for (int i = 0; i < partList.size(); i++) {
            LenovoDriverListResult.DrivePart drivePart = partList.get(i);
            logger.info("will download index = {}/{}, driver info = {}", i + 1, partList.size(), drivePart);
            if (null == drivePart) {
                continue;
            }
            String partName = drivePart.getPartName();
            if (StringUtils.isBlank(partName)) {
                continue;
            }
            partName = StringEscapeUtils.unescapeJava(partName);
            File drivePartFolder = new File(lenovoDriverListResult, partName);
            if (!drivePartFolder.isDirectory()) {
                if (!drivePartFolder.mkdirs()) {
                    logger.warn("failed to make directory: {}", drivePartFolder);
                }
            }

            List<LenovoDriverListResult.Drive> driveList = drivePart.getDrivelist();
            if (CollectionUtils.isEmpty(driveList)) {
                return;
            }

            for (int j = 0; j < driveList.size(); j++) {
                LenovoDriverListResult.Drive drive = driveList.get(j);
                logger.info("will down driver {}/{} of {}/{}, name = {}, url = {}",
                        j + 1, driveList.size(), i + 1, partList.size(),
                        null == drive ? null : (drive.getDriverName() + File.separator + drive.getFileName()),
                        null == drive ? null : drive.getFilePath());
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
                        downloadFailedDrives.add(drive);
                        continue;
                    }
                }
                String filePath = drive.getFilePath();
                logger.info("will down, name = {}, url = {}, folder = {}", drive.getDriverName(), filePath, driveFolder);
                if (StringUtils.isBlank(filePath)) {
                    // 某些驱动，存在下载地址为空的情况。"FileType": ""
                    downloadFailedDrives.add(drive);
                    continue;
                }
                int retryTime = 0;
                while (true) {
                    try {
                        // Expect display default progress bar style:
                        // 100% │███████████████████████████████████████████│ 10/10k (0:00:12 / 0:00:00)
                        DownloadUtils.download(filePath, driveFolder, new DownloadUtils.DownloadProgressCallback() {

                            ProgressBarBuilder builder = null;
                            ProgressBar bar = null;

                            @Override
                            public void onDownloadStarted(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, long length) {
                                builder = new ProgressBarBuilder()
                                        .setUnit("b", 1)
                                        .setInitialMax(length);
                                bar = builder.build();
                            }

                            @Override
                            public void onDownloadProgressChanged(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, long progress, long length) {
                                if (null != bar) {
                                    bar.stepTo(progress);
                                }
                            }

                            @Override
                            public void onDownloadCompleted(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, long progress, long length, int result, Exception exception) {

                            }
                        });
                        break;
                    } catch (Exception e) {
                        if (e instanceof MalformedURLException) {
                            logger.error("failed to download driver, maybe the download url is illegal. path = {}, driver: {}", filePath, drive, e);
                            downloadFailedDrives.add(drive);
                            break;
                        } else if (retryTimesWhenFail < 0) {
                            logger.error("failed to download and retry to download driver: {}", drive, e);
                        } else {
                            if (retryTime < retryTimesWhenFail) {
                                retryTime++;
                                logger.error("failed to download and retry({}/{}) to download driver: {}",
                                        retryTime, retryTimesWhenFail, drive, e);
                            } else {
                                logger.error("failed to download and has up to max retry time {}/{}, so give up to download driver: {}",
                                        retryTime, retryTimesWhenFail, drive, e);
                                downloadFailedDrives.add(drive);
                                break;
                            }
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
