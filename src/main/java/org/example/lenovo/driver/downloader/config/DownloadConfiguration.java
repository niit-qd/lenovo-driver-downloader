package org.example.lenovo.driver.downloader.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author
 */
@Data
public class DownloadConfiguration {

    /**
     * 配置源类型
     */
    public static enum SourceType {
        /**
         * 从url中读取配置
         */
        URL,
        /**
         * json file 从json文件中读取配置
         */
        DriveListFile,
    }

    /**
     * 配置源类型
     */
    private SourceType sourceType;

    /**
     * 配置源类型 {@link #sourceType}是{@link SourceType#URL}时配置
     */
    private String driverListNewUrlPathBase;
    /**
     * 下载目录跟目录
     */
    private String targetBaseFolder;
    /**
     * 驱动列表文件目录名称
     */
    private String driveListFileFolderName = "driveListFile";
    /**
     * 驱动文件目录名称
     */
    private String drivesFolderName = "drives";

    /**
     * 是否追加当前下载时间
     */
    @Getter(AccessLevel.NONE)
    private boolean useDateAsSubFolder;
    /**
     * 是否使用日期作为下载目录的子目录
     *
     * @see #useDateAsSubFolder
     */
    private Date downloadDate;
    /**
     * 日期样式
     *
     * @see #useDateAsSubFolder
     */
    private String saveDatePattern = "yyyyMMddHHmmss";

    /**
     * url参数 searchKey  例如 3977
     *
     * @see #sourceType
     * @see SourceType#URL
     */
    private String parameterSearchKey;
    /**
     * url参数 sysid  例如 42
     *
     * @see #sourceType
     * @see SourceType#URL
     */
    private String parameterSysId;
    /**
     * 驱动列表文件路径 {@link #sourceType}是{@link SourceType#DriveListFile}时配置
     *
     * @see #sourceType
     * @see SourceType#DriveListFile
     */
    private String sourceDriveListFilePath;

    /**
     * 工作线程数
     */
    private int workThreadsCount;

    public boolean getUseDateAsSubFolder() {
        return useDateAsSubFolder;
    }

    /**
     * 获取驱动列表文件目录
     *
     * @return
     */
    public String getDriveListFileFolderName() {
        String saveDateStr = getSaveDateString();
        if (null == saveDateStr) {
            return driveListFileFolderName;
        } else {
            return saveDateStr + File.separator + driveListFileFolderName;
        }
    }

    /**
     * 获取驱动列表文件目录
     *
     * @return
     */
    public String getRealDrivesFolderName() {
        String saveDateStr = getSaveDateString();
        if (null == saveDateStr) {
            return drivesFolderName;
        } else {
            return saveDateStr + File.separator + drivesFolderName;
        }
    }

    /**
     * 获取目录保持时间字符串形式。在{@link #saveDatePattern}有效时可用。
     *
     * @return
     * @throws IllegalArgumentException
     */
    private String getSaveDateString() throws IllegalArgumentException {
        if (useDateAsSubFolder) {
            if (null == downloadDate) {
                return null;
            } else {
                if (StringUtils.isBlank(saveDatePattern)) {
                    return downloadDate.toString();
                }
                return new SimpleDateFormat(saveDatePattern).format(downloadDate);
            }
        } else {
            return null;
        }
    }

}
