package com.sun.player.syncUtil.socetTimeUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sun
 * @version 1.0
 * @see CreateIpUtil
 * @since 2020/1/24 9:44
 **/
public class CreateIpUtil {

    public static AtomicInteger currentIndex = new AtomicInteger(0);
    public static AtomicInteger threeNum = new AtomicInteger(1);
    public static AtomicInteger forNum = new AtomicInteger(1);

    // ip范围
    public static int[][] range = {{607649792, 608174079}, // 36.56.0.0-36.63.255.255
            {1038614528, 1039007743}, // 61.232.0.0-61.237.255.255
            {1783627776, 1784676351}, // 106.80.0.0-106.95.255.255
            {2035023872, 2035154943}, // 121.76.0.0-121.77.255.255
            {2078801920, 2079064063}, // 123.232.0.0-123.235.255.255
            {-1950089216, -1948778497}, // 139.196.0.0-139.215.255.255
            {-1425539072, -1425014785}, // 171.8.0.0-171.15.255.255
            {-1236271104, -1235419137}, // 182.80.0.0-182.92.255.255
            {-770113536, -768606209}, // 210.25.0.0-210.47.255.255
            {-569376768, -564133889}, // 222.16.0.0-222.95.255.255
    };

    public static String getRandomIp() {

        String ip = num2ip(range[currentIndex.get()][0]);
       /* Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));*/
        return ip;
    }

    /*
     * 将十进制转换成IP地址
     */
    public static String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";
        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        //x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);

        int four = forNum.incrementAndGet();
        int three = threeNum.get();

        if (four >= 255) {
            //先设置第四位
            forNum.set(1);
            four = 1;

            three = threeNum.incrementAndGet();

            if (three >= 255) {//直接递归也不需要赋值

                threeNum.set(1);
                int i = currentIndex.incrementAndGet();
                if (i > 9) {//全部重置
                    currentIndex.set(0);
                }
                return num2ip(range[currentIndex.get()][0]);
            }

        }

        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + three + "." + four;

        return x;
    }

    public static void main(String[] args) {

        Set<String> ips = new HashSet<>();
        int count = 1000000;
        for (int i = 0; i < count; i++) {
            String randomIp = getRandomIp();
            ips.add(randomIp);
            System.err.println(randomIp);
        }
        System.err.println(ips.size());

    }
}











