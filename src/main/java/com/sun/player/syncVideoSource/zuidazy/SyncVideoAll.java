package com.sun.player.syncVideoSource.zuidazy;

import com.sun.player.syncDto.VideoCategory;
import com.sun.player.syncRepository.VideoCategoryRep;
import com.sun.player.syncRepository.VideoListRep;
import com.sun.player.syncTask.SyncTask;
import com.sun.player.syncUtil.parseXml.ParseXml;
import com.sun.player.syncUtil.socetTimeUtil.TimeTest;
import com.sun.player.syncVideoSource.SyncClub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sun
 * @version 1.0
 * @see SyncVideoAll
 * @since 2019/12/19 15:49
 **/
@Slf4j
@Component

public class SyncVideoAll implements SyncClub {
    @Resource
    private VideoCategoryRep videoCategoryRep;

    @Resource
    private VideoListRep videoListRepo;

    @Resource
    private SyncTask syncTask;

    private static List<String> baseUrls = new ArrayList<>();

    private String baseUrl;

    /**
     * 同步方法,同步所有的视频 syncAll,初始化数据
     * @since 2019/12/19 15:55
     */
    @Override
    public void syncAll() {

        //查找网站的地址
        Optional<VideoCategory> byId = videoCategoryRep.findById("0");

        byId.ifPresent(Id -> {
            String[] split = Id.getUrl().split(";");
            baseUrls.addAll(Arrays.asList(split));
        });

        if (!byId.isPresent()) {
            log.info("数据库没有网址的数据,可能是第一次初始化数据,请重新启动一次");
            return;
        }
        baseUrl = TimeTest.getBestUrl(baseUrls);
        log.info("使用地址: " + baseUrl);

        //查找网站的所有视频分类
        Optional<List<VideoCategory>> byPid = videoCategoryRep.findByPidOrderBySortIdAsc("0");
        byPid.ifPresent(category -> {

            //获取分类下所有的页

            Map<String, String> allVideoByPage1 =syncTask.getAllVideoByPage(baseUrl, baseUrls, category);

            List<String> byRep = new ArrayList<>(allVideoByPage1.values());

            //遍历返回的结果,对每一个分类进行获取他的所有页
            for (String s : byRep) {

                //获取所有的页的连接
                List<VideoCategory> videoCategory = ParseXml.parseList(s);

                //http请求结果不能重复读取,所以上面的分类其实是第一页的地址,重新发送
                videoCategory.addAll(category);

                //每一个分类开启一个线程处理
                new Thread(() -> {

                    Map<String, String> allVideoByPage2 =syncTask.getAllVideoByPage(baseUrl, baseUrls, videoCategory);

                    List<String> allVideoByPage = new ArrayList<>(allVideoByPage2.values());

                    //把发送请求回来的结果放在结果队列
                    for (int i = 0; i < allVideoByPage.size(); i++) {

                        List<VideoCategory> list1 = syncTask.parseCategory(allVideoByPage.get(i));

                        //访问所有的视频详细信息,就会进入详细页
                        Map<String, String> allVideos = syncTask.getAllVideoByPage(baseUrl, baseUrls, list1);

                        //50条处理
                        syncTask.parseVideoDetail(allVideos);

                        log.info("处理完毕的 url " + videoCategory.get(i).getUrl());
                    }
                }).start();

            }
            log.info("处理完所有信息");
        });

    }

}











