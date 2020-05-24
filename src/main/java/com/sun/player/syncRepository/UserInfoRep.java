package com.sun.player.syncRepository;

import com.sun.player.syncDto.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Sun
 * @version 1.0
 * @see UserInfoRep
 * @since 2020/2/17 17:19
 **/
@Repository
public interface UserInfoRep extends JpaRepository<UserInfo,String> {
}











