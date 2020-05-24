package com.sun.player.syncDto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Sun
 * @version 1.0
 * @see VideoCategory
 * @since 2019/12/19 16:16
 **/
@Entity
@Table(name = "video_category")
@Data
public class VideoCategory {
    @Id
    private String id;
    private String pid;
    private String url;
    private String name;
    private String sortId;
}











