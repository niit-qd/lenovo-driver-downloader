package com.my.downloader.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * @author hw
 */
public class MyThreadUtils {

    private static final Logger logger = LoggerFactory.getLogger(MyThreadUtils.class);

    /**
     * 在线程池中执行多个任务。所有任务都异步执行，所有任务都完成后，程序退出。
     *
     * @param executor ThreadPoolTaskExecutor
     * @param tasks    Runnable 集合
     */
    public static void executeBatchTasks(ThreadPoolTaskExecutor executor, Collection<Runnable> tasks) {
        if (null == executor) {
            return;
        }
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable task : tasks) {
            if (null == task) {
                latch.countDown();
            }
            executor.execute(() -> {
                try {
                    assert task != null;
                    task.run();
                } catch (Exception e) {
                    logger.error(null, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(null, e);
        }
    }

}
