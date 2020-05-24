package com.sun.player.syncUtil.parseXml;

import com.sun.player.syncDto.Music;
import com.sun.player.syncUtil.httpRequest.AsyncHttpClient;
import com.sun.player.syncUtil.socetTimeUtil.CreateIpUtil;
import com.sun.player.syncUtil.wangyiSecurity.JavaEncrypt;
import com.sun.player.webService.util.serchDto.MusicCateGory;
import com.sun.player.webService.util.serchDto.MusicList;
import com.sun.player.webService.util.serchDto.MusicSearchPara;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * @author Sun
 * @version 1.0
 * @see ParseMusicXml
 * @since 2020/1/22 7:47
 **/
@Component
@Slf4j
public class ParseMusicXml {

    @Resource
    AsyncHttpClient asyncHttpClient;

    /***
     * parseMusicKuGou  根据酷狗搜索返回实体对象
     * @param musicSearchPara musicSearchPara
     * @param music music
     * @return java.util.List<com.sun.player.syncDto.MusicWeb>
     * @since 2020/1/25 20:55
     */
    public List<MusicList> parseMusicKuGou(MusicSearchPara musicSearchPara, Music music) {
        List<MusicList> musicList = new ArrayList<>();

        try {

            String searchUrl = music.getSearchUrl() + musicSearchPara.getInput() + "&page=" + musicSearchPara.getPage() + "&pagesize=" + musicSearchPara.getPageSize();
            String s1 = asyncHttpClient.sendMusic(searchUrl, null).get();
            JSONObject jsonObjec = new JSONObject(s1);
            JSONObject data1 = jsonObjec.getJSONObject("data");
            JSONArray info = data1.getJSONArray("info");

            //速度太快,永远不要使用相同的ip

            for (int i = 0; i < info.length(); i++) {

                JSONObject jsonObject1 = info.getJSONObject(i);
                MusicList musicWeb = new MusicList();
                //sqhash 320hash有的没有权限
                String hash = jsonObject1.getString("hash").trim();
                String songname = jsonObject1.getString("songname").trim();
                String singername = jsonObject1.getString("singername").trim();
                String privilege = jsonObject1.getString("privilege").trim();

                //没有版权
                if (privilege.equals("5")) {
                    continue;
                }
                String musicUrl = music.getDetailUrl() + hash;

                //太长了报错?
                if (songname.length() > 200) {
                    songname = songname.substring(0, 200);
                }

                if (singername.length() > 200) {
                    singername = singername.substring(0, 200);
                }
                musicWeb.setName(songname);
                musicWeb.setAuthorName(singername);

                musicWeb.setSongOldUrl(musicUrl);
                musicWeb.setType(1);

                //先保存的id最小

                int i1 = Integer.parseInt(musicSearchPara.getPageSize());
                int page = Integer.parseInt(musicSearchPara.getPage());
                if (i1 < 100) {
                    i1 = 100;
                }
                musicList.add(musicWeb);

            }

        } catch (Exception e) {

        }

        return musicList;
    }

    //实时根据hash获取歌曲的图片,歌词,播放地址,因为播放地址是变动的
    public void parseMusicKuGou(List<MusicList> musicLists, Music music) {

        List<Map<String, Future<String>>> futures = new ArrayList<>();
        for (MusicList musicList : musicLists) {
            Map<String, Future<String>> results = new HashMap<>();
            music.getRequestHead().put("x-forwarded-for", CreateIpUtil.getRandomIp());
            Future<String> stringFuture = asyncHttpClient.sendMusic(musicList.getSongOldUrl(), music.getRequestHead());
            results.put(musicList.getSongOldUrl(), stringFuture);
            futures.add(results);

        }

        for (Map<String, Future<String>> future : futures) {

            future.forEach((k, v) -> {

                String send = null;
                try {
                    send = v.get();
                    JSONObject jsonObject = new JSONObject(send);

                    JSONObject data = jsonObject.getJSONObject("data");
                    String song_name = data.getString("song_name");
                    String author_name = data.getString("author_name");

                    String play_url = null;
                    try {
                        play_url = data.getString("play_url");
                    } catch (JSONException e) {

                    }

                    //没有版权的不要
                    if (play_url == null || play_url.trim().isEmpty()) {
                        for (MusicList musicList : musicLists) {
                            if (musicList.getSongOldUrl().equals(k)) {
                                musicLists.remove(musicList);
                                break;
                            }
                        }
                        return;
                    }
                    List<List<String>> lyric = new ArrayList<>();
                    //歌词去除差异化
                    try {
                        String lyrics = data.getString("lyrics");
                        if (!lyrics.isEmpty()) {

                            String[] split = lyrics.split("\r\n");
                            for (String s : split) {
                                try {
                                    int i1 = s.indexOf("[");
                                    int i2 = s.indexOf("]");
                                    String substring1 = s.substring(i1 + 1, i2);

                                    String pattern = "\\d{2}:\\d{2}.\\d{2}(.*)";
                                    boolean isMatchDate1 = Pattern.matches(pattern, substring1);
                                    if (isMatchDate1) {
                                        //把时间格式转换为s

                                        String[] split1 = substring1.split(":");
                                        String[] split2 = split1[1].split("\\.");

                                        Integer ss;
                                        //分
                                        int i4 = Integer.parseInt(split1[0]);
                                        //s
                                        int i5 = Integer.parseInt(split2[0]);
                                        //毫秒
                                        int i3 = Integer.parseInt(split2[1]);

                                        ss = i4 * 60 + i5;

                                        String substring = s.substring(i2 + 1);
                                        List strings = new ArrayList();
                                        strings.add(ss.toString() + "." + i3);
                                        strings.add(substring);
                                        lyric.add(strings);
                                    }
                                } catch (NumberFormatException e) {

                                }

                            }

                        }
                    } catch (NumberFormatException e) {
                        log.error("没有歌词", "");
                    }

                    String avatar = data.getString("img");

                    for (MusicList musicList : musicLists) {
                        if (musicList.getSongOldUrl().equals(k)) {

                            musicList.setPicUrl(avatar);
                            musicList.setLyrics(lyric);
                            musicList.setSongUrl(play_url);
                            musicList.setName(song_name);
                            musicList.setAuthorName(author_name);
                            musicList.setType(1);
                            break;
                        }
                    }
                } catch (Exception e) {

                }
            });
        }
    }



