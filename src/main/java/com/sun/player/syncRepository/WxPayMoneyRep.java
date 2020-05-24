package com.sun.player.syncRepository;

import com.sun.player.syncDto.WxPayMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Sun
 * @version 1.0
 * @see WxPayMoneyRep
 * @since 2020/2/20 0:09
 **/

@Repository
public interface WxPayMoneyRep extends JpaRepository<WxPayMoney,Integer> {
}



