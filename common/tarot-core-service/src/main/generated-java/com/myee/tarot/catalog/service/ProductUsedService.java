package com.myee.tarot.catalog.service;

import com.myee.tarot.catalog.domain.ProductUsed;
import com.myee.tarot.core.service.GenericEntityService;
import com.myee.tarot.core.util.PageRequest;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.core.util.WhereRequest;

import java.util.List;

/**
 * Created by Enva on 2016/6/1.
 */
public interface ProductUsedService extends GenericEntityService<Long, ProductUsed> {

    public PageResult<ProductUsed> pageList(PageRequest pageRequest);

    PageResult<ProductUsed> pageByStore(Long id, WhereRequest whereRequest);

    List<ProductUsed> listByIDs(List<Long> idList);

    ProductUsed getByCode(String code);

    void deleteWithAttr(ProductUsed productUsed);

    ProductUsed changeProductAndDeviceUsedStore(ProductUsed productUsed);
}
