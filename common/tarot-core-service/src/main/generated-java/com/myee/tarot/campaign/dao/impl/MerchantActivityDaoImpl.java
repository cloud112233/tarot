package com.myee.tarot.campaign.dao.impl;

import com.myee.tarot.campaign.dao.MerchantActivityDao;
import com.myee.tarot.core.Constants;
import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.campaign.domain.MerchantActivity;
import com.myee.tarot.campaign.domain.QMerchantActivity;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2016/7/11.
 */
@Repository
public class MerchantActivityDaoImpl extends GenericEntityDaoImpl<Long, MerchantActivity> implements MerchantActivityDao {
    @Override
    public MerchantActivity findStoreActivity(Long storeId) {
        QMerchantActivity qMerchantActivity = QMerchantActivity.merchantActivity;
        JPQLQuery<MerchantActivity> query = new JPAQuery(getEntityManager());
        List<MerchantActivity> result = query.from(qMerchantActivity).where(qMerchantActivity.store.id.eq(storeId).and(qMerchantActivity.deleteStatus.eq(Constants.DELETE_NO))).fetch();
        return result != null && result.size() > 0 ? result.get(0) : null;
    }

    @Override
    public List<MerchantActivity> findActiveActivity() {
        QMerchantActivity qMerchantActivity = QMerchantActivity.merchantActivity;
        JPQLQuery<MerchantActivity> query = new JPAQuery(getEntityManager());
        List<MerchantActivity> result = query.from(qMerchantActivity).where(qMerchantActivity.deleteStatus.eq(Constants.DELETE_NO).and(qMerchantActivity.activityStatus.ne(Constants.ACITIVITY_START))).fetch();
        return result;
    }


}
