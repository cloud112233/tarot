package com.myee.tarot.catalog.service.impl;

import com.myee.tarot.catalog.domain.DeviceUsed;
import com.myee.tarot.catalog.domain.ProductUsed;
import com.myee.tarot.catalog.service.DeviceUsedService;
import com.myee.tarot.catalog.service.ProductUsedAttributeService;
import com.myee.tarot.core.service.GenericEntityServiceImpl;
import com.myee.tarot.core.util.PageRequest;
import com.myee.tarot.core.util.PageResult;
import com.myee.tarot.catalog.dao.ProductUsedDao;
import com.myee.tarot.catalog.service.ProductUsedService;
import com.myee.tarot.core.util.WhereRequest;
import com.myee.tarot.merchant.domain.MerchantStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Enva on 2016/6/1.
 */
@Service
public class ProductUsedServiceImpl extends GenericEntityServiceImpl<Long, ProductUsed> implements ProductUsedService {

    protected ProductUsedDao productUsedDao;
    @Autowired
    private ProductUsedAttributeService productUsedAttributeService;
    @Autowired
    private DeviceUsedService deviceUsedService;
    @Autowired
    public ProductUsedServiceImpl(ProductUsedDao productUsedDao) {
        super(productUsedDao);
        this.productUsedDao = productUsedDao;
    }

    @Override
    public PageResult<ProductUsed> pageList(PageRequest pageRequest){
        return productUsedDao.pageList(pageRequest);
    }

    @Override
    public PageResult<ProductUsed> pageByStore(Long id, WhereRequest whereRequest){
        return productUsedDao.pageByStore(id, whereRequest);
    }

    @Override
    public List<ProductUsed> listByIDs(List<Long> idList){
        return productUsedDao.listByIDs(idList);
    }

    @Override
    public ProductUsed getByCode(String code){
        return productUsedDao.getByCode( code);
    }

    @Override
    public void deleteWithAttr(ProductUsed productUsed) {
        productUsedAttributeService.deleteByProductUsedId(productUsed.getId());
        productUsedDao.delete(productUsed);
    }

    @Override
    public ProductUsed changeProductAndDeviceUsedStore(ProductUsed productUsed) {
        List<DeviceUsed> deviceUsedList = productUsed.getDeviceUsed();
        if(deviceUsedList != null && deviceUsedList.size() > 0) {
            MerchantStore merchantStore = productUsed.getStore();
            for( DeviceUsed deviceUsed : deviceUsedList ) {
                deviceUsed.setStore(merchantStore);
                deviceUsedService.update(deviceUsed);
            }
        }

        return productUsedDao.update(productUsed);
    }

}
