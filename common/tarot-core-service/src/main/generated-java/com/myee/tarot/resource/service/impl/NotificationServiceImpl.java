package com.myee.tarot.resource.service.impl;

import com.myee.tarot.core.service.GenericEntityServiceImpl;
import com.myee.tarot.resource.dao.NotificationDao;
import com.myee.tarot.resource.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Ray.Fu on 2016/8/10.
 */
@Service
public class NotificationServiceImpl extends GenericEntityServiceImpl<Long, com.myee.tarot.resource.domain.Notification> implements NotificationService {

    protected NotificationDao notificationDao;

    @Autowired
    public NotificationServiceImpl(NotificationDao notificationDao) {
        super(notificationDao);
        this.notificationDao = notificationDao;
    }
}