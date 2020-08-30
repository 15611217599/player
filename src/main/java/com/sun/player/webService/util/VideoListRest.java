package com.sun.player.webService.util;

import com.sun.player.syncDto.VideoCategory;
import com.sun.player.syncDto.VideoList;
import com.sun.player.syncRepository.VideoCategoryRep;
import com.sun.player.syncRepository.VideoListRep;
import com.sun.player.webService.util.serchDto.SearchFilter;
import com.sun.player.webService.util.serchDto.SerchPara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sun
 * @version 1.0
 * @see VideoListRest
 * @since 2020/1/11 1:23
 **/
@RestController
@Slf4j
public class VideoListRest {

    @Resource
    VideoListRep videoListRep;

    @Resource
    VideoCategoryRep videoCategoryRep;

    //searchFilter 筛选按钮缓存
    static ConcurrentHashMap<String, List<List<SearchFilter>>> caches = new ConcurrentHashMap();

    //缓存所有标题的第一页
    static ConcurrentHashMap<String, Page<VideoList>> firstMap = new ConcurrentHashMap();

    //缓存所有的标题
    static ConcurrentHashMap<String, List<VideoCategory>> categoryCache = new ConcurrentHashMap();

    //缓存所有的标题的hot

    static ConcurrentHashMap<String, Map<String, List<VideoList>>> hotCache = new ConcurrentHashMap();

