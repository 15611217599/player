package com.sun.player.syncDto;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

/**
 * 用来存放各个音乐的参数的
 * @author Sun
 * @version 1.0
 * @see Music
 * @since 2020/1/22 17:31
 **/

@Entity
@Data
public class Music {

    //1 酷狗
    @Id
    private String id;
    //网易 qq
    private String showName;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private Map<String, String> requestHead;

    private String searchUrl;

    private String detailUrl;
}











