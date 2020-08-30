package com.sun.player.syncUtil.parseXml;

import com.sun.player.syncDto.VideoCategory;
import com.sun.player.syncDto.VideoList;
import com.sun.player.syncRepository.VideoListRep;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ParseXml {

    @Resource
    VideoListRep videoListRep;

    /***根据分类返回当前分类下的所有页的地址
     * paraseList
     * @param html html
     * @return java.util.List<com.sun.player.syncDto.VideoCategory>
     * @since 2019/12/20 11:03
     */
    public static List<VideoCategory> parseList(String html) {

        List<VideoCategory> list = new ArrayList<>();
        try {
            Document document = Jsoup.parse(html);
            Elements pages = document.getElementsByClass("pages");

            //找到当前地址
            String url = "";
            Element element = pages.get(0);
            /*共9857条数据 当前:1/198页 首页 上一页 1 2 3 4 5 6 下一页 尾页*/
            String text = element.text();

            String[] split = text.split("当前:");
            String[] ye = split[1].split("页");
            String s = ye[0];
            String[] split1 = s.split("/");

            Elements a = element.getElementsByTag("a");
            url = a.get(0).attr("href");
           /* Element element1 = a.get(2);
            url = element1.getElementsByTag("a").get(0).attr("href");
            Element element2 = li1.get(li1.size() - 2);
            String attr = element2.getElementsByTag("a").get(0).attr("href");
            String[] split = attr.split("page=");*/
            //一页有50条
            int pageSize = (Integer.parseInt(split1[1]));
            for (int i = 1; i <= pageSize + 1; i++) {
                VideoCategory videoCategory = new VideoCategory();
                url = url.replaceAll("pg-\\d+", "pg-" + i);
                videoCategory.setUrl(url);
                list.add(videoCategory);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return list;
    }

    /***根据分类返回当前分类下的所有页的地址
     * parseUpdateList
     * @param html html
     * @return java.util.List<com.sun.player.syncDto.VideoCategory>
     * @since 2019/12/20 11:03
     */
    public static List<VideoCategory> parseUpdateList(String html) {

        List<VideoCategory> list = new ArrayList<>();
        try {
            Document document = Jsoup.parse(html);
            Elements pages = document.getElementsByClass("index_list_foot");
            Elements header_list = document.getElementsByClass("header_list");
            Elements li = null;
            try {
                li = header_list.get(0).getElementsByTag("li");
            } catch (Exception e) {
                log.info("这是个错误的页面" + html);
                return list;
            }
            //今日更新
            String strong = li.get(0).getElementsByTag("span").text();

            //找到当前地址
            String url = "";
            Element element = pages.get(0);

            Elements li1 = element.getElementsByTag("li");
            Element element1 = li1.get(2);
            url = element1.getElementsByTag("a").get(0).attr("href");
            //一页有50条
            int pageSize = (Integer.parseInt(strong) / 50);
            for (int i = 1; i <= pageSize + 1; i++) {
                VideoCategory videoCategory = new VideoCategory();
                url = url.replaceAll("page=\\d+", "page=" + i);
                videoCategory.setUrl(url);
                list.add(videoCategory);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return list;
    }

    /***根据当前分类返回的页解析出当前页有哪些视频地址
     * paraseVideoList
     * @param html html
     * @return java.util.List<com.sun.player.syncDto.VideoCategory>
     * @since 2019/12/20 11:04
     */
    public static List<VideoCategory> parseVideoList(String html) {

        List<VideoCategory> list = new ArrayList<>();

        try {
            Document document = Jsoup.parse(html);
            Elements videoContent = document.getElementsByClass("xing_vb4");

            for (int i = 0; i < videoContent.size(); i++) {

                VideoCategory videoCategory = new VideoCategory();

                String href = videoContent.get(i).getElementsByTag("a").get(0).attr("href");
                if (!StringUtils.isEmpty(href.trim())) {
                    videoCategory.setUrl(href);
                    list.add(videoCategory);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<VideoList> parseVideoListDetail(Map<String, String> htmls) {

        List<VideoList> lists = new ArrayList<>();
        for (String key : htmls.keySet()) {

            String html = htmls.get(key);
            VideoList video = new VideoList();

            String id;
            //二级菜单id
            String pid;
            //观看地址
            List<String> lookUrl = new ArrayList<>();
            //下载地址
            List<String> downUrl = new ArrayList<>();
            //影片名
            String name = "";
            //影片图片
            String picture = "";
            //别名
            String otherName = "";
            //导演
            String director = "";
            //主演
            String actors = "";
            //地区
            String area = "";
            //语言
            String language = "";
            //上映时间
            Integer releaseTime = 2000;
            //片长
            String filmLength = "";
            //更新日期
            Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
            //总播放量
            String totalPlayTimes = "1";
            //今日播放量
            String todayPlayTimes = "1";
            //总评分
            String totalScore = "1";
            //评分次数
            String scoresNum = "1";
            //影片简介
            String filmSynopsis;
            //显示评分
            String showScore = "";
            //更新至--或者是影片清晰度
            String newInfo;

            try {
                Document document = Jsoup.parse(html);

                //获取id and pid
                Elements nvc = document.getElementsByClass("nvc");
                Elements a = nvc.get(0).getElementsByTag("a");
                String href = a.get(1).attr("href");
                String[] split = href.split(".html");
                int i = split[0].lastIndexOf("-");
                pid = split[0].substring(i + 1);

                String[] split1 = key.split(".html");
                int i1 = split1[0].lastIndexOf("-");
                id = split1[0].substring(i1 + 1);


                Elements vodh = document.getElementsByClass("vodh");
                Element element = vodh.get(0);


                //picture
                name = vodh.get(0).getElementsByTag("h2").get(0).getAllElements().get(0).text();
                newInfo = vodh.get(0).getElementsByTag("span").get(0).getAllElements().get(0).text();

                picture = document.getElementsByClass("vodImg").get(0).getElementsByTag("img").get(0).getAllElements().get(0).attr("src");

                Elements right = document.getElementsByClass("vodinfobox");
                Elements span = right.get(0).getElementsByTag("li");
                for (Element element1 : span) {
                    if (element1.text().contains("导演：")) {
                        String split3 = element1.getElementsByTag("span").get(0).text();
                        director = split3;
                    } else if (element1.text().contains("演员：")) {

                        String split3 = element1.getElementsByTag("span").get(0).text();
                        actors = split3;
                    } else if (element1.text().contains("别名：")) {
                        String split3 = element1.getElementsByTag("span").get(0).text();
                        otherName = split3;

                    } else if (element1.text().contains("地区：")) {

                        String split3 = element1.getElementsByTag("span").get(0).text();
                        area = split3;
                    } else if (element1.text().contains("语言：")) {

                        String split3 = element1.getElementsByTag("span").get(0).text();
                        language = split3;
                    } else if (element1.text().contains("上映：")) {
                        String split3 = element1.getElementsByTag("span").get(0).text();
                        try {
                            releaseTime = Integer.valueOf(split3);

                        } catch (Exception e) {
                            releaseTime = 2000;
                        }
                    } else if (element1.text().contains("片长：")) {
                        String split3 = element1.getElementsByTag("span").get(0).text();
                        filmLength = split3;
                    } else if (element1.text().contains("更新时间：")) {
                        String split3 = element1.getElementsByTag("span").get(0).text();


                        if (split3 != null && split3.trim().length() > 0) {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //使用了默认的格式创建了一个日期格式化对象。
                                updatedTime = new Timestamp(dateFormat.parse(split3).getTime());

                            } catch (NumberFormatException e) {
                                updatedTime = new Timestamp(System.currentTimeMillis());
                                log.info("日期格式化错误" + split3);
                            }
                        } else {

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //使用了默认的格式创建了一个日期格式化对象。
                            String string = "2000年01月01日 00:00:00";
                            updatedTime = new Timestamp(dateFormat.parse(string).getTime());

                        }
                    }
                }

                filmSynopsis = document.getElementsByClass("vodplayinfo").get(0).text();

                //播放下载地址
                try {
                    //有两个id 1 和2 但是不一定哪个才是m3u8,所以判断一下

                    Elements playlist_wbox = document.getElementsByClass("ibox playBox");
                    Element element3 = playlist_wbox.get(1);
                    Elements suf = element3.getElementsByClass("suf");

                    for (Element element1 : suf) {

                        if ("zuidam3u8".equals(element1.text())) {
                            Element element2 = element1.parent().nextElementSibling();
                            for (Element li : element2.getElementsByTag("li")) {
                                Element m3u8 = li.getElementsByTag("input").get(0);
                                lookUrl.add(m3u8.attr("value"));
                            }
                        }
                    }

                } catch (Exception e) {

                }

                //播放下载地址
                try {
                    //有两个id 1 和2 但是不一定哪个才是m3u8,所以判断一下

                    Elements playlist_wbox = document.getElementsByClass("ibox playBox");
                    Element element3 = playlist_wbox.get(2);
                    Elements suf = element3.getElementsByClass("suf");

                    for (Element element1 : suf) {

                        if ("迅雷下载".equals(element1.text())) {
                            Element element2 = element1.parent().nextElementSibling();
                            for (Element li : element2.getElementsByTag("li")) {
                                Element m3u8 = li.getElementsByTag("input").get(0);
                                downUrl.add(m3u8.attr("value"));
                            }
                        }
                    }

                } catch (Exception e) {

                }


                video.setActors(actors);
                video.setArea(area);
                video.setDirector(director);
                video.setDownUrl(downUrl);
                video.setFilmLength(filmLength);
                video.setFilmSynopsis(filmSynopsis);
                video.setId(id);
                video.setLanguage(language);
                video.setLookUrl(lookUrl);
                video.setName(name);
                video.setNewInfo(newInfo);
                video.setOtherName(otherName);
                video.setPictureUrl(picture);
                //修改picture为服务器图片
                String fileName = picture.substring(picture.lastIndexOf("."));
                video.setPicture(id + fileName);

                video.setPid(pid);

                video.setReleaseTime(releaseTime);
                video.setScoresNum(scoresNum);
                video.setShowScore(showScore);
                video.setTodayPlayTimes(todayPlayTimes);
                video.setUpdatedTime(updatedTime);
                video.setTotalPlayTimes(totalPlayTimes);
                video.setTotalScore(totalScore);

                if (!id.isEmpty()) {
                    lists.add(video);
                }

            } catch (Exception e) {

            }
        }
        return lists;
    }

    public List<VideoList> getVideoListFromTen(String html) {

        List<VideoList> lists = new ArrayList<>();

        Document document = Jsoup.parse(html);


        //hot
        Elements slider_nav__quicklink_slider_nav_watched = document.getElementsByClass("slider_nav _quicklink slider_nav_watched");

        Element element = slider_nav__quicklink_slider_nav_watched.get(0);

        Elements a = element.getElementsByTag("a");

        List<String> names = new ArrayList<>();
        for (Element element1 : a) {
            String title_text = null;

            try {
                title_text = element1.getElementsByClass("title_text").get(0).text();
                if (title_text.contains("·")) {
                    int i = title_text.indexOf("·");
                    title_text = title_text.substring(0, i);
                }
            } catch (Exception e) {

                continue;
            }
            names.add(title_text);
        }

        for (String name : names) {
            getVideoListByName(name, lists, 4);
        }


        List<VideoList> lists1 = new ArrayList<>();


        //一共有5个,0 电影 1 电视剧 2综艺 3 动漫 4轮播图
        Elements mod_column_bd = document.getElementsByClass("mod_column_bd");
        //和下面hot有重复,那就不要这个了,要hot
        for (int i = 0; i < mod_column_bd.size(); i++) {
            Element element1 = mod_column_bd.get(i);

            Elements list_item = element1.getElementsByClass("list_item");

            for (Element element2 : list_item) {
                Elements figure_detail_figure_detail_two_row = element2.getElementsByClass("figure_title figure_title_two_row bold");

                String text = figure_detail_figure_detail_two_row.get(0).text();

                int i1 = text.indexOf("[");
                if (i1 != -1)
                    text = text.substring(0, i1);
                int i2 = text.indexOf("·");
                if (i2 != -1)
                    text = text.substring(0, i2);

                //如果和hot重复,就不要这个了

                boolean isContains = false;
                for (String name : names) {
                    if (name.contains(text) || text.contains(name)) {
                        isContains = true;
                    }
                }
                if (isContains)
                    continue;

                getVideoListByName(text, lists1, i);
                lists.addAll(lists1);
                lists1.clear();
            }

        }


        return lists;

    }

    public void getVideoListByName(String name, List<VideoList> lists, int i) {

        Optional<List<VideoList>> byNameLike = videoListRep.findByNameLikeAndPidNot("%" + name + "%", "-1");
        if (byNameLike.isPresent()) {

            VideoList videoList = byNameLike.get().get(0);

            VideoList videoList1 = new VideoList();

            BeanUtils.copyProperties(videoList, videoList1);

            //用它区分电影电视剧  //-0 电影 -1 电视剧 -2综艺 -3 动漫 -4轮播图
            videoList1.setId(videoList1.getId() + "_" + i);


            videoList1.setPid("-1");

            lists.add(videoList1);

        }
    }
}
