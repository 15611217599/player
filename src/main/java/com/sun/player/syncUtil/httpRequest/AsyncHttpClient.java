package com.sun.player.syncUtil.httpRequest;

import com.sun.player.syncUtil.socetTimeUtil.CreateIpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Sun
 * @version 1.0
 * @see AsyncHttpClient
 * @since 2019/12/19 21:20
 **/
@Component
@Slf4j
public class AsyncHttpClient {

    private static PoolingHttpClientConnectionManager cm;
    private static RequestConfig build;
    private static HttpRequestRetryHandler httpRequestRetryHandler;
    private static ConnectionKeepAliveStrategy myStrategy;

    static {
        cm = new PoolingHttpClientConnectionManager();

        build = RequestConfig.custom()
                .setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000).setSocketTimeout(10000)
                .build();

        // 设置最大连接数
        cm.setMaxTotal(100);
        // 将每个路由默认最大连接数
        cm.setDefaultMaxPerRoute(100);

        // 请求重试处理
        httpRequestRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof SSLException) {// SSL握手异常
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        };

        //连接策略
        myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator
                        (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase
                            ("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 600 * 1000;//如果没有约定，则默认定义时长为600s
            }
        };

        //检测链路
        new IdleConnectionMonitorThread(cm).start();

    }

    @Async("sendPool")
    public Future<String> send(String baseUrl, List<String> baseUrls, String url) {

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).setDefaultRequestConfig(AsyncHttpClient.build).setKeepAliveStrategy(myStrategy).build();

        long start = System.currentTimeMillis();

        List<String> urls = new ArrayList<>(baseUrls);
        urls.remove(baseUrl);
        HttpGet httpGet = new HttpGet(baseUrl + url);

        httpGet.setHeader(":authority", "wolongzy.net");
        httpGet.setHeader(":method", "GET");
        httpGet.setHeader(":path", "/");
        httpGet.setHeader(":scheme", "https");
        httpGet.setHeader(":accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        httpGet.setHeader("x-forwarded-for", CreateIpUtil.getRandomIp());
        httpGet.setHeader("accept-encoding", "gzip, deflate, br");
        httpGet.setHeader("accept-language", "zh-CN,zh;q=0.9");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36");
        httpGet.setHeader("cache-control", "max-age=0");
        httpGet.setHeader("cookie", "PHPSESSID=01s44etqv6r7u32lvkq6n4p111; Hm_lvt_5264ef815f471ca9f4cb4d116d3b3f3c=1582273993,1582297508,1582363946; Hm_lpvt_5264ef815f471ca9f4cb4d116d3b3f3c=1582379561");
        httpGet.setHeader("sec-fetch-dest", "document");
        httpGet.setHeader("sec-fetch-mode", "navigate");
        httpGet.setHeader(":sec-fetch-site", "none");
        httpGet.setHeader("sec-fetch-user", "?1");
        httpGet.setHeader("upgrade-insecure-requests", "1");

        String s = "";
        try {
            CloseableHttpResponse execute = client.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            s = EntityUtils.toString(entity);
        } catch (Exception e) {
            Iterator<String> iterator = urls.iterator();

            while (iterator.hasNext()) {
                String url1 = iterator.next();
                try {
                    HttpGet httpGet_ = new HttpGet(url1 + url);
                    log.info("发送时超时,更换地址 " + baseUrl + "为 " + url1 + "重试");
                    CloseableHttpResponse execute = client.execute(httpGet_);
                    HttpEntity entity = execute.getEntity();
                    s = EntityUtils.toString(entity);
                    log.info("重新发送成功 : " + url1);
                    break;
                } catch (IOException ex) {
                    log.error("重试超时", "重试地址 " + url1 + "失败");
                }
            }

        }

        if (s.isEmpty()) {
            log.error("发送失败 : 地址是 -->" + baseUrl + url, baseUrl + url);
        }
        long l = System.currentTimeMillis();
        log.info("发送耗时 : " + (l - start) + "ms");
        return new AsyncResult(s);
    }

    @Async("sendPool")
    public Future<byte[]> sendByte(String url) {

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).setDefaultRequestConfig(AsyncHttpClient.build).setKeepAliveStrategy(myStrategy).build();

        long start = System.currentTimeMillis();

        HttpGet httpGet = new HttpGet(url);
        byte[] result = new byte[0];

        try {
            CloseableHttpResponse execute = client.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            result = EntityUtils.toByteArray(entity);

        } catch (Exception e) {

            for (int i = 0; i < 5; i++) {

                CloseableHttpResponse execute = null;

                try {

                    Thread.sleep(100);
                    execute = client.execute(httpGet);
                    HttpEntity entity = execute.getEntity();
                    result = EntityUtils.toByteArray(entity);

                } catch (Exception ignored) {

                }
                if (!(result.length == 0))
                    break;
            }

            if (result.length == 0)
                log.error("下载照片失败" + url + e);
        }

        long l = System.currentTimeMillis();
        log.info("下载照片耗时 : " + (l - start) + "ms");
        return new AsyncResult(result);
    }

    @Async("sendPool")
    public Future<String> sendMusic(String url, Map<String, String> params) {

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).setDefaultRequestConfig(AsyncHttpClient.build).setKeepAliveStrategy(myStrategy).build();

        long start = System.currentTimeMillis();

        // 创建Get请求
        HttpGet httpGet = new HttpGet(url);

        if (params != null) {
            params.forEach((k, v) -> {
                httpGet.setHeader(k, v);
            });
        }

        String string = "";
        try {

            CloseableHttpResponse execute = client.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            string = EntityUtils.toString(entity);
        } catch (Exception e) {

            for (int i = 0; i < 5; i++) {

                CloseableHttpResponse execute = null;

                try {

                    Thread.sleep(100);
                    execute = client.execute(httpGet);
                    HttpEntity entity = execute.getEntity();
                    string = EntityUtils.toString(entity);

                } catch (Exception ignored) {

                }
                if (!(string.length() == 0))
                    break;
            }

            if (string.length() == 0)
                log.error("获取音乐失败" + url + e);
        }

        long l = System.currentTimeMillis();
        log.info("获取音乐耗时 : " + (l - start) + "ms");
        return new AsyncResult(string);
    }

    @Async("sendPool")
    //网易请求需要post
    public Future<String> sendMusicPost(String url, Map<String, String> params, Map<String, String> heads) {

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).setDefaultRequestConfig(AsyncHttpClient.build).setKeepAliveStrategy(myStrategy).build();

        long start = System.currentTimeMillis();

        // 创建Get请求
        HttpPost httpPost = new HttpPost(url);
        if (heads != null) {
            heads.forEach((k, v) -> {
                httpPost.setHeader(k, v);
            });
        }

        List<NameValuePair> form = new ArrayList<NameValuePair>();

        if (params != null) {
            params.forEach((k, v) -> {
                form.add(new BasicNameValuePair(k, v));
            });
        }

        String string = "";
        try {
            UrlEncodedFormEntity entity_ = new UrlEncodedFormEntity(form,
                    "utf-8");
            httpPost.setEntity(entity_);
            CloseableHttpResponse execute = client.execute(httpPost);
            HttpEntity entity = execute.getEntity();
            string = EntityUtils.toString(entity);
        } catch (Exception e) {

            for (int i = 0; i < 5; i++) {

                CloseableHttpResponse execute = null;

                try {

                    Thread.sleep(100);
                    execute = client.execute(httpPost);
                    HttpEntity entity = execute.getEntity();
                    string = EntityUtils.toString(entity);

                } catch (Exception ignored) {

                }
                if (!(string.length() == 0))
                    break;
            }

            if (string.length() == 0)
                log.error("获取音乐失败" + url + e);
        }

        long l = System.currentTimeMillis();
        log.info("获取音乐耗时 : " + (l - start) + "ms");
        return new AsyncResult(string);
    }

    private static class IdleConnectionMonitorThread extends Thread {

        private final PoolingHttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // 关闭失效的连接
                        connMgr.closeExpiredConnections();
                        // 可选的, 关闭30秒内不活动的连接
                        connMgr.closeIdleConnections(300, TimeUnit.SECONDS);

                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}











