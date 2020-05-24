package com.sun.player.syncUtil.socetTimeUtil;

import com.sun.player.syncUtil.httpRequest.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sun
 * @version 1.0
 * @see TimeTest
 * @since 2019/12/20 16:40
 **/

@Component
@Slf4j
public class TimeTest {

    public static String getBestUrl(List<String> urls) {
        Map<String, Long> times = new HashMap<>();
        for (String url : urls) {
            long l = System.currentTimeMillis();
            String send = HttpClient.send(url, new HashMap<>());
            if (send != null && !send.contains("504")) {
                times.put(url, System.currentTimeMillis() - l);
                log.info("网络测试: url " + url + " 耗时 :" + (System.currentTimeMillis() - l) + "ms");
            }
        }
        List<Map.Entry<String, Long>> list = new ArrayList<>(times.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getValue));
        if (list.isEmpty()) {
            return urls.get(0);
        }
        return list.get(0).getKey();
    }
}











