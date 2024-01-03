package org.example.lenovo.driver.downloader.url;

public class DriverListUrlConstants {

    /**
     * e.g. https://newsupport.lenovo.com.cn/api/drive/drive_listnew?searchKey=3970&sysid=4
     */
    public static class LenovoUrl {

        public static final String REQUEST_URL_BASE = "https://newsupport.lenovo.com.cn/api/drive/drive_listnew";
        public static final String PARAMETER_KEY_SEARCH_KEY = "searchKey";
        public static final String PARAMETER_KEY_SYS_ID = "sysid";
    }

    /**
     * e.g. https://newthink.lenovo.com.cn/api/ThinkHome/Machine/DriveListInfo?search_key=12357&system_id=42
     */
    public static class ThinkPadUrl {
        public static final String REQUEST_URL_BASE = "https://newthink.lenovo.com.cn/api/ThinkHome/Machine/DriveListInfo";
        public static final String PARAMETER_KEY_SEARCH_KEY = "search_key";
        public static final String PARAMETER_KEY_SYS_ID = "system_id";
    }
}
