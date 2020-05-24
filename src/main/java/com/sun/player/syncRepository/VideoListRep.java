package com.sun.player.syncRepository;

import com.sun.player.syncDto.VideoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * @author Sun
 * @version 1.0
 * @see VideoListRep
 * @since 2019/12/20 19:09
 **/
@Repository
public interface VideoListRep extends JpaRepository<VideoList, String>, JpaSpecificationExecutor<VideoList> {

    /***
     * findAreaPara 根据父id查找到区域筛选条件
     * @param pids pids
     * @return java.util.List<java.lang.String>
     * @since 2020/1/15 7:05
     */
    @Query(value = "select vl.area from VideoList vl  where vl.pid in ?1 group by vl.area order by count(vl.area) desc")
    List<String> findAreaPara(List<String> pids);

    @Query(value = "select vl.releaseTime from VideoList vl where vl.pid in ?1 group by vl.releaseTime order by vl.releaseTime desc")
    List<String> findYearsPara(List<String> pids);

    Optional<List<VideoList>> findByNameLikeAndPidNot(String para,String pid);

    Optional<List<VideoList>> findByPid(String pid);

    @Transactional
    void deleteByPid(String pid);

}











