package com.sun.player.syncDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Sun
 * @version 1.0
 * @see VideoList
 * @since 2019/12/19 23:48
 **/
@Data
@Table(indexes = {
        @Index(name = "name_search", columnList = "name"),
        @Index(name = "sort_time", columnList = "releaseTime"),
        @Index(name = "find_pid", columnList = "pid"),
        @Index(name = "find_area", columnList = "area")
})
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)

public class VideoList {

    //全局id
    @Id
    private String id;
    //二级菜单id
    private String pid;

    //观看地址
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<String> lookUrl;
    //下载地址
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<String> downUrl;

    //影片名
    private String name;
    //影片图片
    private String picture;

    //图片地址,如果没有本地图片就取图片地址
    private String pictureUrl;
    //别名
    private String otherName;
    //导演
    private String director;
    //主演
    private String actors;
    //地区
    private String area;
    //语言
    private String language;

    //上映时间
    private Integer releaseTime;
    //片长
    private String filmLength;
    //更新日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updatedTime;
    //总播放量
    private String totalPlayTimes;
    //今日播放量
    private String todayPlayTimes;
    //总评分
    private String totalScore;
    //评分次数
    private String scoresNum;
    //影片简介
    @Column(length = 10000)
    private String filmSynopsis;
    //显示评分
    private String showScore;
    //更新至--或者是影片清晰度
    private String newInfo;

}











