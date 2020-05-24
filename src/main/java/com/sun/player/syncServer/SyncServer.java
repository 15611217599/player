package com.sun.player.syncServer;

import com.sun.player.syncVideoSource.SyncClub;
import com.sun.player.syncVideoSource.zuidazy.SyncVideoAll;
import com.sun.player.syncVideoSource.zuidazy.SyncVideoUpdate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Sun
 * @version 1.0
 * @see SyncServer
 * @since 2019/12/20 12:12
 **/
@Component
@Data
@Slf4j
public class SyncServer implements ServletContextListener {
    @Resource
    private SyncVideoAll syncVideoAll;

    @Resource
    private SyncClub syncMusicAll;

    @Resource
    private SyncVideoUpdate syncVideoUpdate;

    @Value("${config.isLoadAllVideo}")
    private boolean isLoadAllVideo;

    @Value("${config.isLoadUpdate}")
    private boolean isLoadUpdate;

    public void startServer() {
        /*video*/
        if (isLoadAllVideo) {

            new Thread(() -> {
                //初始化并启动全部更新服务
                syncVideoAll.syncAll();

                log.info("启动全部更新视频进程");
            }).start();
        }

        if (isLoadUpdate) {
            //更新视频
            new Thread(() -> {
                //初始化并启动今日更新进程

                syncVideoUpdate.syncUpdate();

                log.info("启动今日更新进程");

            }).start();
        }
    }

    //一小时一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void syncAutoUpdate() {

        syncVideoUpdate.syncUpdate();

        log.info("启动今日更新进程");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        startServer();
    }
}











