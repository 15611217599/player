package com.sun.player.syncUtil.httpRequest;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author Sun
 * @version 1.0
 * @see HttpClient
 * @since 2019/12/20 16:21
 **/
public class HttpClient {

    private static CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private static RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(60000)
            .setConnectionRequestTimeout(60000)
            .setSocketTimeout(60000)
            .build();

    public static String send(String url, Map<String, String> para) {
        try {

            // 创建Get请求
            HttpGet httpGet = new HttpGet(url);
            if (para != null) {
                para.forEach((k, v) -> {
                    httpGet.setHeader(k, v);
                });
            }

            httpGet.setConfig(config);
            CloseableHttpResponse execute = httpClient.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            String s = EntityUtils.toString(entity);
            return s;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] sendByte(String url) {
        try {

            // 创建Get请求
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(config);
            CloseableHttpResponse execute = httpClient.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            byte[] s = EntityUtils.toByteArray(entity);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String send(String url, String encode) {
        try {

            // 创建Get请求
            HttpGet httpGet = new HttpGet(url);

            httpGet.setConfig(config);
            CloseableHttpResponse execute = httpClient.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            String s = EntityUtils.toString(entity, encode);
            return s;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}











