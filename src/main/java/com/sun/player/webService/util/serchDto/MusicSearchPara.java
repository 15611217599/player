package com.sun.player.webService.util.serchDto;

import lombok.Data;

/**
 * @author Sun
 * @version 1.0
 * @see MusicSearchPara
 * @since 2020/1/22 7:50
 **/
@Data
public class MusicSearchPara {

    private String id;

    //输入的值
    private String input;

    //当前页
    private String page = "1";

    //页大小
    private String pageSize = "10";
}











