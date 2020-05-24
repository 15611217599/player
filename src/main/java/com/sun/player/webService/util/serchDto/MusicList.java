package com.sun.player.webService.util.serchDto;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import java.util.List;

/**
 * @author Sun
 * @version 1.0
 * @see MusicList
 * @since 2020/1/22 14:55
 **/

@Data
public class MusicList {

    private String id;

    //0排行榜 1 酷狗 2网易
    private Integer type;

    private String name;

    private String picUrl;

    private String authorName;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<List<String>> lyrics;

    private String songUrl;

    private String songOldUrl;
}









