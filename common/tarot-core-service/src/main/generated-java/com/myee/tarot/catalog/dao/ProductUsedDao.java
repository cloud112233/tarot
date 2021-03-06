package com.myee.tarot.catalog.dao;

import com.myee.tarot.admin.domain.AdminUser;
import com.myee.tarot.catalog.domain.ProductUsed;
import com.myee.tarot.core.dao.GenericEntityDao;
import com.myee.tarot.core.util.PageRequest;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.core.util.WhereRequest;

import java.util.List;
import java.util.Set;

/**
 * Created by Enva on 2016/6/1.
 */
public interface ProductUsedDao extends GenericEntityDao<Long, ProductUsed> {

    PageResult<ProductUsed> pageList(PageRequest pageRequest);

    PageResult<ProductUsed> pageByStore(Long id, WhereRequest whereRequest);

    List<ProductUsed> listByIDs(List<Long>idList);

    ProductUsed getByCode(String code);

    List<Long> listIDsLikeCode(String code);
}
