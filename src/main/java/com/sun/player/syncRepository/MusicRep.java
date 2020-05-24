package com.sun.player.syncRepository;

import com.sun.player.syncDto.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Sun
 * @version 1.0
 * @see MusicRep
 * @since 2020/1/22 17:32
 **/

@Repository
public interface MusicRep extends JpaRepository<Music, String> {
}











