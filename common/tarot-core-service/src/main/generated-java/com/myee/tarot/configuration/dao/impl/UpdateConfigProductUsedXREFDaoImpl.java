package com.myee.tarot.configuration.dao.impl;

import com.myee.tarot.catalog.dao.ProductUsedDao;
import com.myee.tarot.catalog.domain.ProductUsed;
import com.myee.tarot.configuration.domain.UpdateConfig;
import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.core.util.StringUtil;
import com.myee.tarot.configuration.dao.UpdateConfigProductUsedXREFDao;
import com.myee.tarot.configuration.domain.QUpdateConfigProductUsedXREF;
import com.myee.tarot.configuration.domain.UpdateConfigProductUsedXREF;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Project Name : tarot
 * User: Jelynn
 * Date: 2016/12/19
 * Time: 16:05
 * Describe:
 * Version:1.0
 */
@Repository
public class UpdateConfigProductUsedXREFDaoImpl extends GenericEntityDaoImpl<Long, UpdateConfigProductUsedXREF> implements UpdateConfigProductUsedXREFDao {

	@Autowired
	private ProductUsedDao productUsedDao;

	@Override
	public UpdateConfigProductUsedXREF getByTypeAndDeviceGroupNO(String type, String deviceGroupNO) {
		QUpdateConfigProductUsedXREF qUpdateConfigX = QUpdateConfigProductUsedXREF.updateConfigProductUsedXREF;
		JPQLQuery<UpdateConfigProductUsedXREF> query = new JPAQuery(getEntityManager());
		query.from(qUpdateConfigX);
		if (StringUtil.isNullOrEmpty(type) || StringUtil.isNullOrEmpty(deviceGroupNO)) {
			throw new IllegalArgumentException("type and deviceGroupNO must be not null");
		}
		query.where(qUpdateConfigX.type.eq(type));
		ProductUsed productUsed = productUsedDao.getByCode(deviceGroupNO);
		if(null == productUsed){
			return null;
		}
		query.where(qUpdateConfigX.productUsed.eq(productUsed));
		List<UpdateConfigProductUsedXREF> list = query.fetch();
		if(null == list || list.isEmpty() || list.size() > 1){
			return null; //TODO
		}
		return list.get(0);
	}

	@Override
	public List<UpdateConfigProductUsedXREF> listByTypeAndDeviceGroupNO(String type, String deviceGroupNO) {
		QUpdateConfigProductUsedXREF qUpdateConfigX = QUpdateConfigProductUsedXREF.updateConfigProductUsedXREF;
		JPQLQuery<UpdateConfigProductUsedXREF> query = new JPAQuery(getEntityManager());
		query.from(qUpdateConfigX);
		if (StringUtil.isNullOrEmpty(type) || StringUtil.isNullOrEmpty(deviceGroupNO)) {
			throw new IllegalArgumentException("type and deviceGroupNO must be not null");
		}
		query.where(qUpdateConfigX.type.eq(type));
		ProductUsed productUsed = productUsedDao.getByCode(deviceGroupNO);
		if(null == productUsed){
			return null;
		}
		query.where(qUpdateConfigX.productUsed.id.eq(productUsed.getId())).orderBy(qUpdateConfigX.id.desc());
		return query.fetch();
	}

	@Override
	public List<UpdateConfigProductUsedXREF> listByConfigId(Long configId) {
		QUpdateConfigProductUsedXREF qUpdateConfigX = QUpdateConfigProductUsedXREF.updateConfigProductUsedXREF;
		JPQLQuery<UpdateConfigProductUsedXREF> query = new JPAQuery(getEntityManager());
		query.from(qUpdateConfigX);
		query.where(qUpdateConfigX.updateConfig.id.eq(configId)).orderBy(qUpdateConfigX.productUsed.code.asc());
		return query.fetch();
	}

	@Override
	public void deleteByConfigAndDeviceGroupNO(UpdateConfig updateConfig, String deviceGroupNO) {
		QUpdateConfigProductUsedXREF qUpdateConfigX = QUpdateConfigProductUsedXREF.updateConfigProductUsedXREF;
		JPQLQueryFactory query = new JPAQueryFactory(getEntityManager());
		DeleteClause deleteClause = query.delete(qUpdateConfigX);
		if(updateConfig != null) {
			deleteClause.where(qUpdateConfigX.updateConfig.id.eq(updateConfig.getId()))
					.where(qUpdateConfigX.type.eq(updateConfig.getType()));
		}

		if(deviceGroupNO != null) {
			deleteClause.where(qUpdateConfigX.productUsed.code.eq(deviceGroupNO));
		}

		//两个参数都为空，什么都不执行
		if( updateConfig == null && deviceGroupNO == null ) {

		}
		else {
			deleteClause.execute();
		}
	}

	@Override
	public List<Long> listUpdateConfigIdsByProductUsedIds(List<java.lang.Long> productUsedIdList) {
		QUpdateConfigProductUsedXREF qUpdateConfigX = QUpdateConfigProductUsedXREF.updateConfigProductUsedXREF;
		JPQLQuery<Long> query = new JPAQuery(getEntityManager());
		query.select(qUpdateConfigX.updateConfig.id);
		query.from(qUpdateConfigX);
		query.where(qUpdateConfigX.productUsed.id.in(productUsedIdList));
		query.groupBy(qUpdateConfigX.updateConfig.id);
		query.orderBy(qUpdateConfigX.updateConfig.id.asc());
		return query.fetch();
	}
}
