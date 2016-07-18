package com.myee.tarot.web.admin.controller.pricedraw;

import com.myee.tarot.core.exception.ServiceException;
import com.myee.tarot.core.util.ajax.AjaxResponse;
import com.myee.tarot.pricedraw.domain.MerchantPrice;
import com.myee.tarot.pricedraw.service.MerchantPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2016/7/11.
 */
@Controller
public class MerchantPriceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantPrice.class);

    @Autowired
    private MerchantPriceService merchantPriceService;

    /**
     * 添加个奖项
     * @param merchantPrice
     * @return
     */
    @RequestMapping(value = "price/saveOrUpdate",method = RequestMethod.POST)
    @ResponseBody
    private AjaxResponse priceSave(@RequestBody MerchantPrice merchantPrice){
        try {
            AjaxResponse resp = new AjaxResponse();
            MerchantPrice price = merchantPriceService.update(merchantPrice);
            resp.setStatus(AjaxResponse.RESPONSE_STATUS_SUCCESS);
            resp.addEntry("result",price);
            return AjaxResponse.success();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return AjaxResponse.failed(-1);
    }

    /**
     * 删除 奖项
     * @param id
     * @return
     */
    @RequestMapping(value = "price/deleteById",method = RequestMethod.POST)
    @ResponseBody
    private AjaxResponse priceDelete(@RequestParam("id") Long id){
        try {
            MerchantPrice price = merchantPriceService.findById(id);
            if(price!=null){
                merchantPriceService.delete(price);
                return AjaxResponse.success();
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return AjaxResponse.failed(-1);
    }


}
