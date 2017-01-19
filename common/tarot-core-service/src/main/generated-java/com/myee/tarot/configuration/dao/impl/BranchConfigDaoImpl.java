package com.myee.tarot.configuration.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.myee.tarot.configuration.dao.BranchConfigDao;
import com.myee.tarot.configuration.domain.BranchConfig;
import com.myee.tarot.configuration.domain.QBranchConfig;
import com.myee.tarot.core.Constants;
import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.core.util.StringUtil;
import com.myee.tarot.core.util.WhereRequest;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Chay on 2016/12/15.
 */
@Repository
public class BranchConfigDaoImpl extends GenericEntityDaoImpl<Long, BranchConfig> implements BranchConfigDao {

    public PageResult<BranchConfig> page(WhereRequest whereRequest) throws ParseException {
        PageResult<BranchConfig> pageList = new PageResult<BranchConfig>();
        QBranchConfig qBranchConfig = QBranchConfig.branchConfig;
        JPQLQuery<BranchConfig> query = new JPAQuery(getEntityManager());
        query.from(qBranchConfig);
		if (whereRequest.getQueryObj() != null) {
			JSONObject map = JSON.parseObject(whereRequest.getQueryObj());
			Object obj = map.get(Constants.SEARCH_OPTION_NAME);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				query.where(qBranchConfig.name.like("%" + obj.toString() + "%"));
			}
			obj = map.get(Constants.SEARCH_OPTION_SUB_NAME);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				query.where(qBranchConfig.subName.like("%" + obj.toString() + "%"));
			}
			obj = map.get(Constants.SEARCH_OPTION_MANAGER);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				query.where(qBranchConfig.manager.like("%" + obj.toString() + "%"));
			}
			//TODO  临时处理，前端未找到好的处理方式
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			obj = map.get(Constants.SEARCH_BEGIN_DATE);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				String dateString = obj.toString();
				dateString = dateString.replace("T", " ");
				dateString = dateString.replace("Z", " ");
				query.where(qBranchConfig.createTime.after(format.parse(dateString)));
			}
			obj = map.get(Constants.SEARCH_END_DATE);
			if (obj != null && !StringUtil.isBlank(obj.toString())) {
				String dateString = obj.toString();
				dateString = dateString.replace("T", " ");
				dateString = dateString.replace("Z", " ");
				query.where(qBranchConfig.createTime.before(format.parse(dateString)));
			}
		}
        pageList.setRecordsTotal(query.fetchCount());

        query.orderBy(qBranchConfig.name.asc());
        if( whereRequest.getCount() > Constants.COUNT_PAGING_MARK){
            query.offset(whereRequest.getOffset()).limit(whereRequest.getCount());
        }
		List<BranchConfig> lists = query.fetch();
        pageList.setList(lists);
        return pageList;
//		return null;
    }

	public BranchConfig findByName(String branchName, String branchSubName) {
		QBranchConfig qBranchConfig = QBranchConfig.branchConfig;
		JPQLQuery<BranchConfig> query = new JPAQuery(getEntityManager());
		query.from(qBranchConfig)
			.where(qBranchConfig.name.eq(branchName))
			.where(qBranchConfig.subName.eq(branchSubName));
		return query.fetchFirst();
	}
}
