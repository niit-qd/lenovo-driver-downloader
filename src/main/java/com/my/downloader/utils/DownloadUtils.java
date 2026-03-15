package com.my.downloader.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author hw
 */
public class DownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    public interface DownloadProgressCallback {

        int RESULT_SUCCESSFUL = 0;
        // int RESULT_FAILED = 1;
        int RESULT_FAILED_DOWNLOAD_EXCEPTION = 2;
        int RESULT_FAILED_OTHER_EXCEPTION = 3;
        // int RESULT_UNKNOWN = -1;


        /**
         * @param url                下载地址
         * @param targetFolder       存放目录
         * @param defaultFileName    默认下载名称
         * @param getFileNameFromUrl 是否从url中获取下载名称
         * @param length             远程文件总长度
         */
        void onDownloadStarted(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, long length);

        /**
         * @param url                下载地址
         * @param targetFolder       存放目录
         * @param defaultFileName    默认下载名称
         * @param getFileNameFromUrl 是否从url中获取下载名称
         * @param progress           当前下载进度
         * @param length             数据总长度
         */
        void onDownloadProgressChanged(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, long progress, long length);

        /**
         * @param url                下载地址
         * @param targetFolder       存放目录
         * @param defaultFileName    默认下载名称
         * @param getFileNameFromUrl 是否从url中获取下载名称
         * @param progress           当前下载进度
         * @param length             数据总长度
         * @param result             下载结果，
         * @param exception          下载失败的时候，可能抛出异常
         */
        void onDownloadCompleted(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, long progress, long length, int result, Exception exception);
    }

    /**
     * 下载文件到目标目录
     *
     * @param url          下载地址
     * @param targetFolder 文件存放目录
     * @param callback     下载回调
     * @return 下载的文件
     * @throws IOException 可能发生的异常
     */
    public static File download(String url, File targetFolder, DownloadProgressCallback callback) throws IOException, URISyntaxException {
        return download(url, targetFolder, null, true, callback);
    }

    /**
     * @param url                下载地址
     * @param defaultFileName    默认保存的文件名称
     * @param getFileNameFromUrl 是否从请求url的响应信息中获取文件名称
     * @return 最终获取的请求文件名
     */
    public static String getFileNameFromUrl(String url, String defaultFileName, boolean getFileNameFromUrl) {
        String filename = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try {
                HttpGet httpGet = new HttpGet(url);
                logger.debug("request method: {}, uri: {}, version: {}", httpGet.getMethod(), httpGet.getRequestUri(), httpGet.getVersion());

                filename = httpclient.execute(httpGet, response -> getString(url, defaultFileName, getFileNameFromUrl, response));
            } catch (IOException e) {
                logger.error(null, e);
            }
        } catch (IOException e) {
            logger.error(null, e);
        }

        return filename;
    }

    private static String getString(String url, String defaultFileName, boolean getFileNameFromUrl, HttpResponse response) {
        String fileName = null;
        if (getFileNameFromUrl) {
            Header contentDispositionHeader = response.getFirstHeader("Content-Disposition");
            if (null != contentDispositionHeader) {
                String dispositionValue = contentDispositionHeader.getValue();
                String[] parts = dispositionValue.split(";");
                for (String part : parts) {
                    if (StringUtils.isBlank(part)) {
                        continue;
                    }
                    part = part.trim();
                    int index = part.indexOf("filename=");
                    if (index > 0) {
                        fileName = dispositionValue.substring(index + 10, dispositionValue.length() - 1);
                        logger.trace("从url请求中解析到文件名：{}", fileName);
                        break;
                    }
                }
            }
            if (null == fileName) {
                int index = url.indexOf("?");
                if (-1 == index) {
                    fileName = url.substring(url.lastIndexOf("/") + 1);
                } else {
                    fileName = url.substring(url.lastIndexOf("/") + 1, index);
                }
                logger.trace("从url中解析到文件名：{}", fileName);
            }
        }
        if (null == fileName) {
            fileName = defaultFileName;
            logger.info("使用默认文件名：{}", fileName);
        }
        return fileName;
    }

    /**
     * 下载文件到目标目录
     *
     * @param url                url
     * @param targetFolder       文件保存路径
     * @param defaultFileName    如果无法从url请求中解析文件名，则使用改文件名。
     * @param getFileNameFromUrl 是否从url请求中解析文件名
     * @param callback           下载进度回调
     * @return 下载好的文件
     */
    public static File download(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl, DownloadProgressCallback callback) {
        if (!targetFolder.exists()) {
            if (!targetFolder.mkdirs()) {
                return null;
            }
        }
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URL url1 = new URL(url);
            URI uri = new URI(url1.getProtocol(), url1.getUserInfo(), url1.getHost(), url1.getPort(), url1.getPath(), url1.getQuery(), null);
            HttpGet httpGet = new HttpGet(uri);
            logger.debug("request method: {}", httpGet.getMethod());
            logger.debug("request uri: {}", httpGet.getRequestUri());
            logger.debug("version: {}", httpGet.getVersion());
            return httpclient.execute(httpGet, response -> {
                File targetFile = null;
                OutputStream os = null;
                try {
                    logger.info("----------------------------------------");
                    logger.debug("request method: {}, uri: {}, version: {}", httpGet.getMethod(), httpGet.getRequestUri(), httpGet.getVersion());

                    // Get hold of the response entity
                    HttpEntity entity = response.getEntity();

                    // guess the file name
                    String filename;
                    filename = getString(url, defaultFileName, getFileNameFromUrl, response);


                    // If the response does not enclose an entity, there is no need
                    // to bother about connection release
                    if (entity != null) {
                        // TODO： 注意：获取的长度与实际文件大小不一致
                        long contentLength = entity.getContentLength();
                        if (null != callback) {
                            callback.onDownloadStarted(url, targetFolder, defaultFileName, getFileNameFromUrl, contentLength);
                        }
                        long count = 0;
                        InputStream inStream = null;
                        try {
                            inStream = entity.getContent();
                            // inStream.read();
                            // do something useful with the response
                            targetFile = new File(targetFolder, filename);
                            os = Files.newOutputStream(targetFile.toPath());
                            // IOUtils.copy(inStream, os);
                            int n;
                            byte[] buffer = IOUtils.byteArray(IOUtils.DEFAULT_BUFFER_SIZE);
                            while (IOUtils.EOF != (n = inStream.read(buffer))) {
                                os.write(buffer, 0, n);
                                count += n;
                                if (null != callback) {
                                    callback.onDownloadProgressChanged(url, targetFolder, defaultFileName, getFileNameFromUrl, count, contentLength);
                                }
                            }
                            if (null != callback) {
                                callback.onDownloadCompleted(url, targetFolder, defaultFileName, getFileNameFromUrl, count, contentLength, DownloadProgressCallback.RESULT_SUCCESSFUL, null);
                            }
                            logger.info("download success. url = {}, location = {}", url, targetFile);
                        } catch (IOException ex) {
                            // In case of an IOException the connection will be released
                            // back to the connection manager automatically
                            if (null != callback) {
                                callback.onDownloadCompleted(url, targetFolder, defaultFileName, getFileNameFromUrl, count, contentLength, DownloadProgressCallback.RESULT_FAILED_DOWNLOAD_EXCEPTION, ex);
                            }
                            throw ex;
                        } finally {
                            // Closing the input stream will trigger connection release
                            try {
                                if (null != inStream) {
                                    inStream.close();
                                }
                                if (null != os) {
                                    os.close();
                                }
                            } catch (Exception e) {
                                if (null != callback) {
                                    callback.onDownloadCompleted(url, targetFolder, defaultFileName, getFileNameFromUrl, count, contentLength, DownloadProgressCallback.RESULT_FAILED_OTHER_EXCEPTION, e);
                                }
                                throw e;
                            }
                        }
                    }
                } finally {
                    response.close();
                    if (null != os) {
                        os.close();
                    }
                }
                return targetFile;
            });
        } catch (Exception e) {
            logger.error("catch exception: {}", e.getMessage(), e);
            return null;
        }
    }
}
