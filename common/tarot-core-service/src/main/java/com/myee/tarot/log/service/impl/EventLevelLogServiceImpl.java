package com.myee.tarot.log.service.impl;

import com.myee.tarot.core.service.GenericEntityService;
import com.myee.tarot.core.service.GenericEntityServiceImpl;
import com.myee.tarot.log.dao.EventLevelLogDao;
import com.myee.tarot.log.domain.EventLevelLog;
import com.myee.tarot.log.service.EventLevelLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Ray.Fu on 2016/7/19.
 */
@Service
public class EventLevelLogServiceImpl extends GenericEntityServiceImpl<Long, EventLevelLog> implements EventLevelLogService {

    private EventLevelLogDao eventLevelLogDao;

    @Autowired
    public EventLevelLogServiceImpl(EventLevelLogDao eventLevelLogDao) {
        super(eventLevelLogDao);
        this.eventLevelLogDao = eventLevelLogDao;
    }

    @Override
    public List getEventLevelList() {
        return eventLevelLogDao.getEventLevelList();
    }
}
