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

    public static enum SourceType {
        /**
         * a search key, such as 3977
         */
        URL,
        /**
         * json file
         */
        DriveListFile,
    }

    /**
     * 下载源
     */
    private SourceType sourceType;
    /**
     * 下载目录跟目录
     */
    private String targetBaseFolder;
    /**
     * 工作线程数
     */
    private int workThreadsCount;

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
     * 下载日期
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
     * url参数 searchKey
     *
     * @see #sourceType
     */
    private String parameterSearchKey;
    /**
     * url参数 sysid
     *
     * @see #sourceType
     */
    private String parameterSysId;
    /**
     * 源驱动列表文件路径
     *
     * @see #sourceType
     */
    private String sourceDriveListFilePath;

    public boolean getUseDateAsSubFolder() {
        return useDateAsSubFolder;
    }

    public String getDriveListFileFolderName() {
        String saveDateStr = getSaveDateString();
        if (null == saveDateStr) {
            return driveListFileFolderName;
        } else {
            return saveDateStr + File.separator + driveListFileFolderName;
        }
    }

    public String getRealDrivesFolderName() {
        String saveDateStr = getSaveDateString();
        if (null == saveDateStr) {
            return drivesFolderName;
        } else {
            return saveDateStr + File.separator + drivesFolderName;
        }
    }

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
