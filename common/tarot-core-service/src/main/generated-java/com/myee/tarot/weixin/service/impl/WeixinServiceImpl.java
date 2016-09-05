package com.myee.tarot.weixin.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.myee.djinn.dto.*;
import com.myee.djinn.endpoint.OrchidService;
import com.myee.djinn.rpc.bootstrap.ServerBootstrap;
import com.myee.tarot.catering.dao.TableTypeDao;
import com.myee.tarot.catering.domain.TableType;
import com.myee.tarot.core.util.DateUtil;
import com.myee.tarot.merchant.dao.MerchantStoreDao;
import com.myee.tarot.merchant.domain.MerchantStore;
import com.myee.tarot.uitl.CacheUtil;
import com.myee.tarot.weixin.dao.WFeedBackDao;
import com.myee.tarot.weixin.dao.WxWaitTokenDao;
import com.myee.tarot.weixin.domain.WxWaitToken;
import com.myee.tarot.weixin.domain.WFeedBack;
import com.myee.tarot.weixin.service.WeixinService;
import org.apache.commons.codec.binary.Base64;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSet;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.cache.Cache;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.nio.charset.Charset;
import java.util.*;
import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;
import static org.apache.ignite.cache.CacheMode.PARTITIONED;
/**
 * Created by Martin on 2016/1/19.
 * 部分注释代码由于是微信端取号暂时不启用，故注释
 */
@Service
public class WeixinServiceImpl implements WeixinService {
    private static final Logger logger = LoggerFactory.getLogger(WeixinServiceImpl.class);

    @Autowired
    private TableTypeDao tableTypeDao;
    @Autowired
    private MerchantStoreDao merchantStoreDao;
    @Autowired
    private WxWaitTokenDao waitTokenDao;
    @Autowired
    private WFeedBackDao wFeedBackDao;
    @Autowired
    private ServerBootstrap serverBootstrap;

    private static final int DIVIDE_MILLSECOND = 1000; //new Date().getTime()要除以1000

    private static final int HOUR = 2;

    private static final int MINUTE = 30;

    private static final int SECOND = 0;

    private static final String STATE_WAITING = "排队中";

    private static final String STATE_DINNING = "就餐中";

    private static final String STATE_PASSED = "已过号";

    private static final String STATE_CANCELED = "已取消";

    private static final String STATE_SENT = "已发送";

    private static final String STATE_NOT_SENT = "未发送";

    @Autowired
    private Ignite ignite;

    /**
     * 查看品牌下的店铺
     *
     * @param merchantId
     * @return
     */
    @Override
    public Collection<ShopDetail> listByMerchantId(Long merchantId) {
//        IgniteCache<Long, ShopDetail> cacheShopDetail = CacheUtil.shopOfClient(ignite, merchantId);
//
//        Map<String, ShopDetail> storeMap = hgetall(redisKey, ShopDetail.class);
//        if (null == storeMap || storeMap.isEmpty()) {
//            List<MerchantStore> storeList = merchantStoreDao.listByMerchantId(merchantId);
//            for (MerchantStore store : storeList) {
//                List<TableType> typeList = tableTypeDao.listByMerchantStoreId(store.getMerchant().getId());
//                ShopDetail shopDetail = toDto(store);
//                shopDetail.setWaitList(toWaitList(typeList));
//                storeMap.put(String.valueOf(store.getId()), shopDetail);
//            }
//            hsetall(redisKey, storeMap);
//        }
//        return storeMap.values();
        return null;
    }

    private List<TableWait> toWaitList(List<TableType> typeList) {
        List<TableWait> waitList = Lists.transform(typeList, new Function<TableType, TableWait>() {
            @Override
            public TableWait apply(TableType tableType) {
                TableWait wait = new TableWait();
                wait.setMaxDiner(tableType.getCapacity());
                wait.setMinDiner(tableType.getMinimum());
                wait.setTableType(tableType.getName());
                wait.setTableTypeId(tableType.getId());
                return wait;
            }
        });
        return waitList;
    }