    /*网易云*/

    public List<MusicList> parseMusicWangYi(MusicSearchPara musicSearchPara, Music music) {

        List<MusicList> music1 = new ArrayList<>();
        //获取歌曲id的地址
        String url = music.getSearchUrl();
        //组装发送参数 一次100
        String searchPara = "{\"hlpretag\":\"<span class=\\\"s-fc7\\\">\",\"hlposttag\":\"</span>\",\"id\":\"1001\",\"s\":\"%s\",\"type\":\"1\",\"offset\":\"%s\",\"total\":\"false\",\"limit\":\"%s\",\"csrf_token\":\"\"}";

        int offset = Integer.parseInt(musicSearchPara.getPageSize()) * (Integer.parseInt(musicSearchPara.getPage()) - 1);
        String ser = String.format(searchPara, musicSearchPara.getInput(), offset + "", musicSearchPara.getPageSize());

        try {
            //加密
            HashMap<String, String> encrypt = JavaEncrypt.encrypt(ser);

            Future<String> future1 = asyncHttpClient.sendMusicPost(url, encrypt, null);

            String s2 = future1.get();

            music1 = getMusic(s2);
        } catch (Exception e) {
            return music1;
        }
        return music1;
    }

    //根据查询歌曲结果,组装歌曲
    public List<MusicList> getMusic(String result) {

        List<MusicList> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);

            JSONObject result_ = jsonObject.getJSONObject("result");

            JSONArray songs = null;

            songs = result_.getJSONArray("songs");

            //一页100条,这样看看能不能防止速度太快
            for (int i = 0; i < songs.length(); i++) {

                MusicList musicList = new MusicList();

                JSONObject jsonObject1 = songs.getJSONObject(i);
                //版权
                JSONObject privilege = jsonObject1.getJSONObject("privilege");
                String fee = privilege.getString("fee");
                String payed = privilege.getString("payed");
                String pl = privilege.getString("pl");
                String dl = privilege.getString("dl");

                /*//没有版权
                if ((fee.equals("0") || payed == null || payed.isEmpty() || payed.equals("0")) && Integer.parseInt(pl) > 0 && dl.equals("0")) {

                    continue;
                }
                //没有版权
                if (pl.equals("0") && dl.equals("0")) {

                    continue;
                }*/

                String name = jsonObject1.getString("name");
                String id = jsonObject1.getString("id");
                JSONObject al = jsonObject1.getJSONObject("al");
                String picUrl = al.getString("picUrl");
                JSONArray ar = jsonObject1.getJSONArray("ar");
                String name1 = ar.getJSONObject(0).getString("name");
                //网易
                musicList.setType(2);
                musicList.setName(name);
                musicList.setPicUrl(picUrl);
                musicList.setAuthorName(name1);

                //到时候要根据这个连接获取播放地址,因为地址时变的
                musicList.setSongUrl("http://music.163.com/song/media/outer/url?id=" + id + ".mp3");

                musicList.setSongOldUrl("http://music.163.com/song/media/outer/url?id=" + id + ".mp3");
                list.add(musicList);

            }

