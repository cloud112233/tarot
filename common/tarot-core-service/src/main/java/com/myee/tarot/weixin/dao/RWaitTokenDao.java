package com.myee.tarot.weixin.dao;

import com.myee.tarot.core.dao.GenericEntityDao;
import com.myee.tarot.weixin.domain.RWaitToken;

import java.util.List;

/**
 * Created by Ray on 2016/7/4.
 */
public interface RWaitTokenDao extends GenericEntityDao<Long, RWaitToken> {
    /*@Modifying
    @Query("update RWaitToken rw set rw.state = ?1,rw.updated = FROM_UNIXTIME(?6) where rw.orgID = ?2 and rw.clientID = ?3 and rw.token = ?4 and UNIX_TIMESTAMP(rw.timeTook) = ?5")
    @Transactional*/
    public Integer updateState(Integer state, Long shopId, Long clientId, String token, Long timeTook, Long updateTime);

    /*@Modifying
    @Query("update RWaitToken rw set rw.openId = ?1 where rw.identityCode = ?2 and UNIX_TIMESTAMP(rw.timeTook) = ?3")*/
    public Integer updateWaitTokenOpenId(String openId, String identityCode, Long date);

    /*@Modifying
    @Query("update RWaitToken rw set rw.waitedCount = ?1,rw.predictWaitingTime =?4 where rw.identityCode = ?2 and UNIX_TIMESTAMP(rw.timeTook) = ?3")*/
    public Integer modifyWaitingInfo(Long waitedCount, String identityCode, Long date, Long predictWaitingTime);

//    @Query("select rw from RWaitToken rw where rw.identityCode = ?1 and UNIX_TIMESTAMP(rw.timeTook) >= ?2 and UNIX_TIMESTAMP(rw.timeTook) <= ?3")
    public RWaitToken selectTokenByIc(String identityCode, Long beginTime, Long endTime);

//    @Query("select rw from RWaitToken rw where rw.clientID = ?1 and rw.orgID = ?2 and rw.tableTypeId = ?3 and rw.state = ?4")
    public List<RWaitToken> selectAllTokenByInfo(Long clientId, Long orgId, Long tableTypeId, Integer state);

//    @Query("select rw from RWaitToken rw where rw.clientID = ?1 and rw.orgID = ?2 and rw.tableTypeId = ?3 and rw.state = ?4 and rw.openId is not null")
    public List<RWaitToken> selectAllTokenOpenIdNotNull(Long clientId, Long orgId, Long tableTypeId, Integer state);

//    @Query("select rw from RWaitToken rw where rw.openId = ?1 and rw.state = ?2")
    public List<RWaitToken> selectAllTokenByOpenIdState(String openId, Integer state);

//    @Query("select rw from RWaitToken rw where rw.openId = ?1 and UNIX_TIMESTAMP(rw.timeTook) >= ?2 and UNIX_TIMESTAMP(rw.timeTook) <= ?3")
    public List<RWaitToken> selectWait(String openId, Long bTime, Long eTime);

    /*@Modifying
    @Query("update RWaitToken rw set rw.state = ?1 where rw.waitTokenId = ?2")*/
    public Integer modifyWaitingStatus(Integer state, Long waitQueueId);
}



