package com.sun.player.webService.util.serchDto;

import lombok.Data;

/**
 * @author Sun
 * @version 1.0
 * @see SerchPara
 * @since 2020/1/11 1:30
 **/
@Data
public class SerchPara {
    //顶级分类id
    private String ppid;

    //二级id,支持多选
    private String pids;

    //排序方式 0 上映日期 1 更新日期 2 评分最高
    private String sortWay;

    //年代
    private String years;

    //地区
    private String area;

    private int currentPageNum = 0;

    private int pageSize = 4;
}











