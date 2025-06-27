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
            # 驱动下载站点类型: Lenovo, ThinkPad
            driver-site-type: Lenovo
            # 驱动列表下载的基础url 适配：source-type: URL
            driver-list-new-url-path-base: https://newsupport.lenovo.com.cn/api/drive/drive_listnew
            # 驱动存放目录 适配：source-type: URL
            target-base-folder: target/drives
            # 驱动列表文件路径 适配：source-type: DriveListFile
            # source-drive-list-file-path: ../drivers/drive_listnew
            drives-folder-name: drives
            # 是否使用日期作为下载目录的子目录 适配：save-date-pattern
            use-date-as-forder-path-segment: true
            # 是否为每个驱动配置一个不同的下载时间。默认为false。
            use-same-date-for-each-url-parameter-config: false
            # 日期样式 适配：use-date-as-sub-folder
            save-date-pattern: yyyyMMddHHmmss
            url-parameter-configs:
              - # 配置源类型：URL（需要配置driver-list-new-url-path-base）、DriveListFile
                source-type: URL
                # url参数
                parameter-search-key: 10203
                parameter-sys-id: 26
              - # 配置源类型：URL（需要配置driver-list-new-url-path-base）、DriveListFile
                source-type: URL
                # url参数
                parameter-search-key: 10203
                parameter-sys-id: 42
            # 失败后的重试次数 默认负值（无限次）
            retry-times-when-fail: -1
            # 工作线程属
            work-threads-count: 10
