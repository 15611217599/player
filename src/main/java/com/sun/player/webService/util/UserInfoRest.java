package com.sun.player.webService.util;

import com.sun.player.syncDto.UserInfo;
import com.sun.player.syncRepository.UserInfoRep;
import com.sun.player.webService.util.serchDto.UserInfoResl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author Sun
 * @version 1.0
 * @see UserInfoRest
 * @since 2020/2/17 17:20
 **/
@RestController
public class UserInfoRest {

    @Resource
    UserInfoRep userInfoRep;
    @Value("${config.isShowPay}")
    private boolean isShowPay;
    /***
     * regine  注册
     * @param userInfo userInfo
     * @return com.sun.player.webService.util.serchDto.UserInfoResl
     * @since 2020/2/17 17:29
     */
    @RequestMapping(value = "/regine", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public UserInfoResl regine(@RequestBody UserInfo userInfo) {

        UserInfoResl userInfoResl = new UserInfoResl();
        Optional<UserInfo> byId = userInfoRep.findById(userInfo.getName().trim());

        if (byId.isPresent()) {
            userInfoResl.setState("1");
            userInfoResl.setMsg("用户已存在");

            return userInfoResl;
        }

        userInfo.setVip(false);

        userInfoRep.save(userInfo);

        return userInfoResl;
    }

    /***
     * login 登陆
     * @param userInfo userInfo
     * @return com.sun.player.webService.util.serchDto.UserInfoResl
     * @since 2020/2/17 17:29
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public UserInfoResl login(@RequestBody UserInfo userInfo) {

        UserInfoResl userInfoResl = new UserInfoResl();
        Optional<UserInfo> byId = userInfoRep.findById(userInfo.getName().trim());

        if (byId.isPresent()) {

            UserInfo userInfo1 = byId.get();

            if (userInfo1.getPass().equals(userInfo.getPass())) {
                userInfoResl.setState("0");
                userInfoResl.setUserInfo(userInfo1);
            } else {
                userInfoResl.setState("1");
                userInfoResl.setMsg("密码错误!");
            }

        } else {
            userInfoResl.setState("1");
            userInfoResl.setMsg("用户名不存在!");
        }

        return userInfoResl;
    }

    /***
     * forget 找回密码,前台校验完是否正确了
     * @param userInfo userInfo
     * @return com.sun.player.webService.util.serchDto.UserInfoResl
     * @since 2020/2/17 20:30
     */
    @RequestMapping(value = "/forget", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public UserInfoResl forget(@RequestBody UserInfo userInfo) {

        UserInfoResl userInfoResl = new UserInfoResl();
        Optional<UserInfo> byId = userInfoRep.findById(userInfo.getName().trim());

        if (byId.isPresent()) {

            UserInfo userInfo1 = byId.get();

            if (userInfo1.getAnswer().equals(userInfo.getAnswer())) {
                userInfoResl.setState("0");
                userInfoResl.setUserInfo(userInfo1);

                userInfo1.setPass(userInfo.getPass());

                userInfoRep.save(userInfo1);
            } else {
                userInfoResl.setState("1");
                userInfoResl.setMsg("密保错误!");
            }

        } else {
            userInfoResl.setState("1");
            userInfoResl.setMsg("用户名不存在!");
        }

        return userInfoResl;
    }

    @RequestMapping(value = "/getQuestionforget", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public UserInfoResl forget(@RequestBody String name) {

        UserInfoResl userInfoResl = new UserInfoResl();
        Optional<UserInfo> byId = userInfoRep.findById(name);

        if (byId.isPresent()) {

            UserInfo userInfo1 = byId.get();
            userInfoResl.setState("0");
            userInfoResl.setUserInfo(userInfo1);
        } else {
            userInfoResl.setState("1");
            userInfoResl.setMsg("用户名不存在!");
        }

        return userInfoResl;
    }

    @RequestMapping(value = "/isShowPay", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String forget() {

        if (isShowPay){
            return "true";
        }else {
            return "false";
        }
    }
}











