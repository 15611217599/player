package com.sun.player.syncRepository;

import com.sun.player.syncDto.VideoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Sun
 * @version 1.0
 * @see VideoCategoryRep
 * @since 2019/12/19 21:00
 **/
@Repository
public interface VideoCategoryRep extends JpaRepository<VideoCategory, String> {


    Optional<List<VideoCategory>> findByPidOrderBySortIdAsc(String pid);
}