    @Override
    public ShopDetail getByMerchantStoreId(long clientId, long storeId) {
//        String redisKey = RedisKeys.shopOfClient(clientId);
//        String fieldKey = String.valueOf(storeId);
//        ShopDetail shopDetail = hget(redisKey, fieldKey, ShopDetail.class);
//        if (null == shopDetail) {
//            MerchantStore store = merchantStoreDao.findById(storeId);
//            if (null != store) {
//                shopDetail = toDto(store);
//                List<TableType> typeList = tableTypeDao.listByMerchantStoreId(store.getMerchant().getId());
//                shopDetail.setWaitList(toWaitList(typeList));
//
//                CollectionConfiguration setCfg = new CollectionConfiguration();
//                setCfg.setAtomicityMode(TRANSACTIONAL);
//                setCfg.setCacheMode(PARTITIONED);
//                IgniteSet<String> setTokenCache = CacheUtil.waitOfTableTypeSet(ignite, waitOfTableTypeKey, setCfg);
//
//                hset(redisKey, fieldKey, shopDetail, null);
//            }
//        }
//        return shopDetail;
        return null;
    }

    /**
     * 根据经纬度地理位置搜附近的餐厅
     *
     * @return
     */
    public Collection<ShopDetail> allOrgByGeo(Map map) {
        //通过map里的经纬度查询附近的餐厅
        return null;
    }

    /**
     * 根据选择的地点搜餐厅
     *
     * @param map
     * @return
     */
    public Collection<ShopDetail> allOrgByLocation(Map map) {
        if (map.get("city") != null && map.get("district") != null) {

        } else {
            //让其选择具体的城市
            return null;
        }
        return null;
    }

    @Override
    public WaitToken waitOfTableType(long tableTypeId, String token, Long shopId) {
//        String redisKey = RedisKeys.waitOfTableType(shopId, tableTypeId);
//        return hget(redisKey, token, WaitToken.class);
        return null;
    }

    @Override
    public Collection<WaitToken> allWaitOfTableType(long tableTypeId, Long shopId) {
//        String redisKey = RedisKeys.waitOfTableType(shopId, tableTypeId);
//        Map<String, WaitToken> waitTokenMap = hgetall(redisKey, WaitToken.class);
//        return waitTokenMap.values();
        return null;
    }

    @Override
    public WxWaitToken enqueue(long shopId, String mbNum, int dinerCount, String openId) {
//        try {
//            OrchidService eptService = null;
//            eptService = serverBootstrap.getClient(OrchidService.class, toClientUUID(shopId));
//            Diner diner = new Diner();
//            WaitToken waitToken = eptService.take(dinerCount, diner);
//            waitToken.setOpenId(openId);
//            waitToken.setWaitStatus(WaitTokenState.WAITING.getValue());
//            Date date = new Date();
//            waitToken.setTimeTook(date.getTime() / 1000);
//            //保存某品牌餐厅下某分店的对应餐桌类型ID的排号
//            String redisKey = RedisKeys.waitOfTableType(shopId, waitToken.getTableTypeId());
//            String redisKeyToOpenIdRef = RedisKeys.openIdToTableType(shopId, openId);
//            WxWaitToken wxWaitToken = convertTo(waitToken, date.getTime() / 1000);
//            WxWaitToken rwToken = waitTokenDao.update(wxWaitToken);
//            Date endDate = DateUtil.getNextDayOfDate(date, 2, 30, 0);
//            hsetSimple(redisKeyToOpenIdRef, redisKey, endDate);
//            //将identityCode和查询餐桌类型的排号放入Redis
//            hsetSimple(waitToken.getIdentityCode(), redisKey, endDate);
//            hset(redisKey.trim(), waitToken.getToken(), waitToken, endDate);
//            //将主键ID和查询餐桌类型的排号放入Redis
//            hsetSimple(rwToken.getId().toString(), redisKey, endDate);
//            hset(redisKey.trim(), rwToken.getId().toString(), waitToken, endDate);
//            //去查询进展
//            Map<String, Object> msgMap = listProgressByIdentityCode(waitToken.getIdentityCode());
//            int i = waitTokenDao.modifyWaitingInfo(Long.parseLong(msgMap.get("waitedTableCount").toString()), waitToken.getIdentityCode(), Long.valueOf(msgMap.get("timeTook").toString()), Long.parseLong(msgMap.get("predictWaitingTime").toString()), Long.valueOf(msgMap.get("tableTypeId").toString()));
//            if (i != 1) {
//                logger.error("修改等位状态失败!");
//            }
//            return rwToken;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

        return null;
    }

