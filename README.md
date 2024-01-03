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

