# lenovo-driver-downloader

用途，快速省心下载Lenovo&ThinkPad驱动。

### 配置文件简述
```yaml
# application.yml
com:
  example:
    lenovo:
      driver:
        downloader:
          download-configuration:
            # 配置源类型：URL（需要配置driver-list-new-url-path-base）、DriveListFile
            source-type: URL
            # 驱动列表下载的基础url 适配：source-type: URL
            driver-list-new-url-path-base: https://newsupport.lenovo.com.cn/api/drive/drive_listnew
            # 驱动存放目录 适配：source-type: URL
            target-base-folder: target/drives
            # 驱动列表文件路径 适配：source-type: DriveListFile
            # source-drive-list-file-path: target/drives/20220708224501/drives/drive_listnew
            drives-folder-name: drives
            # 是否使用日期作为下载目录的子目录 适配：save-date-pattern
            use-date-as-sub-folder: true
            # 日期样式 适配：use-date-as-sub-folder
            save-date-pattern: yyyyMMddHHmmss
            # url参数
            parameter-search-key: 12357
            parameter-sys-id: 42
            # 工作线程属
            work-threads-count: 10
```

### url参数的获取
url的参数是从官方驱动网站的请求中获取的，需要自行查找。
- Lenovo的驱动和软件下载
  - 页面地址
    [选择产品获取支持](https://newsupport.lenovo.com.cn/notebook.html)
    [选择产品获取支持](https://newsupport.lenovo.com.cn/notebook.html)
    [查找驱动程序及工具软件](https://newsupport.lenovo.com.cn/driveDownloads_index.html)
    [驱动和软件下载 小新 15 2022 (Intel平台:IAL7版)](https://newsupport.lenovo.com.cn/driveList.html?fromsource=driveList&selname=%E5%B0%8F%E6%96%B0%2015%202022%20(Intel%E5%B9%B3%E5%8F%B0:IAL7%E7%89%88))
  - 参数搜索地址
    https://newsupport.lenovo.com.cn/api/drive/drive_listnew?searchKey=15159&sysid=248
    从上面url中获取产品key`searchKey`和系统id`sysid`。`sysid`不存在时表示默认。
    此url，也可用于`ThinkPad`产品驱动的查询。所以本项目使用了该地址进行查询。
- ThinkPad的驱动和软件下载
  - 页面地址
    [请选择您的产品获取支持](https://newthink.lenovo.com.cn/)
    [ThinkPad笔记本](https://newthink.lenovo.com.cn/product.html#series=ThinkPad%E7%AC%94%E8%AE%B0%E6%9C%AC)
    [ThinkPad X13 Gen3 AMD](https://newthink.lenovo.com.cn/driveList.html?selname=ThinkPad%20X13%20Gen3%20AMD)
    [驱动和软件下载]()
  - 参数搜索地址
    https://newthink.lenovo.com.cn/api/ThinkHome/Machine/DriveListInfo?search_key=15272&system_id=248
    从上面url中获取产品key`search_key`和系统id`system_id`。`system_id`不存在时表示默认。
    此url，不可用于`Lenovo`产品驱动的查询。