    private Ordering<WaitToken> orderingByTook = Ordering.from(new Comparator<WaitToken>() {
        @Override
        public int compare(WaitToken o1, WaitToken o2) {
            return o1.getTimeTook().compareTo(o2.getTimeTook());
        }
    });

    private Ordering<WxWaitToken> orderingByTook2 = Ordering.from(new Comparator<WxWaitToken>() {
        @Override
        public int compare(WxWaitToken o1, WxWaitToken o2) {
            Long o1TimeTook1 = new Long(o1.getTimeTook().getTime());
            Long o1TimeTook2 = new Long(o2.getTimeTook().getTime());
            return o1TimeTook1.compareTo(o1TimeTook2);
        }
    });

    static WxWaitToken convertTo(WaitToken waitToken, Long dateTimeLong) {
        WxWaitToken entity = new WxWaitToken();
        MerchantStore merchantStore = new MerchantStore();
        merchantStore.setId(waitToken.getShopId());
        entity.setTableId(waitToken.getTableId());
        entity.setTableTypeId(waitToken.getTableTypeId());
        entity.setComment(waitToken.getComment());
        entity.setChannelType("weixin");
//        entity.setDinerId(100L);
        entity.setStore(merchantStore);
        entity.setIdentityCode(waitToken.getIdentityCode());
        entity.setOpenId(waitToken.getOpenId());
        entity.setToken(waitToken.getToken());
        entity.setDinnerCount(waitToken.getDinnerCount());
        Date dt = new Date(dateTimeLong * DIVIDE_MILLSECOND);
        entity.setTimeTook(dt);
        entity.setState(waitToken.getWaitStatus());
        return entity;
    }

    @Override
    public void dequeue(long tableTypeId, String token, Long clientId, Long shopId) {
//        IgniteCache<String, WaitToken> cacheWaitToken = CacheUtil.waitOfTableType(ignite, tableTypeId);
//        cacheWaitToken.remove();
//        hdelete(redisKey, token);
    }

    @Override
    public DrawToken getByMerchantStoreId(long storeId, String token) {
//        String redisKey = RedisKeys.drawOfStore(storeId);
//        return hget(redisKey, token, DrawToken.class);
        return null;
    }

