package org.example.lenovo.driver.downloader.runner;

import lombok.Getter;
import lombok.Setter;
import org.example.lenovo.driver.downloader.Task.DriveDownloadTask;
import org.example.lenovo.driver.downloader.config.DownloadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author czx
 */
@ConfigurationProperties(prefix = "com.example.lenovo.driver.downloader")
@Component
public class DriveDownloadTaskRunner implements ApplicationRunner, CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DriveDownloadTaskRunner.class);

    @Getter
    @Setter
    private DownloadConfiguration downloadConfiguration;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("downloadConfiguration = {}", downloadConfiguration);

        DriveDownloadTask.downloadDriveList(downloadConfiguration);
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
