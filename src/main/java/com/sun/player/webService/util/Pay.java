package com.sun.player.webService.util;

import com.sun.player.syncDto.UserInfo;
import com.sun.player.syncDto.WxPayMoney;
import com.sun.player.syncRepository.UserInfoRep;
import com.sun.player.syncRepository.WxPayMoneyRep;
import com.sun.player.webService.util.serchDto.UserInfoResl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sun
 * @version 1.0
 * @see Pay
 * @since 2020/2/19 23:41
 **/

@RestController
@Slf4j
public class Pay {

    @Resource
    WxPayMoneyRep wxPayMoneyRep;

    @Resource
    UserInfoRep userInfoRep;
    //1 支付的人可以修改  2 取消支付的人可以修改 3 应用程序60s查询不到支付可以修改
    static AtomicBoolean isPaying = new AtomicBoolean(false);
    //正在支付的人
    static String payName = "";

    //支付成功的放在map里面讲道理它必须确认才能山,而且客户端必须确认啊

    static ConcurrentHashMap<String, Date> payMsg = new ConcurrentHashMap();

    /***
     * getPayState 是否可以进行支付 true 可以 false 有人支付
     * @return boolean
     * @since 2020/2/19 23:46
     */
    @RequestMapping(value = "/getPayState", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public boolean getPayState(@RequestBody String name) {

        boolean b = isPaying.get();
        if (b) {
            log.info("用户" + name + "获取支付锁失败");
            return false;
        } else {

            isPaying.set(true);
            payName = name;
            log.info("用户" + name + "锁定了支付");

            //锁定了支付,就要去循环了
            SyncSurePay();
        }

        return true;
    }

    /***
     * consolePay 取消支付
     * @param name name
     * @since 2020/2/20 0:43
     */
    @RequestMapping(value = "/consolePay", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void consolePay(@RequestBody String name) {

        if (payName.equals(name)) {

            log.info("用户" + name + "取消了支付");
            isPaying.set(false);
            payName = "";
        } else {

            log.info("用户" + name + "试图解锁" + payName + "的支付锁失败");
        }

    }

    /***
     * surePayed 查询成功了没
     * @param name name
     * @return com.sun.player.webService.util.serchDto.UserInfoResl
     * @since 2020/2/20 0:44
     */
    @RequestMapping(value = "/surePayed", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public UserInfoResl surePayed(@RequestBody String name) {

        UserInfoResl userInfoResl = new UserInfoResl();
        Date aBoolean = payMsg.get(name);

        if (aBoolean != null) {
            //支付成功了

            log.info("用户" + name + "支付成功了");
            Optional<UserInfo> byId = userInfoRep.findById(name);
            UserInfo r = byId.get();
            userInfoResl.setState("0");
            userInfoResl.setUserInfo(r);

            //删除这条记录
            payMsg.remove(name);
        } else {
            userInfoResl.setState("1");
            log.info("用户" + name + "支付还未成功");
        }

        return userInfoResl;
    }

    /***
     * SyncSurePay 定时查询是否又支付进来
     * @since 2020/2/20 0:11
     */
    public void SyncSurePay() {

        new Thread(() -> {

            //如果没有支付成功

            for (int i = 0; i < 30; i++) {

                //用户取消了啊
                if (!isPaying.get()) {
                    break;
                }
                //查看是否有支付记录
                List<WxPayMoney> all = wxPayMoneyRep.findAll();

                //没有支付记录,继续循环获取
                if (all.isEmpty()) {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {

                    }

                } else {//找到一条支付记录


                    wxPayMoneyRep.deleteAll();

                    try {
                        Optional<UserInfo> byId = userInfoRep.findById(payName);

                        UserInfo userInfo = byId.get();
                        Timestamp activeDate = userInfo.getActiveDate();

                        //第一种 没有充值过的
                        if (activeDate == null) {
                            activeDate = new Timestamp(System.currentTimeMillis());
                        }

                        //往后延迟一个月
                        Calendar cal = Calendar.getInstance();

                        cal.setTime(activeDate);
                        cal.add(Calendar.MONTH, 1);

                        Date date = cal.getTime();

                        userInfo.setActiveDate(new Timestamp(date.getTime()));
                        userInfo.setVip(true);
                        log.info("会员" + payName + "支付成功");

                        userInfoRep.save(userInfo);
                        break;
                    } catch (Exception e) {

                        log.error("会员" + payName + "支付成功,但是保存异常", e);
                    } finally {

                        payMsg.put(payName, new Date());
                        //支付成功,解锁支付模块
                        isPaying.set(false);
                        payName = "";

                    }
                }
            }
            //如果一分钟没人取消也没有成功支付,就取消
            isPaying.set(false);
            payName = "";
        }).start();
    }

}











