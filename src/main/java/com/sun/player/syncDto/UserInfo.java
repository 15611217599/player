package com.sun.player.syncDto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * @author Sun
 * @version 1.0
 * @see UserInfo
 * @since 2020/2/17 17:13
 **/

@Entity
@Data
public class UserInfo {

    @Id
    private String name = "";
    private String pass = "";
    private Timestamp activeDate;
    private String questions = "";
    private String answer = "";
    private boolean isVip = false;
}