    /**
     * 根据openId和clientID和orgId查询状态
     *
     * @param openId
     * @param orgID
     * @return
     */
    @Override
    public Map<String, Object> listProgressByOpenId(String openId, Long orgID, Long tableTypeId) {
        IgniteCache<String, String> openIdToStoreCache = CacheUtil.openidToStore(ignite, orgID);
        String waitOfTableTypeKey = openIdToStoreCache.get(openId);
        Set<String> setWaitTokenKey = Sets.newHashSet();
        IgniteCache<String, WaitToken> waitTokenTypeIdCache = CacheUtil.getCache(ignite, waitOfTableTypeKey);
        CollectionConfiguration setCfg = new CollectionConfiguration();
        setCfg.setAtomicityMode(TRANSACTIONAL);
        setCfg.setCacheMode(PARTITIONED);
        IgniteSet<String> setTokenCache = CacheUtil.waitOfTableTypeSet(ignite, waitOfTableTypeKey, setCfg);
        Map<String, WaitToken> waitTokenMap = waitTokenTypeIdCache.getAll(setTokenCache);
        List<WaitToken> waitTokenList = orderingByTook.sortedCopy(waitTokenMap.values());
        Set<Integer> waitNumSet = Sets.newHashSet();
        Map<String, Object> latestDevInfo = Maps.newHashMap();
        //顾客取号的号数
        int userNum = 0;
        //之前等待桌数
        int beforeCount = 0;
        String tokenNum = null;
        MerchantStore merchantStore = merchantStoreDao.findById(orgID);
        String shopName = merchantStore.getName();
        String queueStatus = null;
        Date date = new Date();
        for (WaitToken waitToken : waitTokenList) {
            waitNumSet.add(Integer.parseInt(waitToken.getToken().substring(1, 3)));
            if (openId.equals(waitToken.getOpenId())) {
                tokenNum = waitToken.getToken();
                userNum = Integer.parseInt(waitToken.getToken().substring(1, 3));
                queueStatus = assignVal(waitToken.getWaitStatus());
            }
        }
        for (int i : waitNumSet) {
            if (i < userNum) {
                beforeCount++;
            }
        }
        codeBlock(latestDevInfo, shopName, tokenNum, beforeCount, queueStatus, date);
        return latestDevInfo;
    }

    private void codeBlock(Map<String, Object> latestDevInfo, String shopName, String tokenNum, int beforeCount, String queueStatus, Date date) {
        latestDevInfo.put("shopName", shopName);
        latestDevInfo.put("tokenNum", tokenNum);
        latestDevInfo.put("waitedTable", beforeCount + "桌");
        latestDevInfo.put("predictTime", String.valueOf(beforeCount * 30) + "分钟");
        latestDevInfo.put("queueStatus", queueStatus);
        latestDevInfo.put("queryTime", DateUtil.formatDateTime(date));
    }

    private String assignVal(int status) {
        String str = "";
        if (status == 1) {
            str = STATE_WAITING;
        } else if (status == 2) {
            str = STATE_DINNING;
        } else if (status == 3) {
            str = STATE_PASSED;
        } else if (status == 4) {
            str = STATE_CANCELED;
        } else if (status == 6) {
            str = STATE_SENT;
        } else
            str = STATE_NOT_SENT;
        return str;
    }

