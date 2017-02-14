package com.myee.tarot.configuration.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.myee.tarot.catalog.dao.ProductUsedDao;
import com.myee.tarot.catalog.domain.ProductUsed;
import com.myee.tarot.configuration.dao.UpdateConfigProductUsedXREFDao;
import com.myee.tarot.configuration.domain.UpdateConfigProductUsedXREF;
import com.myee.tarot.core.Constants;
import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.core.util.DateTimeUtils;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.core.util.StringUtil;
import com.myee.tarot.core.util.WhereRequest;
import com.myee.tarot.configuration.dao.UpdateConfigDao;
import com.myee.tarot.configuration.domain.QUpdateConfig;
import com.myee.tarot.configuration.domain.UpdateConfig;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chay on 2016/12/15.
 */
@Repository
public class UpdateConfigDaoImpl extends GenericEntityDaoImpl<Long, UpdateConfig> implements UpdateConfigDao {

    @Autowired
    protected ProductUsedDao productUsedDao;
    @Autowired
    protected UpdateConfigProductUsedXREFDao updateConfigProductUsedXREFDao;

    public PageResult<UpdateConfig> page(WhereRequest whereRequest) throws ParseException {
        PageResult<UpdateConfig> pageList = new PageResult<UpdateConfig>();
        QUpdateConfig qUpdateConfig = QUpdateConfig.updateConfig;
        JPQLQuery<UpdateConfig> query = new JPAQuery(getEntityManager());
        query.from(qUpdateConfig);
        if (whereRequest.getQueryObj() != null) {
            JSONObject map = JSON.parseObject(whereRequest.getQueryObj());
            Object obj = null;
            obj = map.get(Constants.SEARCH_UPDATE_CONFIG_DEVICE_GROUP_NO);
            if (obj != null && !StringUtil.isBlank(obj.toString())) {
                List<Long> productUsedIdList = productUsedDao.listIDsLikeCode(obj.toString());
                //如果找到的数组长度是0，则直接返回空结果集
                if(productUsedIdList.size() == 0) {
                    return emptyPageResult(pageList);
                }

                //存放设备关联配置的ID
                List<Long> updateConfigIdList = updateConfigProductUsedXREFDao.listUpdateConfigIdsByProductUsedIds(productUsedIdList);
                if(updateConfigIdList.size() == 0) {
                    return emptyPageResult(pageList);
                }

                query.where(qUpdateConfig.id.in(updateConfigIdList));
/*//                BooleanExpression booleanExpression = null;
                List<UpdateConfigProductUsedXREF> updateConfigProductUsedXREFTemp = null;
                for( int i = 0; i < size;i++ ) {
                    ProductUsed productUsedTemp = productUsedList.get(i);
                    String idTemp = productUsedTemp.getId().toString();
                    //从关系表查找出该设备关联的所有配置记录
                    updateConfigProductUsedXREFTemp =
                    if( updateConfigProductUsedXREFTemp != null && updateConfigProductUsedXREFTemp.size() > 0) {
                        updateConfigProductUsedXREFList.addAll(updateConfigProductUsedXREFTemp);
                    }


//                    if( i == 0) {
//                        booleanExpression = qUpdateConfig.deviceGroupNOList.like("%" + idTemp +"%");
//                    }
//                    else{
//                        booleanExpression = booleanExpression.or(qUpdateConfig.deviceGroupNOList.like("%" + idTemp +"%"));
//                    }
                }
//                if(booleanExpression != null) {
//                    query.where(booleanExpression);
//                }*/
            }
            obj = map.get(Constants.SEARCH_OPTION_TYPE);
            if (obj != null && !StringUtil.isBlank(obj.toString())) {
                query.where(qUpdateConfig.type.eq(obj.toString()));
            }
            obj = map.get(Constants.SEARCH_UPDATE_CONFIG_SEE_TYPE);
            if (obj != null && !StringUtil.isBlank(obj.toString())) {
                query.where(qUpdateConfig.seeType.eq(obj.toString()));
            }
            obj = map.get(Constants.SEARCH_UPDATE_CONFIG_BRANCH_ID);
            if (obj != null && !StringUtil.isBlank(obj.toString())) {
                query.where(qUpdateConfig.branchConfig.id.eq(Long.parseLong(obj.toString())));
            }
            //TODO  临时处理，前端未找到好的处理方式
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            obj = map.get(Constants.SEARCH_BEGIN_DATE);
            if (obj != null && !StringUtil.isBlank(obj.toString())) {
                String dateString = obj.toString();
                query.where(qUpdateConfig.createTime.after(DateTimeUtils.calBeginDay(dateString, format)));
            }
            obj = map.get(Constants.SEARCH_END_DATE);
            if (obj != null && !StringUtil.isBlank(obj.toString())) {
                String dateString = obj.toString();
                query.where(qUpdateConfig.createTime.before(DateTimeUtils.calEndDay(dateString, format)));
            }
        }
        pageList.setRecordsTotal(query.fetchCount());

        query.orderBy(qUpdateConfig.createTime.desc());
        if (whereRequest.getCount() > Constants.COUNT_PAGING_MARK) {
            query.offset(whereRequest.getOffset()).limit(whereRequest.getCount());
        }
        List<UpdateConfig> lists = query.fetch();
        pageList.setList(lists);
        return pageList;
    }

    private PageResult emptyPageResult(PageResult pageList) {
        pageList.setRecordsTotal(0);
        pageList.setList(Collections.<UpdateConfig>emptyList());
        return pageList;
    }
}
