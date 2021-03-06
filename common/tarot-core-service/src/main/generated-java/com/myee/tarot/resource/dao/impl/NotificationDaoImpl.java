package com.myee.tarot.resource.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.myee.tarot.core.Constants;
import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.core.util.DateTimeUtils;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.core.util.StringUtil;
import com.myee.tarot.core.util.WhereRequest;
import com.myee.tarot.resource.dao.NotificationDao;
import com.myee.tarot.resource.domain.Notification;
import com.myee.tarot.resource.domain.QNotification;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Ray.Fu on 2016/8/10.
 */
@Repository
public class NotificationDaoImpl extends GenericEntityDaoImpl<Long, Notification> implements NotificationDao {

    @Override
    public PageResult<Notification> pageByStore(Long id, WhereRequest whereRequest) throws ParseException {
        PageResult<Notification> pageList = new PageResult<Notification>();
        QNotification qNotification = QNotification.notification;
        JPQLQuery<Notification> query = new JPAQuery(getEntityManager());
        query.from(qNotification);
		if (whereRequest.getQueryObj() != null) {
			JSONObject map = JSON.parseObject(whereRequest.getQueryObj());
			Object obj = map.get(Constants.SEARCH_NOTICE_TYPE);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				query.where(qNotification.noticeType.eq(obj.toString()));
			}
			obj = map.get(Constants.SEARCH_CONTENT);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				query.where(qNotification.content.like("%" + obj.toString() + "%"));
			}
			//TODO  临时处理，前端未找到好的处理方式
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			obj = map.get(Constants.SEARCH_BEGIN_DATE);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				String dateString = obj.toString();
				query.where(qNotification.createTime.after(DateTimeUtils.calBeginDay(dateString,format)));
			}
			obj = map.get(Constants.SEARCH_END_DATE); //往前一天的0点作为查询时间
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				String dateString = obj.toString();
				query.where(qNotification.createTime.before(DateTimeUtils.calEndDay(dateString,format)));
			}
		}
//        query.where(qNotification.store.id.eq(id));  //日志查询不区分门店
        pageList.setRecordsTotal(query.fetchCount());

        query.orderBy(qNotification.createTime.desc());
        if( whereRequest.getCount() > Constants.COUNT_PAGING_MARK){
            query.offset(whereRequest.getOffset()).limit(whereRequest.getCount());
        }
		List<Notification> lists = query.fetch();
        pageList.setList(lists);
        return pageList;
    }
}
