package com.sun.player.syncTask;

import com.sun.player.syncDto.VideoCategory;
import com.sun.player.syncDto.VideoList;
import com.sun.player.syncRepository.VideoListRep;
import com.sun.player.syncUtil.httpRequest.AsyncHttpClient;
import com.sun.player.syncUtil.parseXml.ParseXml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Sun
 * @version 1.0
 * @see SyncTask
 * @since 2019/12/20 21:30
 **/
@Service
@Slf4j
public class SyncTask {
    @Resource
    private VideoListRep videoListRepo;
    @Resource
    private AsyncHttpClient asyncHttpClient;

    /***根据所有分页的类别获取当前页面存在的视频
     * parseCategory
     * @since 2019/12/20 10:05
     */

    public List<VideoCategory> parseCategory(String s) {
        List<VideoCategory> videoLists = new ArrayList<>();
        try {

            Long start = System.currentTimeMillis();

            //把所有的页面中,视频的详细信息地址解析出来
            videoLists = ParseXml.parseVideoList(s);

            log.info("获取到从分类中返回的详情页列表,处理耗时 : " + (System.currentTimeMillis() - start));

        } catch (Exception e) {
            log.error("", e);
        }
        return videoLists;
    }

    public void parseVideoDetail( Map<String, String> allVideos) {
        try {
            Long start = System.currentTimeMillis();

            //这是所有的视频连接,包括基本信息和下载地址
            List<VideoList> videoLists = ParseXml.parseVideoListDetail(allVideos);

            //批量下载照片,这样处理可以多线程下载  ----2020114 修改 图片不要插入数据库,直接存储本地服务图片
            //SyncVideoUpdate.imgQue.addAll(videoLists);

            //存储
            saveVideos(videoLists);

            log.info("获取到从详情页获取的视频信息,处理耗时 : " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("", e);
        }

    }

    public void saveVideos(List<VideoList> videoLists) {
        try {

            Long start = System.currentTimeMillis();
            videoListRepo.saveAll(videoLists);

            log.info("获取视频保存信息,共 : " + videoLists.size() + "条 处理耗时 : " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            for (VideoList videoList : videoLists) {
                log.error("", e);
            }

        }

    }

    /***根据分页获取所有视频
     * getAllVideoByPage
     * @param videoCategorys videoCategorys
     * @return java.util.List<java.util.concurrent.Future < org.apache.http.HttpResponse>>
     * @since 2019/12/20 10:52
     */

    public Map<String,String> getAllVideoByPage(String baseUrl, List<String> baseUrls, List<VideoCategory> videoCategorys) {

        Map<String,String> list = new HashMap<>();
        Map<String,Future<String>> list_tmp = new HashMap<>();
        for (VideoCategory videoCategory : videoCategorys) {
            String url = videoCategory.getUrl();

            //新增热门推荐
            if (url.equals("-1")) {
                continue;
            }

            //根据分页获取所有的视频
            Future<String> send = asyncHttpClient.send(baseUrl, baseUrls, url);
            list_tmp.put(url,send);
        }

        for (String stringFuture : list_tmp.keySet()) {
            try {
                list.put(stringFuture,list_tmp.get(stringFuture).get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Bean(name = "sendPool")

    public Executor asyncServiceExecutor() {

        //ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //配置核心线程数

        executor.setCorePoolSize(100);

        //配置最大线程数

        executor.setMaxPoolSize(500);

        //配置队列大小

        executor.setQueueCapacity(99999);

        //配置线程池中的线程的名称前缀

        executor.setThreadNamePrefix("async-send-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务

        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //执行初始化

        executor.initialize();

        return executor;

    }

    @Bean(name = "updatePool")

    public Executor asyncUpdateExecutor() {

        //ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //配置核心线程数

        executor.setCorePoolSize(100);

        //配置最大线程数

        executor.setMaxPoolSize(500);

        //配置队列大小

        executor.setQueueCapacity(99999);

        //配置线程池中的线程的名称前缀

        executor.setThreadNamePrefix("async-update-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务

        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //执行初始化

        executor.initialize();

        return executor;

    }

}











