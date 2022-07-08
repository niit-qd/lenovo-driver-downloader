package com.my.downloader.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author
 */
public class DownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    /**
     * 下载文件到目标目录
     *
     * @param url
     * @param targetFolder
     * @return
     * @throws IOException
     */
    public static File download(String url, File targetFolder) throws IOException, URISyntaxException {
        return download(url, targetFolder, null, true);
    }

    /**
     * @param url
     * @param defaultFileName
     * @param getFileNameFromUrl
     * @return
     */
    public static String getFileNameFromUrl(String url, String defaultFileName, boolean getFileNameFromUrl) {
        String filename = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(url);
            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // Get hold of the response entity
                HttpEntity entity = response.getEntity();

                // guess the file name
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
                                filename = dispositionValue.substring(index + 10, dispositionValue.length() - 1);
                                logger.trace("从url请求中解析到文件名：{}", filename);
                                break;
                            }
                        }
                    }
                    if (null == filename) {
                        int index = url.indexOf("?");
                        if (-1 == index) {
                            filename = url.substring(url.lastIndexOf("/") + 1);
                        } else {
                            filename = url.substring(url.lastIndexOf("/") + 1, index);
                        }
                        logger.trace("从url中解析到文件名：{}", filename);
                    }
                }
                if (null == filename) {
                    filename = defaultFileName;
                    logger.info("使用默认文件名：{}", filename);
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            logger.error(null, e);
        } catch (IOException e) {
            logger.error(null, e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error(null, e);
            }
        }

        return filename;
    }

    /**
     * 下载文件到目标目录
     *
     * @param url                url
     * @param targetFolder       文件保存路径
     * @param defaultFileName    如果无法从url请求中解析文件名，则使用改文件名。
     * @param getFileNameFromUrl 是否从url请求中解析文件名
     * @return
     * @throws IOException
     */
    public static File download(String url, File targetFolder, String defaultFileName, boolean getFileNameFromUrl) throws IOException, URISyntaxException {
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        File targetFile = null;
        OutputStream os = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            URL url1 = new URL(url);
            URI uri = new URI(url1.getProtocol(), url1.getUserInfo(), url1.getHost(), url1.getPort(), url1.getPath(), url1.getQuery(), null);
            HttpGet httpget = new HttpGet(uri);
            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                logger.info("----------------------------------------");
                logger.info("{}", response.getStatusLine());

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();

                // guess the file name
                String filename = null;
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
                                filename = dispositionValue.substring(index + 10, dispositionValue.length() - 1);
                                logger.trace("从url请求中解析到文件名：{}", filename);
                                break;
                            }
                        }
                    }
                    if (null == filename) {
                        int index = url.indexOf("?");
                        if (-1 == index) {
                            filename = url.substring(url.lastIndexOf("/") + 1);
                        } else {
                            filename = url.substring(url.lastIndexOf("/") + 1, index);
                        }
                        logger.trace("从url中解析到文件名：{}", filename);
                    }
                }
                if (null == filename) {
                    filename = defaultFileName;
                    logger.info("使用默认文件名：{}", filename);
                }


                // If the response does not enclose an entity, there is no need
                // to bother about connection release
                if (entity != null) {
                    InputStream inStream = entity.getContent();
                    try {
                        // inStream.read();
                        // do something useful with the response
                        targetFile = new File(targetFolder, filename);
                        os = new FileOutputStream(targetFile);
                        IOUtils.copy(inStream, os);
                        logger.info("download success. url = {}, location = {}", url, targetFile);
                    } catch (IOException ex) {
                        // In case of an IOException the connection will be released
                        // back to the connection manager automatically
                        throw ex;
                    } finally {
                        // Closing the input stream will trigger connection release
                        inStream.close();
                    }
                }
            } finally {
                response.close();
                if (null != os) {
                    os.close();
                }
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                throw e;
            }
        }
        return targetFile;
    }
}