            //保存当前歌手的所有歌曲
        } catch (Exception e) {

            return list;
        }
        return list;
    }

    //发送id获取歌词

    public void sendGeCi(List<MusicList> musicLists, Music music) {
        List<Map<String, Future<String>>> futures = new ArrayList<>();

        for (MusicList musicList : musicLists) {

            Map<String, String> map = new HashMap<>();
            map.put("x-forwarded-for", CreateIpUtil.getRandomIp());
            Map<String, Future<String>> results = new HashMap<>();
            String songUrl = musicList.getSongUrl();
            int i = songUrl.indexOf("id=");
            String id = songUrl.substring(i + 3, songUrl.length() - 4);
            String url = String.format(music.getDetailUrl(), id);
            Future<String> stringFuture = asyncHttpClient.sendMusic(url, map);
            results.put(musicList.getSongUrl(), stringFuture);
            futures.add(results);
        }

        for (Map<String, Future<String>> future : futures) {

            future.forEach((k, v) -> {
                List<List<String>> geCi = getGeCi(v);

                for (MusicList musicList : musicLists) {

                    if (musicList.getSongUrl().equals(k)) {
                        musicList.setLyrics(geCi);
                        break;
                    }
                }

            });
        }
    }

    //根据id获取歌词

    public List<List<String>> getGeCi(Future<String> stringFuture) {
        List<List<String>> lists = new ArrayList<>();
        StringBuffer s1 = new StringBuffer();
        try {

            s1.append(stringFuture.get());
            JSONObject jsonObject = new JSONObject(s1.toString());
            JSONObject lrc = jsonObject.getJSONObject("lrc");
            String lyric = lrc.getString("lyric");
            String[] split = lyric.split("\n");
            for (String s : split) {
                try {
                    int i1 = s.indexOf("[");
                    int i2 = s.indexOf("]");
                    if (i1 == -1 || i2 == -1) {
                        continue;
                    }
                    String substring1 = null;
                    try {
                        substring1 = s.substring(i1 + 1, i2);
                    } catch (Exception e) {

                        log.info("没有找到合适的歌词");

                        return lists;
                    }

                    String pattern = "\\d{2}:\\d{2}(.*)";
                    boolean isMatchDate1 = Pattern.matches(pattern, substring1);
                    if (isMatchDate1) {
                        //把时间格式转换为s

                        String[] split1 = substring1.split(":");
                        String[] split2 = split1[1].split("\\.");

                        Integer ss;
                        //分
                        int i4 = Integer.parseInt(split1[0]);
                        //s
                        int i5 = Integer.parseInt(split2[0]);
                        //毫秒
                        String s2 = "00";
                        if (split2.length > 1) {

                            s2 = split2[1];
                        }
                        if (s2.length() > 2) {
                            s2 = s2.substring(0, 2);
                        }

                        ss = i4 * 60 + i5;

                        String substring = s.substring(i2 + 1);
                        List strings = new ArrayList();

                        strings.add(ss.toString() + "." + s2);
                        strings.add(substring);
                        lists.add(strings);
                    }
                } catch (NumberFormatException e) {

                }

            }
        } catch (Exception e) {

        }

        return lists;

    }

    /*** 获取歌曲的主题
     * getMusicCateGoryKuGou
     * @param html html
     * @return java.util.List<com.sun.player.webService.util.serchDto.MusicCateGory>
     * @since 2020/2/6 15:31
     */

    public List<MusicCateGory> getMusicCateGoryKuGou(String html) {

        List<MusicCateGory> list_ = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(html);
            JSONObject rank = jsonObject.getJSONObject("rank");
            JSONArray list = rank.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                MusicCateGory musicCateGory = new MusicCateGory();
                JSONObject o = list.getJSONObject(i);
                String rankid = o.getString("rankid");
                //正方形图片
                String imgurl = o.getString("imgurl");
                //长方形图片
                String bannerurl = o.getString("bannerurl");
                String rankname = o.getString("rankname");

                musicCateGory.setId(rankid);

                imgurl = imgurl.replaceAll("\\{size\\}", "400");
                musicCateGory.setPicUrl(imgurl);

                musicCateGory.setShowName(rankname);

                list_.add(musicCateGory);
            }
        } catch (JSONException e) {

        }

        return list_;
    }

    /*** 根据热门主题获取里面歌曲的hash
     * getMusicListFromCateGory
     * @param html html
     * @return java.util.List<java.lang.String>
     * @since 2020/2/6 15:42
     */

    public List<String> getMusicListFromCateGory(String html) {
        List<String> hashs = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(html);

            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray info = data.getJSONArray("info");

            for (int i = 0; i < info.length(); i++) {

                Music music = new Music();

                JSONObject jsonObject1 = info.getJSONObject(i);
                String hash = jsonObject1.getString("hash");

                String privilege = jsonObject1.getString("privilege");

                if (privilege.equals("5"))
                    continue;
                if (hash != null && !hash.isEmpty())
                    hashs.add(hash);

            }
        } catch (JSONException e) {

        }

        return hashs;
    }
}











