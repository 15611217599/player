package com.sun.player;

import com.sun.player.webService.util.serchDto.MusicList;
import com.sun.player.syncUtil.httpRequest.AsyncHttpClient;
import com.sun.player.syncUtil.wangyiSecurity.JavaEncrypt;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Sun
 * @version 1.0
 * @see MoveElementTest
 * @since 2020/1/23 8:09
 **/
public class MoveElementTest {

    public static void main(String[] args) throws Exception {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        String url = "https://music.163.com/weapi/cloudsearch/get/web?csrf_token=";

        Map<String, String> para = new HashMap<>();
        para.put("Referer", "https://music.163.com/search/");
        para.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");

        String serchPara = "{\"hlpretag\":\"<span class=\\\"s-fc7\\\">\",\"hlposttag\":\"</span>\",\"id\":\"1001\",\"s\":\"陈奕迅\",\"type\":\"1\",\"offset\":\"0\",\"total\":\"true\",\"limit\":\"100\",\"csrf_token\":\"\"}";

        HashMap<String, String> encrypt = JavaEncrypt.encrypt(serchPara);

        Future<String> stringFuture = asyncHttpClient.sendMusicPost(url, encrypt, para);

        String s = stringFuture.get();

        JSONObject jsonObject = new JSONObject(s);

        JSONObject result = jsonObject.getJSONObject("result");

        JSONArray songs = result.getJSONArray("songs");
        String songCount = result.getString("songCount");

        Map<String, String> nams_1 = new HashMap<>();
        for (int i = 0; i < songs.length(); i++) {

            MusicList musicList = new MusicList();

            JSONObject jsonObject1 = songs.getJSONObject(i);
            String name = jsonObject1.getString("name");
            String id = jsonObject1.getString("id");
            JSONObject al = jsonObject1.getJSONObject("al");

            JSONObject privilege = jsonObject1.getJSONObject("privilege");

            String fee = privilege.getString("fee");

            if (!fee.equals("0"))
                nams_1.put(name, fee);

            String picUrl = al.getString("picUrl");


            musicList.setName(name);
            musicList.setPicUrl(picUrl);
            musicList.setAuthorName("");

        }

        nams_1.forEach((k, v) -> {
            System.out.println(k + v);
        });

        /*String url = "http://music.163.com/song/media/outer/url?id=1306459970";
        Map<String, String> map = new HashMap<>();
        map.put("x-forwarded-for", CreateIpUtil.getRandomIp());
        Future<String> stringFuture = asyncHttpClient.sendMusic(url, map);

        String s = stringFuture.get();
        s.length();*/

    }
}











