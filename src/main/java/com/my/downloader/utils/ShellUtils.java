package com.my.downloader.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author hw
 */
public class ShellUtils {

    public static void main(String[] args) {
        String cmd = "cmd  /E:ON /c  ";
        cmd += "cd " + "\"" + "D:\\tmp\\test\\output2\\1、开发准备\\270P\\tmp" + "\"";
        cmd += " && D: && ffmpeg_convert_test.bat";
        executeShell(cmd);
    }

    private static final Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    /**
     * 执行shell命令
     *
     * @param shellCommand shell命令
     * @return 命令执行结果
     */
    public static String executeShell(String shellCommand) {
        logger.info("shellCommand = {}", shellCommand);

        StringBuilder sb = new StringBuilder();

        Process process = null;
        InputStream is = null;
        InputStream eis = null;
        try {
            process = Runtime.getRuntime().exec(shellCommand);
            is = process.getInputStream();
            eis = process.getErrorStream();
//            eis = process.getErrorStream();
            String info = readStream(is);
//            String error = readStream(eis);
            if (StringUtils.isNotBlank(info)) {
                sb.append(info);
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
//            if (StringUtils.isNotBlank(error)) {
//                sb.append(error);
//            }
        } catch (Exception e) {
            logger.error(null, e);
        } finally {
            closeClosable(is);
            closeClosable(eis);
            if (null != process) {
                process.destroy();
            }
        }

        return sb.toString();
    }

    /**
     * 执行shell文件
     *
     * @param shellFile shell命令脚本文件
     * @return 执行结果
     */
    public static String executeShellFile(File shellFile) throws Exception {
        if (!shellFile.isFile()) {
            throw new Exception("given shell file is not exist. shell file = " + shellFile);
        }
        boolean isOsWindows = OsUtils.isOsWindows();
        String command;
        if (isOsWindows) {
            command = "cmd  /E:ON /c  cd  \"" + shellFile.getParent() + "\" && " + shellFile.getParent().substring(0, 2) + " && \"" + shellFile.getAbsolutePath() + "\"";
        } else {
            command = "cd  \"" + shellFile.getParent() + "\" && " + shellFile.getParent().substring(0, 2) + " ; \"" + shellFile.getAbsolutePath() + "\"";
        }
        return executeShell(command);
    }

    /**
     * 读取执行结果
     *
     * @param is shell的执行结果输入流
     * @return 命令执行结果
     */
    public static String readStream(InputStream is) throws Exception {
        if (null == is) {
            throw new Exception("given input stream is null");
        }
        StringBuilder sb = new StringBuilder();

        try {
            InputStreamReader isr = new InputStreamReader(is, "GBK");
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
                logger.info(line);
            }

        } catch (IOException e) {
            logger.error(null, e);
            throw e;
        }
        return sb.toString();
    }

    private static void closeClosable(Closeable closeable) {
        if (null == closeable) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            logger.error(null, e);
        }
    }
}
