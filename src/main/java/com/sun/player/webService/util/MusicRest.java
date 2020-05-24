package com.sun.player.webService.util;

import com.sun.player.syncDto.Music;
import com.sun.player.syncRepository.MusicRep;
import com.sun.player.syncUtil.httpRequest.AsyncHttpClient;
import com.sun.player.syncUtil.parseXml.ParseMusicXml;
import com.sun.player.webService.util.serchDto.MusicCateGory;
import com.sun.player.webService.util.serchDto.MusicList;
import com.sun.player.webService.util.serchDto.MusicSearchPara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Sun
 * @version 1.0
 * @see MusicRest
 * @since 2020/1/22 7:46
 **/
@RestController
@Slf4j
public class MusicRest {

    @Resource
    ParseMusicXml parseMusicXml;

    @Resource
    MusicRep musicRep;

    @Resource
    AsyncHttpClient asyncHttpClient;

    //音乐连接头部信息和链接地址缓存
    private static Map<String, Music> searchParaMap = new HashMap<>();

    //音乐排行榜的标题
    private static Map<String, List<MusicCateGory>> musicCateGoryCache = new ConcurrentHashMap<>();

    //音乐排行榜下面的播放地址
    private static Map<String, List<String>> musicCateGoryHashCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {

        List<Music> all = musicRep.findAll();

        if (!all.isEmpty()) {

            for (Music music : all) {
                searchParaMap.put(music.getId(), music);
                Map<String, String> requestHead = music.getRequestHead();

            }
        }

    }

    @RequestMapping(value = "/getMusicInit", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")

    public List<Map<String, String>> getInitParam() {

        List<Map<String, String>> list = new ArrayList<>();

        searchParaMap.forEach((k, v) -> {

            Map<String, String> initPara = new HashMap<>();
            initPara.put("id", v.getId());
            initPara.put("showName", v.getShowName());
            initPara.put("page", "1");
            initPara.put("pageSize", "10");
            list.add(initPara);
        });

        return list;
    }

    /***
     * searchMusic 搜索音乐
     * @param musicSearchPara musicSearchPara
     * @return java.util.List<com.sun.player.syncDto.MusicWeb>
     * @since 2020/1/22 18:26
     */

    @RequestMapping(value = "/musicSearch", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<MusicList> searchMusic(@RequestBody MusicSearchPara musicSearchPara) {

        log.info("音乐搜索:" + musicSearchPara.getInput());
        List<MusicList> musicLists = new ArrayList<>();
        String id = musicSearchPara.getId();

        //处理酷狗
        if (musicSearchPara.getId().equals("1") || musicSearchPara.getId().equals("0")) {

            musicLists = parseMusicXml.parseMusicKuGou(musicSearchPara, searchParaMap.get("1"));

            parseMusicXml.parseMusicKuGou(musicLists, searchParaMap.get("1"));

            //处理网易
        } else if (musicSearchPara.getId().equals("2")) {

            musicLists = parseMusicXml.parseMusicWangYi(musicSearchPara, searchParaMap.get("2"));

            parseMusicXml.sendGeCi(musicLists, searchParaMap.get("2"));
        }

        return musicLists;
    }

    /*** 根据歌曲的hash获取具体音乐,因为酷狗的过期,还是得有一个
     * getMusicListFromHash
     * @return java.util.List<com.sun.player.webService.util.serchDto.MusicList>
     * @since 2020/2/6 15:48
     */
    @RequestMapping(value = "/getMusicByMusic", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<MusicList> getMusicListFromHash(@RequestBody List<String> musicLists) {

        List<MusicList> musicLists1 = new ArrayList<>();
        for (String musicList : musicLists) {
            MusicList musicList1 = new MusicList();
            musicList1.setSongOldUrl(musicList);
            musicLists1.add(musicList1);
        }
        parseMusicXml.parseMusicKuGou(musicLists1, searchParaMap.get("1"));

        return musicLists1;
    }

    /***
     * getMusicCateGory  获取热门音乐标题
     * @return java.util.List<com.sun.player.webService.util.serchDto.MusicCateGory>
     * @since 2020/2/6 16:19
     */
    @RequestMapping(value = "/getMusicCateGory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<MusicCateGory> getMusicCateGory() {

        List<MusicCateGory> list = musicCateGoryCache.get("musicCateGory");

        if (list == null) {
            cacheTopCategory();
            list = musicCateGoryCache.get("musicCateGory");
        }

        return list;
    }

    //获取热门歌曲根据主题id
    @RequestMapping(value = "/getMusicListByCategory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<MusicList> getMusicListByCategory(@RequestBody MusicSearchPara musicSearchPara) {

        List<MusicList> musicList = new ArrayList<>();

        List<String> list = musicCateGoryHashCache.get(musicSearchPara.getId());

        if (list == null) {

            cacheTopCategory();
            list = musicCateGoryHashCache.get(musicSearchPara.getId());
        }

        int page = Integer.parseInt(musicSearchPara.getPage());
        int pageSize = Integer.parseInt(musicSearchPara.getPageSize());

        int total = (page - 1) * pageSize;

        //手动分页
        for (int i = total; i < list.size(); i++) {
            MusicList musicList1 = new MusicList();

            musicList1.setSongOldUrl(list.get(i));

            musicList.add(musicList1);

            if (i - total == pageSize-1) {
                break;
            }
        }

        parseMusicXml.parseMusicKuGou(musicList, searchParaMap.get("1"));

        return musicList;
    }

    //缓存热门主题和详细歌曲播放地址

    public void cacheTopCategory() {

        String cateGoryUrl = "http://m.kugou.com/rank/list&json=true";
        Future<String> stringFuture = asyncHttpClient.sendMusic(cateGoryUrl, null);
        String s = null;
        try {
            s = stringFuture.get();
        } catch (Exception ignored) {
        }
        List<MusicCateGory> musicCateGoryKuGou = parseMusicXml.getMusicCateGoryKuGou(s);
        musicCateGoryCache.put("musicCateGory", musicCateGoryKuGou);

        Map<String, Future<String>> details = new HashMap<>();

        for (MusicCateGory musicCateGory : musicCateGoryKuGou) {

            String id = musicCateGory.getId();

            String getCateGoryDetailUrl = "http://mobilecdngz.kugou.com/api/v3/rank/song?rankid=%s&ranktype=2&page=1&pagesize=500&volid=&plat=2&version=8955&area_code=1";

            String format = String.format(getCateGoryDetailUrl, id);
            Future<String> detail = asyncHttpClient.sendMusic(format, null);

            details.put(id, detail);
        }

        //根据热门主题组装播放地址缓存
        for (String id : details.keySet()) {

            List<String> list = new ArrayList<>();

            Future<String> stringFuture1 = details.get(id);
            String s1 = null;
            try {
                s1 = stringFuture1.get();
            } catch (Exception ignored) {
            }
            List<String> musicListFromCateGory = parseMusicXml.getMusicListFromCateGory(s1);
            Music music = searchParaMap.get("1");
            String detailUrl = music.getDetailUrl();

            for (String s2 : musicListFromCateGory) {

                String format = detailUrl + s2;
                list.add(format);
            }

            musicCateGoryHashCache.put(id, list);
        }

        log.info("ok");
    }
}











