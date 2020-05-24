package com.sun.player.webService.util.serchDto;

import com.sun.player.syncDto.UserInfo;
import lombok.Data;

/**
 * @author Sun
 * @version 1.0
 * @see UserInfoResl
 * @since 2020/2/17 17:21
 **/
@Data
public class UserInfoResl {

    //0成功 1失败
    private String state = "0";

    //返回信息
    private String msg;

    //用户信息
    private UserInfo userInfo;
}











