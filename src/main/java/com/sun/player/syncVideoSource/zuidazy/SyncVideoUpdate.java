package com.sun.player.syncVideoSource.zuidazy;

import com.sun.player.syncDto.VideoCategory;
import com.sun.player.syncDto.VideoList;
import com.sun.player.syncRepository.VideoCategoryRep;
import com.sun.player.syncRepository.VideoListRep;
import com.sun.player.syncTask.SyncTask;
import com.sun.player.syncUtil.httpRequest.AsyncHttpClient;
import com.sun.player.syncUtil.httpRequest.HttpClient;
import com.sun.player.syncUtil.parseXml.ParseXml;
import com.sun.player.syncUtil.socetTimeUtil.TimeTest;
import com.sun.player.syncVideoSource.SyncUpdateClub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

/**
 * @author Sun
 * @version 1.0
 * @see SyncVideoUpdate
 * @since 2019/12/19 15:49
 **/

@Component
@Slf4j
public class SyncVideoUpdate implements SyncUpdateClub {

    @Resource
    private VideoCategoryRep videoCategoryRep;

    @Resource
    private VideoListRep videoListRepo;

    @Resource
    private SyncTask syncTask;

    private static List<String> baseUrls = new ArrayList<>();

    private String baseUrl;

    public static BlockingQueue<VideoList> imgQue = new ArrayBlockingQueue<>(100000);

    @Resource
    private AsyncHttpClient asyncHttpClient;

    @Resource
    private ParseXml parseXml;

    /***-
     * 同步当前更新视频方法
     * syncUpdate
     * @since 2019/12/19 15:57
     */
    @Override
    public void syncUpdate() {

        if (baseUrls.isEmpty()) {
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
        }

        baseUrl = TimeTest.getBestUrl(baseUrls);
        log.info("更新视频使用地址: " + baseUrl);

        List<VideoCategory> videoCategorys = new ArrayList<>();
        VideoCategory videoCategory = new VideoCategory();
        videoCategory.setUrl("");
        videoCategorys.add(videoCategory);

        //根据首页获取更新的信息
        Map<String, String> allVideoByPage1 = syncTask.getAllVideoByPage(baseUrl, baseUrls, videoCategorys);

        List<String> byRep = new ArrayList<>(allVideoByPage1.values());

        for (String s : byRep) {
            //获取所有的页的连接
            List<VideoCategory> videoCategoryList = ParseXml.parseUpdateList(s);

            //http请求结果不能重复读取,所以上面的分类其实是第一页的地址,重新发送
            videoCategoryList.addAll(videoCategorys);

            //每一个分类开启一个线程处理
            new Thread(() -> {

                Map<String, String> allVideoByPage2 = syncTask.getAllVideoByPage(baseUrl, baseUrls, videoCategoryList);
                List<String> allVideoByPage = new ArrayList<>(allVideoByPage2.values());
                //把发送请求回来的结果放在结果队列
                for (int i = 0; i < allVideoByPage.size(); i++) {

                    List<VideoCategory> list1 = syncTask.parseCategory(allVideoByPage.get(i));

                    //访问所有的视频详细信息,就会进入详细页
                    Map<String, String> allVideos = syncTask.getAllVideoByPage(baseUrl, baseUrls, list1);

                    //50条处理
                    syncTask.parseVideoDetail(allVideos);

                    log.info("处理完毕的 url " + videoCategoryList.get(i).getUrl());
                }
            }).start();
        }

        syncTopVideoFromTencent();
    }

    @Override
    public void syncPic() {
        new Thread(() -> {
            log.info("启动同步照片进程");

            while (true) {

                VideoList take = null;
                try {

                    take = imgQue.take();
                    //在这里看一下是否已经有了,有的话不要再下载了
                    File path = null;
                    try {
                        path = new File(ResourceUtils.getURL("classpath:").getPath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    String gitPath = path.getParentFile().getParentFile().getParent() + File.separator + "static" + File.separator + "imageFile" + File.separator;
                    File file = new File(gitPath);
                    if (!file.exists()) {
                        //windows cmd获取后会多一个file:\前缀,奇怪

                        int i = gitPath.indexOf(":");
                        gitPath = gitPath.substring(i + 2);
                    }

                    String fileName = take.getPictureUrl().substring(take.getPictureUrl().lastIndexOf("."));

                    String fullPath = gitPath + take.getId() + fileName;

                    if ((new File(fullPath).exists())) {
                        continue;
                    }

                    Future<byte[]> future = asyncHttpClient.sendByte(take.getPictureUrl());
                    byte[] bytes = future.get();

                    if (bytes.length == 0) {

                        imgQue.add(take);
                    } else {

                        buff2Image(bytes, fullPath);

                    }
                } catch (Exception e) {
                    log.error("照片同步线程出错: " + e, e);
                }

            }
        }).start();
    }

    //图片存储
    private void buff2Image(byte[] b, String tagSrc) throws Exception {
        File file = new File(tagSrc);
        FileOutputStream foul = new FileOutputStream(file);
        //将字节写入文件
        foul.write(b);
        foul.close();
    }

    /*** 更新腾讯视频推荐视频
     * syncTopVideoFromTencent
     * @since 2020/1/30 19:40
     */

    public void syncTopVideoFromTencent() {

        String tenCentUrl = "https://v.qq.com/";
        String gbk = HttpClient.send(tenCentUrl, "utf-8");

        List<VideoList> videoListFromTen = parseXml.getVideoListFromTen(gbk);

        //跟新前要删除以前的吧,不然不得冲突
        try {

            videoListRepo.deleteByPid("-1");
            log.info("删除腾讯视频,然后在更新");
            videoListRepo.saveAll(videoListFromTen);

        } catch (Exception e) {

        }
        log.info("更新腾讯视频:" + videoListFromTen.size() + "条");
    }

}