    /**
     * 拿identityCode去数据库里查找
     *
     * @param identityCode
     * @return
     */
    @Override
    public Map<String, Object> listProgressByIdentityCode(String identityCode) {
        //先到Redis里去找，通过identityCode获取当时保存token的redisKey
        IgniteCache<String, String> identityCodeWaitTokenTypeKeyCache = CacheUtil.identityCodeWaitTokenType(ignite);
        String waitTokenTypeKey = identityCodeWaitTokenTypeKeyCache.get(identityCode);
        Date date = new Date();
        //排号
        String tokenNum = null;
        //店铺名称
        String shopName = null;
        Map<String, Object> infoStr = Maps.newHashMap();
        //前面还有多少桌
        int index = 0;
        String queueStatus = null;
        long timeTook = 0;
        if (waitTokenTypeKey != null) {
            IgniteCache<String, WaitToken> waitTokenTypeIdCache = CacheUtil.getCache(ignite, waitTokenTypeKey);
            CollectionConfiguration setCfg = new CollectionConfiguration();
            setCfg.setAtomicityMode(TRANSACTIONAL);
            setCfg.setCacheMode(PARTITIONED);
            IgniteSet<String> setTokenCache = CacheUtil.waitOfTableTypeSet(ignite, waitTokenTypeKey, setCfg);
            Map<String, WaitToken> waitTokenMap = waitTokenTypeIdCache.getAll(setTokenCache);
            List<WaitToken> waitTokenList = orderingByTook.sortedCopy(waitTokenMap.values());

            //排序waiTokenMap
            for(WaitToken waitToken : waitTokenList) {
                if (!waitToken.getIdentityCode().equals(identityCode)) {
                    index++;
                } else {
                    //根据identityCode查询merchantStoreId
                    Date startDate = DateUtil.getZeroClockOfDate(date);
                    Date endDate = DateUtil.getNextDayOfDate(date, HOUR, MINUTE, SECOND);
                    Long bTimeLong = startDate.getTime();
                    Long eTimeLong = endDate.getTime();
                    WxWaitToken wxWaitToken = waitTokenDao.getByIdentityCode(identityCode, bTimeLong, eTimeLong);
                    //根据merchanStoreId查询merchanStoreName
                    MerchantStore merchantStore = merchantStoreDao.findById(wxWaitToken.getStore().getId());
                    shopName = merchantStore.getName();
                    tokenNum = waitToken.getToken();
                    timeTook = waitToken.getTimeTook();
                    queueStatus = assignVal(waitToken.getWaitStatus());
                    infoStr.put("tableTypeId", wxWaitToken.getTableTypeId());
                    break;
                }
            }
        } else {
            Date startDate = DateUtil.getZeroClockOfDate(date);
            Date endDate = DateUtil.getNextDayOfDate(date, HOUR, MINUTE, SECOND);
            Long bTimeLong = startDate.getTime();
            Long eTimeLong = endDate.getTime();
            //再到MYSQL去找,用identityCode找到对应的clientId,orgId,和token，
            WxWaitToken wtoken = waitTokenDao.getByIdentityCode(identityCode, bTimeLong, eTimeLong);
            if (wtoken != null) {
                //根据clientId和orgId和tableId，找到该餐馆的某餐桌类型等待的token
                List<WxWaitToken> tokenList = waitTokenDao.listByConditions(wtoken.getStore().getId(), wtoken.getTableId(), WaitTokenState.WAITING.getValue());
                List<WxWaitToken> sortedTokens = orderingByTook2.sortedCopy(tokenList);
                for (WxWaitToken wt : sortedTokens) {
                    if (!wt.getIdentityCode().equals(identityCode)) {
                        index++;
                    } else {
                        queueStatus = assignVal(wt.getState());
                        timeTook = wt.getTimeTook().getTime();
                        //根据merchanStoreId查询merchanStoreName
                        MerchantStore merchantStore = merchantStoreDao.findById(wt.getStore().getId());
                        infoStr.put("tableTypeId", wt.getTableTypeId());
                        shopName = merchantStore.getName();
                        break;
                    }
                }
            } else {
                codeBlock(infoStr, shopName, tokenNum, index, queueStatus, date);
                infoStr.put("waitedTableCount", index);
                infoStr.put("predictWaitingTime", Long.valueOf(index * 10));
                infoStr.put("timeTook", timeTook);
                return infoStr;
            }
        }
        codeBlock(infoStr, shopName, tokenNum, index, queueStatus, date);
        infoStr.put("waitedTableCount", index);
        infoStr.put("predictWaitingTime", Long.valueOf(index * 10));
        infoStr.put("timeTook", timeTook);
        return infoStr;
    }

    @Override
    public WxWaitToken getByOpenId(String openId, Integer state) {
        WxWaitToken wxWaitToken = waitTokenDao.getByCondition(openId, state);
        return wxWaitToken;
    }

    final static Charset charset = Charset.forName("UTF8");

    static String toClientUUID(long shopId) {
        String rawId = String.format("shopId:%08d", shopId);
        return Base64.encodeBase64String(rawId.getBytes(charset));
    }

