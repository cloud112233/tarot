package com.myee.tarot.datacenter.dao.impl;

import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.core.util.WhereRequest;
import com.myee.tarot.datacenter.dao.WaitTokenDao;
import com.myee.tarot.weixin.domain.QWxWaitToken;
import com.myee.tarot.weixin.domain.WxWaitToken;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

/**
 * Created by Jelynn on 2016/7/20.
 */
@Repository
public class WaitTokenDaoImpl  extends GenericEntityDaoImpl<Long, WxWaitToken> implements WaitTokenDao {

    public static Log log = LogFactory.getLog(WaitTokenDaoImpl.class);

    @Override
    public PageResult<WxWaitToken> pageWaitToken(WhereRequest whereRequest) {
        PageResult<WxWaitToken> pageList = new PageResult<WxWaitToken>();
        QWxWaitToken qWxWaitToken = QWxWaitToken.wxWaitToken;
        JPQLQuery<WxWaitToken> query = new JPAQuery(getEntityManager());

        if(StringUtils.isNotBlank(whereRequest.getWaitState())){
            query.where(qWxWaitToken.state.eq(Integer.parseInt(whereRequest.getWaitState())));
        }
        if(null !=whereRequest.getBeginDate()){
            query.where(qWxWaitToken.created.before(whereRequest.getBeginDate()));
        }
        if(null != whereRequest.getEndDate()){
            query.where(qWxWaitToken.created.after(whereRequest.getBeginDate()));
        }
        pageList.setRecordsTotal(query.from(qWxWaitToken).fetchCount());
        if( whereRequest.getCount() > 0){
            query.offset(whereRequest.getOffset()).limit(whereRequest.getCount());
        }
        pageList.setList(query.fetch());
        return pageList;
    }
}