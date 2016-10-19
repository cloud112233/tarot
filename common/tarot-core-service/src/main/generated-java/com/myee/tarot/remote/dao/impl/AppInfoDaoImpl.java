package com.myee.tarot.remote.dao.impl;

import com.myee.tarot.core.dao.GenericEntityDaoImpl;
import com.myee.tarot.metric.domain.AppInfo;
import com.myee.tarot.remote.dao.AppInfoDao;
import org.springframework.stereotype.Repository;

/**
 * Created by Chay on 2016/8/10.
 */
@Repository
public class AppInfoDaoImpl extends GenericEntityDaoImpl<Long, AppInfo> implements AppInfoDao {
}