    /**
     * 绑定二维码
     *
     * @param identityCode
     */
    @Override
    public boolean bondQrCodeByScan(String identityCode, String openId) {
        //先到Redis里去找，通过identityCode获取当时保存token的redisKey
        Cache<String, String> identityCodeWaitTokenTypeKeyCache = CacheUtil.identityCodeWaitTokenType(ignite);
        String waitTokenTypeKey = identityCodeWaitTokenTypeKeyCache.get(identityCode);
        Date date = new Date();
        Set<String> setWaitTokenKey = Sets.newHashSet();
        if (waitTokenTypeKey != null) {
            IgniteCache<String, WaitToken> waitTokenTypeIdCache = CacheUtil.getCache(ignite, waitTokenTypeKey);
            CollectionConfiguration setCfg = new CollectionConfiguration();
            setCfg.setAtomicityMode(TRANSACTIONAL);
            setCfg.setCacheMode(PARTITIONED);
            IgniteSet<String> setTokenCache = CacheUtil.waitOfTableTypeSet(ignite, waitTokenTypeKey, setCfg);
            Map<String, WaitToken> waitTokenMap = waitTokenTypeIdCache.getAll(setTokenCache);
            List<WaitToken> waitTokenList = orderingByTook.sortedCopy(waitTokenMap.values());
            //按WaitToken来取
            for (WaitToken waitToken : waitTokenList) {
                if (waitToken.getIdentityCode().equals(identityCode)) {
                    //删掉原来Redis中对应的排号数据，为了修改将openid绑定进去
                    waitTokenTypeIdCache.remove(waitToken.getToken());
                    waitToken.setOpenId(openId);
                    Date endDate = DateUtil.getNextDayOfDate(date, HOUR, MINUTE, HOUR);
                    waitTokenTypeIdCache.put(waitToken.getToken(), waitToken);
                    waitTokenTypeIdCache.withExpiryPolicy(new AccessedExpiryPolicy(new Duration(date.getTime() / DIVIDE_MILLSECOND, endDate.getTime() / DIVIDE_MILLSECOND)));
                    Long timeTookLong = waitToken.getTimeTook();
                    waitTokenDao.updateWaitTokenOpenId(openId, waitToken.getIdentityCode(), timeTookLong);
                    //将openId跟店铺id作为key，绑定到之前的排号list的key中
                    IgniteCache<String, String> openIdToStoreCache = CacheUtil.openidToStore(ignite, waitToken.getShopId());
                    openIdToStoreCache.put(openId, waitTokenTypeKey);
                    openIdToStoreCache.withExpiryPolicy(new AccessedExpiryPolicy(new Duration(date.getTime() / DIVIDE_MILLSECOND, endDate.getTime() / DIVIDE_MILLSECOND)));
                    break;
                }
            }
            return true;
        } else { //如果Redis里找不到，就到MySql里通过当前时间的范围，按identityCode搜索
            Date startDate = DateUtil.getZeroClockOfDate(date);
            Date endDate = DateUtil.getNextDayOfDate(date, HOUR, MINUTE, SECOND);
            Long bTimeLong = startDate.getTime() / DIVIDE_MILLSECOND;
            Long eTimeLong = endDate.getTime() / DIVIDE_MILLSECOND;
            WxWaitToken rw = waitTokenDao.getByIdentityCode(identityCode, bTimeLong, eTimeLong);
            if (rw != null) {
                rw.setOpenId(openId);
                Long timeTookLong = rw.getTimeTook().getTime() / DIVIDE_MILLSECOND;
                waitTokenDao.updateWaitTokenOpenId(rw.getOpenId(), rw.getIdentityCode(), timeTookLong);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public List<WxWaitToken> listByOpenId(String openId, Date startDate, Date endDate) {
        List<WxWaitToken> list = waitTokenDao.selectWait(openId, startDate.getTime() / DIVIDE_MILLSECOND, endDate.getTime() / DIVIDE_MILLSECOND);
        return list;
    }

    @Override
    public int modifyWaitingInfo(Long waitedCount, String identityCode, Long date, Long predictWaitingTime, Long tableTypeId) {
        int i = waitTokenDao.modifyWaitingInfo(waitedCount, identityCode, date, predictWaitingTime, tableTypeId);
        return i;
    }

    @Override
    public String hGetWaitTokenRedisKey(Long waitTokenId) {
//        String key = getSimple(waitTokenId.toString()).toString();
//        return key;
        return null;
    }

    @Override
    public void hDelWaitTokenRedisRecord(Long waitTokenId) {
//        hdeleteSimple(waitTokenId.toString());
    }

    @Override
    public int modifyWaitingStatus(int statusValue, long waitQueueId) {
        int i = waitTokenDao.modifyWaitingStatus(statusValue, waitQueueId);
        return i;
    }

    /**
     * 根据商店的经纬
     *
     * @param storeLongitude
     * @param storeLatitude
     * @param longitude
     * @param latitude
     * @return
     */
    @Override
    public Double calcDistance(Double storeLongitude, Double storeLatitude, Double longitude, Double latitude) {
        Double distance = LantitudeLongitudeDist(storeLongitude, storeLatitude, longitude, latitude);
        return distance;
    }

    /**
     * 发送意见
     *
     * @param context
     * @return
     */
    @Override
    public void sendFeedback(String context, String openId) {
        WFeedBack fb = new WFeedBack();
        fb.setFeedBackOpenId(openId);
        fb.setFeedBackContext(context);
        wFeedBackDao.update(fb);
    }

    private static final double EARTH_RADIUS = 6378137;//赤道半径(单位m)

    /**
     * 转化为弧度(rad)
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 基于余弦定理求两经纬度距离
     *
     * @param lon1 第一点的精度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的精度
     * @param lat2 第二点的纬度
     * @return 返回的距离，单位km
     */
    public static double LantitudeLongitudeDist(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);

        double radLon1 = rad(lon1);
        double radLon2 = rad(lon2);

        if (radLat1 < 0)
            radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
        if (radLat1 > 0)
            radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
        if (radLon1 < 0)
            radLon1 = Math.PI * 2 - Math.abs(radLon1);// west
        if (radLat2 < 0)
            radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
        if (radLat2 > 0)
            radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
        if (radLon2 < 0)
            radLon2 = Math.PI * 2 - Math.abs(radLon2);// west
        double x1 = EARTH_RADIUS * Math.cos(radLon1) * Math.sin(radLat1);
        double y1 = EARTH_RADIUS * Math.sin(radLon1) * Math.sin(radLat1);
        double z1 = EARTH_RADIUS * Math.cos(radLat1);

        double x2 = EARTH_RADIUS * Math.cos(radLon2) * Math.sin(radLat2);
        double y2 = EARTH_RADIUS * Math.sin(radLon2) * Math.sin(radLat2);
        double z2 = EARTH_RADIUS * Math.cos(radLat2);

        double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
        //余弦定理求夹角
        double theta = Math.acos((EARTH_RADIUS * EARTH_RADIUS + EARTH_RADIUS * EARTH_RADIUS - d * d) / (2 * EARTH_RADIUS * EARTH_RADIUS));
        double dist = theta * EARTH_RADIUS;
        return dist;
    }

    public ShopDetail toDto(MerchantStore merchantStore) {
        ShopDetail dto = new ShopDetail();
        dto.setClientId(merchantStore.getMerchant().getId());
        dto.setClientName(merchantStore.getMerchant().getName());
        dto.setShopId(merchantStore.getId());
        dto.setShopName(merchantStore.getName());
        dto.setScore(merchantStore.getScore());
        dto.setShopType(merchantStore.getStoreType());
        dto.setAddress(merchantStore.getAddress().getAddress());
        dto.setOpeningTime(toHourString(merchantStore.getTimeOpen()) + "~" + toHourString(merchantStore.getTimeClose()));
        return dto;
    }

    String toHourString(Date time) {
        return new DateTime(time.getTime()).toString("HH:mm");
    }
}