```

---

### url参数的获取

url的参数是从官方驱动网站的请求中获取的，需要自行查找。<br>

- Lenovo的驱动和软件下载<br>
    - 页面地址<br>
      [选择产品获取支持](https://newsupport.lenovo.com.cn/notebook.html)<br>
      [选择产品获取支持](https://newsupport.lenovo.com.cn/notebook.html)<br>
      [查找驱动程序及工具软件](https://newsupport.lenovo.com.cn/driveDownloads_index.html)<br>
      [驱动和软件下载 小新 15 2022 (Intel平台:IAL7版)](https://newsupport.lenovo.com.cn/driveList.html?fromsource=driveList&selname=%E5%B0%8F%E6%96%B0%2015%202022%20(Intel%E5%B9%B3%E5%8F%B0:IAL7%E7%89%88))<br>
    - 参数搜索地址<br>
      https://newsupport.lenovo.com.cn/api/drive/drive_listnew?searchKey=15159&sysid=248<br>
      从上面url中获取产品key`searchKey`和系统id`sysid`。`sysid`不存在时表示默认。
      此url，也可用于`ThinkPad`产品驱动的查询。所以本项目使用了该地址进行查询。
- ThinkPad的驱动和软件下载<br>
  从返回的数据结构上来看，这个结构更容易理解。
  笔者比较懒，使用联想的驱动JSON结构完成解析之后，不想再花时间处理了。如果你有时间，可以参考联想的结构自行实现吧。 ^_^
    - 页面地址<br>
      [请选择您的产品获取支持](https://newthink.lenovo.com.cn/)<br>
      [ThinkPad笔记本](https://newthink.lenovo.com.cn/product.html#series=ThinkPad%E7%AC%94%E8%AE%B0%E6%9C%AC)<br>
      [ThinkPad X13 Gen3 AMD](https://newthink.lenovo.com.cn/driveList.html?selname=ThinkPad%20X13%20Gen3%20AMD)<br>
    - 参数搜索地址<br>
      https://newthink.lenovo.com.cn/api/ThinkHome/Machine/DriveListInfo?search_key=15272&system_id=248<br>
      从上面url中获取产品key`search_key`和系统id`system_id`。`system_id`不存在时表示默认。
      此url，不可用于`Lenovo`产品驱动的查询。

---

*由于无法正确获取远程驱动文件的真实大小，所以实际检测进度的时候，计算值是不准确的。*

### 对于部分旧机型无法找到驱动下载页的问题的处理

在[搜索页](https://newthink.lenovo.com.cn/driverdownload.html)，输入关键字，会有，注意请求中有下面这种请求

```http request
https://newthink.lenovo.com.cn/api/ThinkHome/ProductLine/SearchProductLine?search_key=E530&page_index=1&page_size=100
```

这里的关键参数是`search_key`，可以手动将要查询的机型替换该参数的值，请求即可。得到如下形式的返回值：

```json
{
  "statusCode": 200,
  "message": {
    "info": "Success"
  },
  "data": {
    "total": 2,
    "data": [
      {
        "product_line_id": "10203",
        "product_line_name": "ThinkPad E530/E530c",
        "product_line_alias": "ThinkPad E530/E530c",
        "parent_id": "9103",
        "is_last": "1",
        "product_line_position": "0",
        "product_line_image": "https://webdoc.lenovo.com.cn/think/machinepic/thinkpade530_s.jpg",
        "link_id": "-9425",
        "link_sub_id_list": "",
        "link_parent_id": "-7760",
        "link_parent_name": "",
        "product_line_status": "0",
        "product_line": "120",
        "product_line_type": "12",
        "product_line_memo": "",
        "order_number": "54",
        "dirver_logic_type": "0",
        "is_tj": "0",
        "is_top": "0",
        "is_hot": "0",
        "is_driver": "1"
      },
      {
        "product_line_id": "10641",
        "product_line_name": "扬天E5300d",
        "product_line_alias": "扬天E5300d",
        "parent_id": "3492",
        "is_last": "1",
        "product_line_position": "2",
        "product_line_image": "https://webdoc.lenovo.com.cn/think/MachinePic/扬天ei_s.jpg",
        "link_id": "-8942",
        "link_sub_id_list": "",
        "link_parent_id": "647",
        "link_parent_name": "",
        "product_line_status": "2",
        "product_line": "119",
        "product_line_type": "1",
        "product_line_memo": "",
        "order_number": "3460",
        "dirver_logic_type": "1",
        "is_tj": "0",
        "is_top": "0",
        "is_hot": "0",
        "is_driver": "1"
      }
    ]
  }
}
```

这里取值`product_line_id`，作为上面配置参数`parameter-search-key`的值即可。
由于可能有多个值，根据`product_line_name`或`product_line_alias`的值进行判断真正所需要的机型id即可。
如果名称显示为unicode编码，自行转码即可。

---

驱动详情页（ThinkPad）
1. api
    ```shell
    https://newsupport.lenovo.com.cn/api/drive/driver_detailnew?driverID=62483
    ```

2. 示例
    ```json
    {
        "statusCode": 200,
        "message": "success",
        "data": {
            "Data": {
                "Driver": [
                    {
                        "DriverEdtionId": "62483",
                        "DriverName": "Intel\u4e3b\u677f\u82af\u7247\u7ec4\u9a71\u52a8\u7a0b\u5e8f(Windows 8.1 64-bit\/7)",
                        "DriverCode": "DRV201601190013",
                        "DriverId": "5007",
                        "PDriverEdtionId": "0",
                        "EdtionTitle": "",
                        "Status": "1",
                        "Version": "10.1.1.45",
                        "HisCount": "",
                        "ActiveTime": "",
                        "IsEnable": "1         ",
                        "IsMachineCodeShow": "0",
                        "HardwareId": "8086_1901,8086_9D23,8086_A110,8086_A112,8086_A114,8086_A11C,8086_A121,8086_A131,8086_A150",
                        "InstallCode": "-s -norestart",
                        "SystemBus": "\u7cfb\u7edf\u603b\u7ebf",
                        "IsIssued": "",
                        "DriverIssuedDateTime": "2020\/8\/27",
                        "IsLastEdition": "1",
                        "CreateBy": "\u8303\u73a5",
                        "CreateTime": "2020\/9\/1",
                        "UpdateBy": "",
                        "UpdateTime": "2020\/9\/1",
                        "ShowPositionId": "0",
                        "Field3": "",
                        "Field4": "",
                        "Field1": "\/\/SetupChipset.exe",
                        "Field2": "",
                        "Remark": "",
                        "ProductLineList": "120",
                        "InstallOverTime": "9999",
                        "ISDCH": "0",
                        "FTypeId": "1",
                        "CoreVersion": "10.1.1.45",
                        "PLID": "007280",
                        "PartID": "43",
                        "TypeName": "\u4e3b\u677f",
                        "FileName": "intelchip-r07ic03w.exe",
                        "FileType": "exe",
                        "FileSize": "4.81 MB",
                        "FilePath": "https:\/\/newdriverdl.lenovo.com.cn\/newlenovo\/alldriversupload\/62483\/intelchip-r07ic03w.exe?token=tB1cks3YW3EUMN9aA91D4RJZf5K1Pw6N95xNMwEtyJvJjhyoJfdsChYCuOAwbXMQ",
                        "DriverEdtionIdTheNew": "62483"
                    }
                ],
                "History": [
                    {
                        "DriverEdtionId": "9471",
                        "DriverName": "Intel\u4e3b\u677f\u82af\u7247\u7ec4\u9a71\u52a8\u7a0b\u5e8f(Windows 8.1 64-bit\/7)",
                        "DriverCode": "DRV201601190013",
                        "DriverId": "5007",
                        "PDriverEdtionId": "0",
                        "EdtionTitle": "Intel\u4e3b\u677f\u82af\u7247\u7ec4\u9a71\u52a8\u7a0b\u5e8f(Windows 8.1 64-bit\/7)",
                        "Status": "1",
                        "Version": "10.1.1.9",
                        "PartID": "43",
                        "HisCount": "0",
                        "ActiveTime": "",
                        "IsEnable": "1         ",
                        "IsMachineCodeShow": "0",
                        "HardwareId": "8086_1901,8086_9D23,8086_A110,8086_A112,8086_A114,8086_A11C,8086_A121,8086_A131,8086_A150",
                        "InstallCode": "-s -norestart",
                        "SystemBus": "\u7cfb\u7edf\u603b\u7ebf",
                        "IsIssued": "10101",
                        "DriverIssuedDateTime": "2016\/1\/19",
                        "IsLastEdition": "0",
                        "CreateBy": "\u5d14\u6653\u4eac",
                        "CreateTime": "2019\/7\/26",
                        "UpdateBy": "",
                        "UpdateTime": "2019\/7\/26",
                        "ShowPositionId": "0",
                        "Field3": "",
                        "Field4": "",
                        "Field1": "\/\/SetupChipset.exe",
                        "Field2": "",
                        "Remark": "",
                        "ProductLineList": "120",
                        "InstallOverTime": "9999",
                        "ISDCH": "0",
                        "FTypeId": "1",
                        "CoreVersion": "10.1.1.9",
                        "PLID": "007280",
                        "CreateByCode": "cuixj",
                        "UpdateByCode": "cuixj",
                        "FileName": "intelchip[r07ic02w].exe",
                        "FileType": "exe",
                        "FileSize": "2.93 MB",
                        "FilePath": "https:\/\/driverdl.lenovo.com.cn\/think\/download\/driver\/10065\/intelchip[r07ic02w].exe?token=mMetAqgrQvO8-u3e5H1lk_s4giqv1yFj2AsQQkQ45KjXnT8umh6p1RxHAgTbaWxf",
                        "hisUpdateTime": "2025\/3\/3"
                    }
                ],
                "DriverDescribe": [
                    {
                        "FDriverEdtionId": "62483",
                        "FDescriptionContent": "&lt;p&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;text-align: left; color: rgb(0, 0, 0); text-transform: none; text-indent: 0px; letter-spacing: normal; font-size: 12pt; font-style: normal; font-variant: normal; font-weight: 400; text-decoration: none; word-spacing: 0px; display: inline !important; white-space: normal; orphans: 2; float: none; -webkit-text-stroke-width: 0px; background-color: rgb(255, 255, 255);&#39;&gt;\u5b89\u88c5\u65b9\u6cd5:&lt;\/span&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;\/font&gt;&lt;\/p&gt;&lt;ul style=&#39;LIST-STYLE-TYPE: none&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;\u4e0b\u8f7d\u5e76\u5b89\u88c5\uff08\u63a8\u8350\uff09\uff1a &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;1.\u4ee5\u7ba1\u7406\u5458\u7684\u8eab\u4efd\u767b\u5f55 Windows \u64cd\u4f5c\u7cfb\u7edf &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;2.\u5355\u51fb\u7acb\u5373\u4e0b\u8f7d\u6309\u94ae\uff0c\u5f53\u5f39\u51fa\u6587\u4ef6\u4e0b\u8f7d\u5bf9\u8bdd\u6846\u65f6\uff0c\u9009\u62e9\u201c\u4fdd\u5b58\u201d &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;3.\u4e0b\u8f7d\u9a71\u52a8\u7a0b\u5e8f\u5230\u60a8\u6307\u5b9a\u7684\u4f4d\u7f6e &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;4.\u53cc\u51fb\u8fd0\u884c\u5df2\u7ecf\u4e0b\u8f7d\u7684\u9a71\u52a8\u7a0b\u5e8f\u6587\u4ef6\uff0c\u6309\u5c4f\u5e55\u63d0\u793a\u64cd\u4f5c\u5373\u53ef &lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;\u76f4\u63a5\u5b89\u88c5\uff08\u4e0d\u63a8\u8350\uff09\uff1a &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;1.\u4ee5\u7ba1\u7406\u5458\u7684\u8eab\u4efd\u767b\u5f55 Windows \u64cd\u4f5c\u7cfb\u7edf &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;2.\u5355\u51fb\u7acb\u5373\u4e0b\u8f7d\u6309\u94ae\uff0c\u5f53\u5f39\u51fa\u6587\u4ef6\u4e0b\u8f7d\u5bf9\u8bdd\u6846\u65f6\uff0c\u9009\u62e9\u201c\u8fd0\u884c\u201d\u6309\u94ae &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;3.\u7cfb\u7edf\u5c06\u5f39\u51fa\u5b89\u5168\u8b66\u544a\uff0c\u9009\u62e9\u201c\u8fd0\u884c\u201d &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;4.\u6309\u5c4f\u5e55\u63d0\u793a\u64cd\u4f5c\u5373\u53ef\u3002 &amp;nbsp;&amp;nbsp;&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;br&gt;&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;\/ul&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;\/p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;font face=&#39;sans-serif&#39;&gt;\u652f\u6301\u7cfb\u7edf: &lt;\/font&gt;&lt;\/span&gt;&lt;\/p&gt;&lt;ul style=&#39;LIST-STYLE-TYPE: none&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Microsoft Windows 8.1&amp;nbsp; 64-bit&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Microsoft Windows 7&amp;nbsp; 32-bit, 64-bit&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;br&gt;&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;\/ul&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;\/p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;font face=&#39;sans-serif&#39;&gt;\u652f\u6301\u673a\u578b: &lt;\/font&gt;&lt;\/span&gt;&lt;\/p&gt;&lt;ul style=&#39;LIST-STYLE-TYPE: none&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;ThinkPad L460&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;ThinkPad T460p&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;br&gt;&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;\/ul&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;\/p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;font face=&#39;sans-serif&#39;&gt;\u652f\u6301\u90e8\u4ef6: &lt;\/font&gt;&lt;\/span&gt;&lt;\/p&gt;&lt;ul style=&#39;LIST-STYLE-TYPE: none&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Intel(R) 8 Series Chipsets &lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Intel(R) 9 series chipset&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Intel(R) Core(R) M processor family&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;6th Generation Intel(R) Core(TM) processor family&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;5th Generation Intel(R) Core(TM) processor family&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;4th Generation Intel(R) Core(TM) processor family&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;3rd Generation Intel(R) Core(TM) processor family&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;2nd Generation Intel(R) Core(TM) processor family&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;IntelR Atom\/CeleronR\/PentiumR Processor&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Wellsburg&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;Patsburg&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;br&gt;&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;\/ul&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;\/p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;p&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;font face=&#39;sans-serif&#39;&gt;\u66f4\u65b0\u8bf4\u660e: &lt;\/font&gt;&lt;\/span&gt;&lt;\/p&gt;&lt;ul style=&#39;LIST-STYLE-TYPE: none&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;li fulltext=&#39;true&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;\/span&gt;&lt;div&gt;&lt;font face=&#39;sans-serif&#39;&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;R07IC03W &amp;lt;10.1.1.45&amp;gt;&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&lt;span style=&#39;color: rgb(0, 0, 0); font-family: sans-serif; font-size: 12pt; font-style: normal; font-variant: normal; font-weight: 400; letter-spacing: normal; orphans: 2; text-align: left; text-decoration: none; text-indent: 0px; text-transform: none; -webkit-text-stroke-width: 0px; white-space: normal; word-spacing: 0px;&#39;&gt;- [\u91cd\u8981] \u89e3\u51b3\u5b89\u5168\u6f0f\u6d1e\u95ee\u9898&amp;nbsp;CVE-2019-6173 \u548c CVE-2019-6196&lt;br&gt;- (\u4fee\u6b63) \u8106\u5f31\u6027\u95ee\u9898: CVE-2019-0128 &lt;\/span&gt;&lt;br style=&#39;color: rgb(0, 0, 0); font-family: sans-serif; font-size: 12px; font-style: normal; font-variant: normal; font-weight: 400; letter-spacing: normal; orphans: 2; text-align: left; text-decoration: none; text-indent: 0px; text-transform: none; -webkit-text-stroke-width: 0px; white-space: normal; word-spacing: 0px;&#39;&gt;&lt;span style=&#39;color: rgb(0, 0, 0); font-family: sans-serif; font-size: 12pt; font-style: normal; font-variant: normal; font-weight: 400; letter-spacing: normal; orphans: 2; text-align: left; text-decoration: none; text-indent: 0px; text-transform: none; -webkit-text-stroke-width: 0px; white-space: normal; word-spacing: 0px;&#39;&gt;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp; \u5b89\u88c5\u7a0b\u5e8f\u5728\u82f1\u7279\u5c14(R)\u82af\u7247\u7ec4\u8bbe\u5907\u8f6f\u4ef6(INF\u66f4\u65b0\u5b9e\u7528\u7a0b\u5e8f)\u4e4b\u524d\u7684\u4e0d\u5f53\u6743\u9650&lt;\/span&gt;&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;&amp;lt;10.1.1.9&amp;gt;(R07IC02W)&lt;\/span&gt;&lt;br&gt;&lt;span style=&#39;font-size: 12pt;&#39;&gt;- (\u65b0\u589e) \u65b0\u53d1\u5e03\u652f\u6301ThinkPad L460, T460p.&lt;\/span&gt;&lt;\/font&gt;&lt;\/div&gt;&lt;\/li&gt;&lt;\/ul&gt;",
                        "FCreator": "",
                        "FCreateDateTime": "2019\/9\/6",
                        "FTimestamp": "System.Byte[]",
                        "FSourcePath": ""
                    }
                ],
                "OS": [
                    {
                        "Name": "Windows 7 32-bit"
                    },
                    {
                        "Name": "Windows 7 64-bit"
                    },
                    {
                        "Name": "Windows 8.1 64-bit"
                    }
                ]
            },
            "StatusCode": "200",
            "Message": "\u6210\u529f"
        }
    }
    ```