    /**
     * getVideoList 根据条件查询出具体视频
     * @param serchPara serchPara
     * @since 2020/1/11 1:34
     */
    @RequestMapping(value = "/getVideoList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Page<VideoList> getVideoList(@RequestBody SerchPara serchPara) {

        log.info("getVideoList 入参是 " + serchPara);

        if (serchPara.getPids().trim().length() == 0 && serchPara.getCurrentPageNum() == 0 && serchPara.getSortWay().equals("0") && serchPara.getYears().length() == 0 && serchPara.getArea().length() == 0) {
            Page<VideoList> videoLists = firstMap.get(serchPara.getPpid());

            if (videoLists == null) {
                initFirstPage();

                videoLists = firstMap.get(serchPara.getPpid());
            }
            return videoLists;

        }

        List<String> pids = new ArrayList<>();
        String sortWay = null;
        StringBuffer years = new StringBuffer();

        StringBuffer area = new StringBuffer();
        //如果子id没有值,则取父id所有子分类

        if (serchPara.getPids().isEmpty()) {
            String ppid = serchPara.getPpid();
            Optional<List<VideoCategory>> byPidOrderBySortIdAsc = videoCategoryRep.findByPidOrderBySortIdAsc(ppid);
            boolean present = byPidOrderBySortIdAsc.isPresent();

            if (present) {
                byPidOrderBySortIdAsc.ifPresent(videoCategories -> {
                    for (int i = 0; i < videoCategories.size(); i++) {
                        pids.add(videoCategories.get(i).getId());
                    }
                });
            } else {
                //如果父id没有值,说明只有一个分类
                pids.add(ppid);
            }

        } else {
            if (!serchPara.getPids().isEmpty())
                pids.add(serchPara.getPids());
        }

        //排序方式

        String sortWay1 = serchPara.getSortWay();

        if (StringUtils.isEmpty(sortWay1)) {
            //默认按更新日期排序
            sortWay = "releaseTime";
        } else {
            if (sortWay1.equals("0")) {
                sortWay = "releaseTime";

            } else if (sortWay1.equals("1")) {

                //热度暂时没有
                sortWay = "updatedTime";
            } else if (sortWay1.equals("2")) {

                sortWay = "showScore";
            }
        }

        //年代

        if (serchPara.getYears() != null)
            years.append(serchPara.getYears());

        //地区
        if (serchPara.getArea() != null)
            area.append(serchPara.getArea());
        Pageable pageable = PageRequest.of(serchPara.getCurrentPageNum(), serchPara.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, sortWay)));

        Page<VideoList> page = videoListRep.findAll((root, query, cb) -> {

            List<Predicate> list = new ArrayList<>();

            if (!pids.isEmpty())
                list.add(cb.and(cb.and(root.get("pid").as(String.class).in(pids))));
            if (!years.toString().isEmpty())
                list.add(cb.and(cb.and(root.get("releaseTime").as(String.class).in(years.toString()))));

            if (!area.toString().isEmpty())
                list.add(cb.and(cb.and(root.get("area").as(String.class).in(area.toString()))));

            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        }, pageable);
        // log.info("getVideoList 返回的结果是 " + page.getContent());
        return page;
    }

    /***
     * getSearchFilter 获取每个分类下不同的筛选条件
     * @return java.util.List<com.sun.player.webService.util.serchDto.SearchFilter>
     * @since 2020/1/14 13:46
     */
    @RequestMapping(value = "/getSearchFilter", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<List<SearchFilter>> getSearchFilter() {

         List<List<SearchFilter>> searchFilter = caches.get("searchFilter");

        if (searchFilter == null) {
            syncCache();
            searchFilter = caches.get("searchFilter");
        }

        return searchFilter;
    }

    //输入搜索方法
    @RequestMapping(value = "/getSearch", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<VideoList> getVideoList(@RequestBody String serchPara) {
        List<VideoList> lists = new ArrayList<>();
        //不要热门的查出来
        Optional<List<VideoList>> byNameLike = videoListRep.findByNameLikeAndPidNot(serchPara + "%", "-1");

        byNameLike.ifPresent(videoLists -> {
            lists.addAll(videoLists);
        });

        return lists;
    }

    //获取所有标题分类的方法
    @RequestMapping(value = "/getCategory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<VideoCategory> getVideoList() {

        List<VideoCategory> list = categoryCache.get("category");
        if (list == null) {

            initCategory();

            list = categoryCache.get("category");
        }
        return list;
    }

    //获取所有标题分类的  最新视频更新日期
    @RequestMapping(value = "/getHot", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map<String, List<VideoList>> getHot() {
        Map<String, List<VideoList>> hot = hotCache.get("hot");

        if (hot == null) {
            initHotVideo();
            hot = hotCache.get("hot");
        }

        return hot;
    }

    //每天凌晨3点执行一次 缓存所有最新视频
    @Scheduled(cron = "0 0/10 * * * ?")

    @PostConstruct
    public void initHotVideo() {

        Map<String, List<VideoList>> hot = new HashMap<>();

        hot.put("最热", new ArrayList<VideoList>());
        hot.put("热播电影", new ArrayList<VideoList>());
        hot.put("热播电视剧", new ArrayList<VideoList>());
        hot.put("热播综艺节目", new ArrayList<VideoList>());
        hot.put("热播动漫", new ArrayList<VideoList>());

        //推荐页里面的轮播图是id尾号为_4的  0 电影 1 电视剧 2综艺 3 动漫 4轮播图

        Optional<List<VideoList>> byPid = videoListRep.findByPid("-1");

        if (byPid.isPresent()) {
            List<VideoList> lists1 = byPid.get();

            //等于4的是轮播图
            for (VideoList videoList : lists1) {

                if (videoList.getId().split("_")[1].equals("0")) {
                    hot.get("热播电影").add(videoList);
                } else if (videoList.getId().split("_")[1].equals("1")) {
                    hot.get("热播电视剧").add(videoList);
                } else if (videoList.getId().split("_")[1].equals("2")) {
                    hot.get("热播综艺节目").add(videoList);
                } else if (videoList.getId().split("_")[1].equals("3")) {
                    hot.get("热播动漫").add(videoList);
                } else if (videoList.getId().split("_")[1].equals("4")) {
                    hot.get("最热").add(videoList);
                }
            }

        }

        hotCache.put("hot", hot);
    }

    //每天凌晨2点执行一次 缓存所有的标题的筛选条件
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncCache() {
        List<List<SearchFilter>> list = new ArrayList<>();

        Optional<List<VideoCategory>> byPidOrderBySortIdAsc = videoCategoryRep.findByPidOrderBySortIdAsc("0");
        byPidOrderBySortIdAsc.ifPresent(videoCategories -> {

            for (VideoCategory videoCategory : videoCategories) {

                List<SearchFilter> li = new ArrayList<>();

                //第一个筛选条件  类别
                SearchFilter searchFilter = new SearchFilter();
                searchFilter.setName("类别");
                searchFilter.setLink_name("pids");

                Optional<List<VideoCategory>> vbyPidOrderBySortIdAsc = videoCategoryRep.findByPidOrderBySortIdAsc(videoCategory.getId());
                List<String> names = new ArrayList<>();
                List<String> items = new ArrayList<>();
                names.add("全部");
                items.add("");

                List<String> pids = new ArrayList<>();
                pids.add(videoCategory.getId());
                vbyPidOrderBySortIdAsc.ifPresent(videoCategories1 -> {
                    for (VideoCategory videoCategory1 : videoCategories1) {
                        items.add(videoCategory1.getId());
                        names.add(videoCategory1.getName());
                        pids.add(videoCategory1.getId());
                    }
                });
                searchFilter.setItem(items);
                searchFilter.setItemName(names);

                //第二个筛选条件 年代
                SearchFilter searchFilter1 = new SearchFilter();
                searchFilter1.setName("年代");
                searchFilter1.setLink_name("years");
                List<String> names1 = new ArrayList<>();
                List<String> items1 = new ArrayList<>();

                names1.add("全部");
                items1.add("");

                List<String> yearsPara = videoListRep.findYearsPara(pids);

                for (int i = 0; i < yearsPara.size(); i++) {
                    String s = yearsPara.get(i);
                    if (s.trim().isEmpty()) {
                        continue;
                    }
                    Calendar date = Calendar.getInstance();
                    String year = String.valueOf(date.get(Calendar.YEAR));

                    if (Integer.parseInt(s) > Integer.parseInt(year)) {
                        continue;
                    }

                    names1.add(s);
                    items1.add(s);

                    if (names1.size() == 8) {
                        break;
                    }
                }

                searchFilter1.setItemName(names1);
                searchFilter1.setItem(items1);

                //第三个  排序
                SearchFilter searchFilter2 = new SearchFilter();
                searchFilter2.setName("排序");
                searchFilter2.setLink_name("sortWay");
                List<String> names2 = new ArrayList<>();
                List<String> items2 = new ArrayList<>();

                names2.add("上映");
                names2.add("更新");
                names2.add("评分");

                items2.add("0");
                items2.add("1");
                items2.add("2");

                searchFilter2.setItemName(names2);
                searchFilter2.setItem(items2);

                //第四个筛选条件 区域
                SearchFilter searchFilter3 = new SearchFilter();
                searchFilter3.setName("地区");
                searchFilter3.setLink_name("area");
                List<String> names3 = new ArrayList<>();
                List<String> items3 = new ArrayList<>();

                names3.add("全部");
                items3.add("");

                List<String> areaPara = videoListRep.findAreaPara(pids);

                for (int i = 0; i < areaPara.size(); i++) {
                    String s = areaPara.get(i);
                    if (s.trim().isEmpty()) {
                        continue;
                    }

                    names3.add(s);
                    items3.add(s);

                    if (names3.size() == 8) {
                        break;
                    }
                }
                searchFilter3.setItem(items3);
                searchFilter3.setItemName(names3);

                li.add(searchFilter);
                li.add(searchFilter1);
                li.add(searchFilter3);
                li.add(searchFilter2);
                list.add(li);
            }

        });

        caches.put("searchFilter", list);
    }

    //每天凌晨3点执行一次 缓存所有的标题第一页
    @Scheduled(cron = "0 0/10 * * * ?")
    public void initFirstPage() {
        String sortWay = null;

        //如果子id没有值,则取父id所有子分类

        Optional<List<VideoCategory>> byPidOrderBySortIdAsc1 = videoCategoryRep.findByPidOrderBySortIdAsc("0");

        List<VideoCategory> list1 = byPidOrderBySortIdAsc1.get();
        for (VideoCategory videoCategory : list1) {

            List<String> pids = new ArrayList<>();
            String ppid = videoCategory.getId();

            Optional<List<VideoCategory>> byPidOrderBySortIdAsc = videoCategoryRep.findByPidOrderBySortIdAsc(ppid);
            boolean present = byPidOrderBySortIdAsc.isPresent();

            if (present) {
                byPidOrderBySortIdAsc.ifPresent(videoCategories -> {
                    for (int i = 0; i < videoCategories.size(); i++) {
                        pids.add(videoCategories.get(i).getId());
                    }
                });
            } else {
                //如果父id没有值,说明只有一个分类
                pids.add(ppid);
            }

            //排序方式
            sortWay = "releaseTime";

            Pageable pageable = PageRequest.of(0, 8, Sort.by(new Sort.Order(Sort.Direction.DESC, sortWay)));
            Page<VideoList> page = videoListRep.findAll((root, query, cb) -> {

                List<Predicate> list = new ArrayList<>();
                Predicate predicate = root.isNotNull();

                if (!pids.isEmpty())
                    list.add(cb.and(cb.and(root.get("pid").as(String.class).in(pids))));
                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            }, pageable);

            firstMap.put(videoCategory.getId(), page);
            // log.info("getVideoList 返回的结果是 " + page.getContent());
        }

    }

    //每天凌晨3点执行一次 缓存所有的标题
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void initCategory() {

        Optional<List<VideoCategory>> byPidOrderBySortIdAsc = videoCategoryRep.findByPidOrderBySortIdAsc("0");

        List<VideoCategory> list = byPidOrderBySortIdAsc.get();

        categoryCache.put("category", list);
    }
}











