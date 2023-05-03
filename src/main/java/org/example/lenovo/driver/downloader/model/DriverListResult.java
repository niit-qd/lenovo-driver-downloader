package org.example.lenovo.driver.downloader.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class DriverListResult {
    private String statusCode;
    private String message;
    private DriveData data;

    @Data
    public static class DriveData {
        private Integer localOSID;
        @JSONField(name = "defaultOS")
        private List<DefaultOS> defaultOSes;
        private List<DriveOS> osList;
        private List<DrivePart> partList;
        private Integer driveListCount;
        @JSONField(name = "driverSerious")
        private List<DriverSerious> driverSeriouses;
        private List<DriverMT> driverMTList;
    }

    @Data
    public static class DefaultOS {
        private String ID;
        private String NAME;
        private String Status;
        private String Memo;
        private String OrderNumber;
        private String CreateBy;
        private String CreateTime;
        private String UpdateBy;
        private String UpdateTime;
        private String ProductLineList;
    }

    @Data
    public static class DriveOS {
        private String OSID;
        private String OrderNumber;
        private String OSName;
    }

    @Data
    public static class DrivePart {
        private String PartID;
        private String PartName;
        private String OrderNum;
        private List<Drive> drivelist;
    }

    @Data
    public static class Drive {
        private String DriverEdtionId;
        private String DriverName;
        private String DriverCode;
        private String Version;
        private String PartID;
        private String HisCount;
        private String ActiveTime;
        private String HardwareId;
        private String InstallCode;
        private String SystemBus;
        private String IsIssued;
        private String DriverIssuedDateTime;
        private String CreateTime;
        private String UpdateTime;
        private String Remark;
        private String InstallOverTime;
        private String ISDCH;
        private String CoreVersion;
        private String FileName;
        private String FileSize;
        private String FilePath;
        private String FileType;
    }

    @Data
    public static class DriverSerious {
        private String ProductTreeId;
        private String NodeCode;
        private String PicturePath;
        private String MarketTime;
        private String MEMO;
    }

    @Data
    public static class DriverMT {
        private String ProductTreeId;
        private String NodeCode;
    }
}
