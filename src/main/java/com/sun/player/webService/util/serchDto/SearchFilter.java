package com.sun.player.webService.util.serchDto;

import lombok.Data;

import java.util.List;

/**
 * @author Sun
 * @version 1.0
 * @see SearchFilter
 * @since 2020/1/14 13:42
 **/
@Data
public class SearchFilter {
    private String name;
    private String link_name;
    private List<String> item;
    private List<String> itemName;
}











