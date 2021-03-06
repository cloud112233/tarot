package com.myee.tarot.web.configuration.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.myee.djinn.dto.NoticeType;
import com.myee.djinn.endpoint.TrunkingInterface;
import com.myee.djinn.rpc.bootstrap.ServerBootstrap;
import com.myee.tarot.catalog.domain.DeviceUsed;
import com.myee.tarot.catalog.domain.ProductUsed;
import com.myee.tarot.catalog.service.ProductUsedService;
import com.myee.tarot.configuration.domain.BranchConfig;
import com.myee.tarot.configuration.service.BranchConfigService;
import com.myee.tarot.core.Constants;
import com.myee.tarot.core.util.*;
import com.myee.tarot.core.util.FileUtils;
import com.myee.tarot.core.util.ajax.AjaxPageableResponse;
import com.myee.tarot.core.util.ajax.AjaxResponse;
import com.myee.tarot.configuration.domain.UpdateConfig;
import com.myee.tarot.configuration.domain.UpdateConfigProductUsedXREF;
import com.myee.tarot.configuration.service.UpdateConfigProductUsedXREFService;
import com.myee.tarot.configuration.service.UpdateConfigService;
import com.myee.tarot.resource.type.UpdateConfigSeeType;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Chay on 2016/12/15.
 */
@Controller
public class UpdateConfigController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateConfigController.class);

    @Value("${cleverm.push.dirs}")
    private String DOWNLOAD_HOME;
    @Value("${cleverm.push.http}")
    private String DOWNLOAD_HTTP;
    @Autowired
    private UpdateConfigService updateConfigService;
    @Autowired
    private BranchConfigService branchConfigService;
    @Autowired
    private UpdateConfigProductUsedXREFService updateConfigProductUsedXREFService;
    @Autowired
    private ProductUsedService productUsedService;
    @Autowired
    private ServerBootstrap serverBootstrap;

    @RequestMapping(value = {"admin/updateConfig/update"}, method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasAuthority('configuration_update_u')")
    public AjaxResponse addUpdateConfig(@Valid @RequestBody UpdateConfig updateConfig, HttpServletRequest request) throws Exception {
        AjaxResponse resp;
        try {
            String type = updateConfig.getType();
            String typeName = com.myee.djinn.dto.NoticeType.getValue(type);
            if( type == null || StringUtil.isBlank(type) || typeName == null
                    || StringUtil.isBlank(typeName)
                    || Constants.UPDATE_TYPE_EXCEPT_LIST.contains(type)) {
                return AjaxResponse.failed(-1,"参数错误");
            }

            String seeType = updateConfig.getSeeType();
            String seeTypeName = new UpdateConfigSeeType().getUpdateConfigSeeTypeName(seeType);
            //如果设备可见类型没设置，默认所有设备不可见
            if( seeType == null || StringUtil.isBlank(seeType)
                    || seeTypeName == null || StringUtil.isBlank(seeTypeName)) {
                updateConfig.setSeeType(Constants.UPDATE_SEE_TYPE_NONE);
            }

            updateConfig = updateConfigService.update(updateConfig);

            resp = AjaxResponse.success();
            resp.addEntry(Constants.RESPONSE_UPDATE_RESULT, objectToEntry(updateConfig));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"出错");
        }
        return resp;
    }

//    @RequestMapping(value = {"admin/updateConfig/delete"}, method = RequestMethod.POST)
//    @ResponseBody
//    public AjaxResponse delUpdateConfig(@RequestBody TableType type, HttpServletRequest request) throws Exception {
//        AjaxResponse resp;
//        try {
//            return AjaxResponse.success();
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(),e);
//            return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"出错");
//        }
//    }

    @RequestMapping(value = {"admin/updateConfig/bindProductUsed"}, method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasAuthority('configuration_update_u')")
    public AjaxResponse deviceUsedBindProductUsed(@RequestParam(value = "bindString") String bindString, @RequestParam(value = "configId") Long configId, HttpServletRequest request) {
        try {
            AjaxResponse resp;
            List<Long> bindList = JSON.parseArray(bindString, Long.class);
            UpdateConfig updateConfig = updateConfigService.findById(configId);
            if (updateConfig == null) {
                return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"配置项不存在");
            }
            //只有bindList有数据，才去查询绑定设备组
            List<ProductUsed> productUsedList = null;
            if( bindList != null && bindList.size() > 0 ) {
                productUsedList = productUsedService.listByIDs(bindList);

                if(productUsedList == null || productUsedList.size() == 0) {
                    return AjaxResponse.failed(-1, "设备组参数无效");
                }
                updateConfig.setProductUsed(productUsedList);
            }

            updateConfig.setDeviceGroupNOList(bindString);
            updateConfig = updateConfigService.update(updateConfig);

            //手动更新关联关系表
            UpdateConfigProductUsedXREF updateConfigProductUsedXREF = null;
            String type = updateConfig.getType();
            //先删除该配置关联设备组的关系
            updateConfigProductUsedXREFService.deleteByConfigAndDeviceGroupNO(updateConfig,null);
            if( productUsedList != null && productUsedList.size() > 0 ) {
                for (ProductUsed productUsed : productUsedList) {
                    String deviceGroupNO = productUsed.getCode();
                    UpdateConfigProductUsedXREF updateConfigProductUsedXREF_DB = null;
                    //同一类型下同一设备的绑定关系只能有一条记录，自研平板类型除外，自研平板一个设备可绑定多个版本。但同版本的记录只能有一个。
                    if( !type.equals(Constants.UPDATE_TYPE_SELF_DESIGN_PAD) ){
                        updateConfigProductUsedXREF_DB = updateConfigProductUsedXREFService.getByTypeAndDeviceGroupNO(type,deviceGroupNO);
                    }
                    //非自研平板直接更新数据库关系数据
                    if(updateConfigProductUsedXREF_DB != null) {
//                        updateConfigProductUsedXREFService.deleteByConfigAndDeviceGroupNO(updateConfig,deviceGroupNO);
                        updateConfigProductUsedXREF = updateConfigProductUsedXREF_DB;
                    }
                    //自研平板则新建关系
                    else {
                        updateConfigProductUsedXREF = new UpdateConfigProductUsedXREF();
                    }
                    updateConfigProductUsedXREF.setProductUsed(productUsed);
                    updateConfigProductUsedXREF.setUpdateConfig(updateConfig);
                    updateConfigProductUsedXREF.setType(type);
                    updateConfigProductUsedXREFService.update(updateConfigProductUsedXREF);
                }
            }

            resp = AjaxResponse.success();
            resp.addEntry(Constants.RESPONSE_UPDATE_RESULT, objectToEntry(updateConfig));
            return resp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return AjaxResponse.failed(-1, "失败");
        }

    }

    @RequestMapping(value = {"admin/updateConfig/paging"}, method = RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("hasAuthority('configuration_update_r')")
    public AjaxPageableResponse pageUpdateConfig(Model model, HttpServletRequest request, WhereRequest pageRequest) {
        AjaxPageableResponse resp = new AjaxPageableResponse();
        try {
            PageResult<UpdateConfig> pageList = updateConfigService.page(pageRequest);
            List<UpdateConfig> updateConfigList = pageList.getList();
            for (UpdateConfig updateConfig : updateConfigList) {
                resp.addDataEntry(objectToEntry(updateConfig));
            }
            resp.setRecordsTotal(pageList.getRecordsTotal());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return AjaxPageableResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"出错");
        }
        return resp;
    }

    @RequestMapping(value = {"admin/updateConfig/listType"}, method = RequestMethod.GET)
    @ResponseBody
    public List listType(Model model, HttpServletRequest request, WhereRequest pageRequest) {
        try {
            List<Map> resp = new ArrayList();
            NoticeType[] noticeTypes = com.myee.djinn.dto.NoticeType.values();
            for( com.myee.djinn.dto.NoticeType noticeType : noticeTypes ) {
                String key = noticeType.getCaption();
                //是排除列表的类型我们不使用
                if( Constants.UPDATE_TYPE_EXCEPT_LIST.contains(key) ) {
                    continue;
                }
                Map entry = new HashMap();
                entry.put("name", noticeType.getValue());
                entry.put("value", key);//跟前端select刚好是反的
                resp.add(entry);
            }
            return resp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @RequestMapping(value = {"admin/updateConfig/listSeeType"}, method = RequestMethod.GET)
    @ResponseBody
    public List listSeeType(Model model, HttpServletRequest request, WhereRequest pageRequest) {
        try {
            return new UpdateConfigSeeType().getUpdateConfigSeeType4Select();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @RequestMapping(value = {"admin/updateConfig/getProductUsedInfo"}, method = RequestMethod.GET)
    @ResponseBody
    public AjaxResponse getProductUsedInfo(@RequestParam(value = "productUsedId") Long productUsedId, HttpServletRequest request, WhereRequest whereRequest) {
        try {
            AjaxResponse resp = new AjaxResponse();

            ConcurrentMap<String, Channel> map = serverBootstrap.getAllChannels();
            if ( null == productUsedId || "".equals(productUsedId) ) {
                return AjaxResponse.failed(-1,"参数错误");
            }
            ProductUsed productUsed = productUsedService.findById(productUsedId);
            if(productUsed == null) {
                return AjaxResponse.failed(-1,"设备组不存在");
            }

            List<DeviceUsed> deviceUsedList = productUsed.getDeviceUsed();
            if( deviceUsedList == null || deviceUsedList.size() == 0 ) {
                return AjaxResponse.failed(-1,"该设备组未关联设备");
            }

//            Map result = new HashMap();
            String boardNo = null;
            String returnKey = null;
            String version = null;
            TrunkingInterface endpointInterface = null;
            for(DeviceUsed deviceUsed : deviceUsedList) {
                Map entry = new HashMap();
                boardNo = deviceUsed.getBoardNo();
                entry.put(Constants.RESPONSE_DEVICE_USED,objectToEntry(deviceUsed));


                /**测试代码开始------------------------------------------------------
                 * 测试用代码，为了测试前端页面当version为空数组时显示是否正常
                 */
                /*entry.put(Constants.RESPONSE_VERSION, new JSONArray());
                entry.put(Constants.RESPONSE_CODE,0);
                entry.put(Constants.RESPONSE_MESSAGE,"查询成功");
                resp.addDataEntry(entry);
                continue;*/
                //测试代码结束------------------------------------------------------

                if(!map.containsKey(boardNo)) {
                    entry.put(Constants.RESPONSE_CODE,-1);
                    entry.put(Constants.RESPONSE_MESSAGE,Constants.MESSAGE_DEVICE+Constants.MESSAGE_OFFLINE);
                    resp.addDataEntry(entry);
                    continue;
                }

                try {
                    endpointInterface = serverBootstrap.getClient(TrunkingInterface.class, boardNo);
                    version = endpointInterface.lastVersion();
                    JSONArray jsonArray = JSON.parseArray(version);
                    String type = "";
                    String typeName = "";
                    JSONArray jsonArrayResult = new JSONArray();
                    if( jsonArray != null && jsonArray.size() > 0 ) {
                        for( Object obj : jsonArray ) {
                            JSONObject jsonObject = JSON.parseObject(obj.toString());
                            type = jsonObject.getString(Constants.SEARCH_OPTION_TYPE);
                            typeName =  (type == null || "".equals(type)) ? "" : com.myee.djinn.dto.NoticeType.getValue(type);
                            jsonObject.put(Constants.SEARCH_OPTION_TYPE, typeName);
                            jsonArrayResult.add(jsonObject);
                        }
                    }

                    entry.put(Constants.RESPONSE_VERSION, jsonArrayResult);
                } catch (Exception e) {
                    entry.put(Constants.RESPONSE_CODE,-1);
                    entry.put(Constants.RESPONSE_MESSAGE,Constants.MESSAGE_DEVICE+Constants.MESSAGE_OFFLINE);
                    resp.addDataEntry(entry);
                    continue;
                }
                entry.put(Constants.RESPONSE_CODE,0);
                entry.put(Constants.RESPONSE_MESSAGE,"查询成功");
                resp.addDataEntry(entry);
            }

            return resp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return AjaxResponse.failed(-1, "查询出错");
        }
    }

    //把类转换成entry返回给前端，解耦和
    private Map objectToEntry(DeviceUsed deviceUsed) {
        Map entry = new HashMap();
        entry.put("id", deviceUsed.getId());
        entry.put("name", deviceUsed.getName());
        entry.put("boardNo", deviceUsed.getBoardNo());
        entry.put("deviceNum", deviceUsed.getDeviceNum());
        entry.put("description", deviceUsed.getDescription());
        entry.put("phone", deviceUsed.getPhone());
        //清空关联的设备类型的属性，防止前端无限循环
        deviceUsed.getDevice().setAttributes(null);
        entry.put("device", deviceUsed.getDevice());
        return entry;
    }

    //把类转换成entry返回给前端，解耦和
    private Map objectToEntry(UpdateConfig updateConfig) {
        Map entry = new HashMap();
        entry.put("id", updateConfig.getId());
        entry.put("name", updateConfig.getName());
        entry.put("description", updateConfig.getDescription());
        String type = updateConfig.getType();
        entry.put("type", type);
        entry.put("typeName", (type == null || "".equals(type)) ? "" : com.myee.djinn.dto.NoticeType.getValue(type));
        String seeType = updateConfig.getSeeType();
        entry.put("seeType", seeType);
        entry.put("seeTypeName", (seeType == null || "".equals(seeType)) ? "" : new UpdateConfigSeeType().getUpdateConfigSeeTypeName(seeType));
        entry.put("createTime", updateConfig.getCreateTime());
        entry.put("path", updateConfig.getPath());
        entry.put("boardNoList", updateConfig.getDeviceGroupNOList());
        entry.put("productUsedList", listBindProductUsedByConfig(updateConfig));
        entry.put("branchConfig", updateConfig.getBranchConfig());
        return entry;
    }

    //根据UpdateConfig去查询关联的设备组.传入对象中如果已经有关联设备组列表了，就不去数据库查了
    private List<ProductUsed> listBindProductUsedByConfig(UpdateConfig updateConfig) {
        if(updateConfig == null || updateConfig.getId() == null) {
            return Collections.EMPTY_LIST;
        }
        //传入对象如果已经有关联列表了，就不去数据库查了
        List<ProductUsed> listAlready = updateConfig.getProductUsed();
        List<ProductUsed> listResult = new ArrayList<ProductUsed>();
        if( listAlready != null && listAlready.size() > 0 ) {
            for( ProductUsed productUsed : listAlready ) {
                listResult.add(prepareObject(productUsed));
            }
            return listResult;
        }
        //传入对象没有关联列表则取数据库查
        List<UpdateConfigProductUsedXREF> updateConfigProductUsedXREFList = updateConfigProductUsedXREFService.listByConfigId(updateConfig.getId());
        if( updateConfigProductUsedXREFList == null || updateConfigProductUsedXREFList.size() == 0 ) {
            return Collections.EMPTY_LIST;
        }
        for( UpdateConfigProductUsedXREF updateConfigProductUsedXREF : updateConfigProductUsedXREFList ) {
            listResult.add(prepareObject(updateConfigProductUsedXREF.getProductUsed()));
        }
        return listResult;
    }

    //把productUsed关联会无限循环的字段设为null
    private ProductUsed prepareObject(ProductUsed productUsed) {
        productUsed.setDeviceUsed(null);
        productUsed.setAttributes(null);
        return productUsed;
    }

    @ExceptionHandler({SQLException.class, Exception.class})
    protected void handleException(Exception ex, HttpServletResponse resp) {
        LOGGER.error(ex.getMessage(), ex);
    }

    //-------------------------------------------------------------------------------------
    /**
     * 根据软硬件版本号，并根据设备组编号从数据库查询该设备组是否有自研平板升级配置，
     * 如果有，则查找升级文件。
     * 因为自研平板升级不支持跨版本升级，所以如果该设备组编号配置有高版本的升级，则自动可以升级它的现有版本到高版本间的任一版本。
     *
     * @param jsonArgs {'version':'设备组当前版本','name':'名称，非必须','type':'类型','orgId':'所属门店ID，非必须，建议带上','deviceGroupNO':'设备组编号' }
     *  type取值详见  NoticeType, 例如应用取值: NoticeType.APK.getCaption();
     *  deviceGroupNO : 为设备组编号
     *  orgId : 新方案升级采用勾选设备，不与店铺关联，所以该字段非必须（建议带上）
     *  name ： 软件名称，通过type可以获取到对应的版本信息，所以该字段非必须
     * version version规则: Cooky - C001      M01        A001       - RK3288_v2   -20160804 ota
     *        ||||||||||| 项目名    通用版本  头部或胸部   软件版本号    硬件版本号    时间   有ota表示是ota下载升级包，没有ota表示是直接复制到平板的升级包
     *                存储路径规则: 100/boardUpdate/项目名/硬件版本号/软件版本号
     *                查找文件规则:升级包都是差分升级包，由1升2,2再升3....，所以查询版本号的时候，是取当前版本号+1的升级包
     * @return
     */
    @RequestMapping(value = {"api/updateConfig/boardUpdate"}, method = RequestMethod.POST)
    @ResponseBody
    public AjaxResponse boardUpdateUrl(@RequestParam(value = "jsonArgs")String jsonArgs) {
        LOGGER.info("boardUpdate jsonArgs= {}", jsonArgs);
        AjaxResponse resp = AjaxResponse.success();
        if( jsonArgs == null || "".equals(jsonArgs) ) {
            return AjaxResponse.failed(-1, "参数错误");
        }
        try {
            JSONObject object = JSON.parseObject(jsonArgs);
            String version = object.getString("version");
            String name = object.getString("name");
            String type = object.getString("type");
            String orgId = object.getString("orgId");
            String deviceGroupNO = object.getString("deviceGroupNO");

            if ( null == version || StringUtil.isBlank(version)
                    || null == type || StringUtil.isBlank(type)
                    || null == deviceGroupNO || StringUtil.isBlank(deviceGroupNO)) {
                return AjaxResponse.failed(-1, "版本号、类型、设备组编号不能为空");
            }
            if ( !type.equals(Constants.UPDATE_TYPE_SELF_DESIGN_PAD) ) {
                return AjaxResponse.failed(-1, "类型不正确");
            }
            String[] versionSplit = version.split("-");
            if (versionSplit.length != 4) {
                return AjaxResponse.failed(-1, "版本号格式不正确");
            }
            String productName = versionSplit[0];
            String nextSoftwareVersion = calSoftwareVersionNext(versionSplit[1]);
            String thisPartCode = getPartCode(versionSplit[1]);//获取头、胸、手、编号
            int thisSoftwareVersionNum = getSoftwareVersionNum(versionSplit[1]);
            String hardwareVersion = versionSplit[2];
            LOGGER.info("boardUpdate thisPartCode= {};productName= {},nextSoftwareVersion= {},thisSoftwareVersionNum= {},hardwareVersion= {}"
                    ,thisPartCode,productName,nextSoftwareVersion,thisSoftwareVersionNum,hardwareVersion);
////            String time = versionSplit[3];
//            File dest = org.apache.commons.io.FileUtils.getFile(DOWNLOAD_HOME, Constants.BOARD_UPDATE_BASEPATH, productName + "/" + hardwareVersion + "/" + softwareVersion);
//            LOGGER.info("boardUpdate 文件保存路径:" + dest.getPath());
//            //只取zip结尾的文件
//            FileFilter fileFilter = new FileFilter() {
//                @Override
//                public boolean accept(File file) {
//                    if (file.getName().endsWith(".zip")) {
//                        return true;
//                    }
//                    return false;
//                }
//            };
//            File[] listFile = dest.listFiles(fileFilter);
//            if (listFile == null || listFile.length != 1) {
//                return AjaxResponse.failed(-1, "升级文件未准备好");
//            }
//            String downloadUrl = DOWNLOAD_HTTP + Constants.BOARD_UPDATE_BASEPATH + productName + "/" + hardwareVersion + "/" + softwareVersion + "/" + listFile[0].getName();
//            resp.addEntry("downloadPath", downloadUrl);
//            LOGGER.info("boardUpdate 文件下载url:" + downloadUrl);

            LOGGER.info("boardUpdate jsonArgs= {}  DOWNLOAD_HOME={}", jsonArgs, DOWNLOAD_HOME);
            String latestVersionStr = "";

            //通过 type  deviceGroupNO 获取到对应设备组和配置记录的关系表数据
            List<UpdateConfigProductUsedXREF> updateConfigProductUsedXREFList = updateConfigProductUsedXREFService.listByTypeAndDeviceGroupNO(type, deviceGroupNO);
            if (null == updateConfigProductUsedXREFList || updateConfigProductUsedXREFList.size() == 0) {
                return AjaxResponse.failed(-1, "未找到该设备组的自研平板升级配置");
            }

            //遍历循环看是否有获得+1版本权限，如果有高版本权限，则自动具有+1版本权限
            boolean hasUpdatePriority = false;//当前设备组是否有升级权限
            for( UpdateConfigProductUsedXREF uCPXREF : updateConfigProductUsedXREFList) {
                UpdateConfig updateConfig = uCPXREF.getUpdateConfig();
                //如果该设置设置了所有不可见，则跳过该版本继续遍历
                if(Constants.UPDATE_SEE_TYPE_NONE.equals(updateConfig.getSeeType())){
                    continue;
                }
                String configFilePath = updateConfig.getPath();
                configFilePath = DOWNLOAD_HOME + File.separator + Constants.ADMIN_PACK + File.separator + configFilePath;
                LOGGER.info("boardUpdate configFilePath ={} ", configFilePath);
                latestVersionStr = FileUtils.readTXT(configFilePath);
                LOGGER.info("boardUpdate latestVersionStr ={} ", latestVersionStr);
                if(latestVersionStr == null || StringUtil.isBlank(latestVersionStr)) {
                    continue;
                }

                //--------------------------jsonArray不能被转换成jsonObject---------------------
                JSONArray jsonArray = JSON.parseArray(latestVersionStr);
                if( jsonArray == null || jsonArray.size() == 0 ) {
                    LOGGER.info("boardUpdate latestVersionStr 空数组 ");
                    continue;
                }
                String configVersion = jsonArray.getJSONObject(0).getString("version");
                //配置文件存储的版本号不对，则继续遍历
                if( configVersion == null || configVersion.equals("") || configVersion.split("-").length != 4 ) {
                    LOGGER.info("boardUpdate version 不符合要求 ");
                    continue;
                }
                String[] configVersionSplit = configVersion.split("-");
                String configProductName = configVersionSplit[0];
                String configPartCode = getPartCode(configVersionSplit[1]);
                int configSoftwareVersionNum = getSoftwareVersionNum(configVersionSplit[1]);
                String configHardwareVersion = configVersionSplit[2];
                LOGGER.info("boardUpdate configPartCode= {};configProductName= {},configSoftwareVersionNum= {},configHardwareVersion= {}"
                        ,configPartCode,configProductName,configSoftwareVersionNum,configHardwareVersion);
                //如果配置文件的软件版本没有当前配置高，则继续寻找
                if( configSoftwareVersionNum <= thisSoftwareVersionNum ) {
                    LOGGER.info("boardUpdate 配置文件版本没有当前版本高 ");
                    continue;
                }
                //如果硬件版本不匹配，需要过滤？？？
                if( !configProductName.equals(productName)
                        || !configHardwareVersion.equals(hardwareVersion)
                        || !configPartCode.equals(thisPartCode)) {
                    LOGGER.info("boardUpdate 配置文件项目名、硬件版本或头胸手编号不一致 ");
                    continue;
                }

                LOGGER.info("boardUpdate 全部校验通过");
                hasUpdatePriority = true;
                break;
            }

            if(!hasUpdatePriority) {
                return AjaxResponse.failed(-1, "未找到该设备组的自研平板可用升级配置");
            }

            //有升级权限，则根据路径去找升级配置文件
            String nextConfigPath = DOWNLOAD_HOME + "/"  + Constants.BOARD_UPDATE_BASEPATH +
                    productName + "/"  + hardwareVersion + "/" + nextSoftwareVersion + "/" + Constants.UPDATE_FILENAME_SELF_DESIGN_PAD;
            LOGGER.info("boardUpdate 下个版本配置文件保存路径:" + nextConfigPath);
            latestVersionStr = FileUtils.readTXT(nextConfigPath);
            if(latestVersionStr == null || StringUtil.isBlank(latestVersionStr)) {
                return AjaxResponse.failed(-1, "未找到比当前版本+1的版本配置内容");
            }

            Map entry = new HashMap();
            entry.put(Constants.UPDATE_TYPE_SELF_DESIGN_PAD, latestVersionStr);
            resp.addDataEntry(entry);

//            UpdateConfig updateConfig = updateConfigProductUsedXREF.getUpdateConfig();
//            if(Constants.UPDATE_SEE_TYPE_NONE.equals(updateConfig.getSeeType())){
//                return latestVersionStr;
//            }
//            String configFilePath = updateConfig.getPath();
//            configFilePath = DOWNLOAD_HOME + File.separator + Constants.ADMIN_PACK + File.separator + configFilePath;
//            LOGGER.info("boardUpdate configFilePath ={} ", configFilePath);
//            latestVersionStr = FileUtils.readTXT(configFilePath);
//            LOGGER.info("boardUpdate latestVersionStr ={} ", latestVersionStr);
//            return latestVersionStr;

            return resp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),"boardUpdate "+ e);
            return AjaxResponse.failed(-1, "出错");
        }

    }

    /**
     * 计算当前版本号的下一个版本号
     *
     * @param softVersion
     * @return
     */
    private String calSoftwareVersionNext(String softVersion) throws Exception {
        int length = softVersion.length();
        //版本号前缀
        String preSoftVersion = softVersion.substring(0, length - 3);
        //当前软件版本号的数字
        int thisVersion = Integer.parseInt(softVersion.substring(length - 3));
//        LOGGER.info("格式化字符串输出:"+String.format("%03d",thisVersion + 1 ));
        return preSoftVersion + String.format("%03d", thisVersion + 1);
    }

    /**
     * 获取当前版本号的数组，比如A007的007
     * @param softVersion
     * @return
     * @throws Exception
     */
    private int getSoftwareVersionNum(String softVersion) throws Exception {
        int length = softVersion.length();
        //当前软件版本号的数字
        return Integer.parseInt(softVersion.substring(length - 3));
    }

    /**
     * 获取当前版本号的部位编号，C001M01,C001M02等
     * @param softVersion
     * @return
     * @throws Exception
     */
    private String getPartCode(String softVersion) throws Exception {
        return softVersion.substring(0, 7);
    }

    /*分支管理*************************************************************************/
    @RequestMapping(value = {"admin/configuration/branch/update"}, method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasAuthority('configuration_branch_u')")
    public AjaxResponse addTableType(@RequestBody BranchConfig branchConfig, HttpServletRequest request) throws Exception {
        AjaxResponse resp;
        try {
            String branchName = branchConfig.getName();
            String branchSubName = branchConfig.getSubName();
            BranchConfig branchConfigDB = branchConfigService.findByName(branchName, branchSubName);
            if (branchConfigDB != null && !branchConfigDB.getId().equals(branchConfig.getId())) {
                return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"已存在相同主副标题的分支！");
            }

            branchConfig = branchConfigService.update(branchConfig);
            resp = AjaxResponse.success();
            resp.addEntry(Constants.RESPONSE_UPDATE_RESULT, branchConfig);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"出错");
        }
        return resp;
    }

    @RequestMapping(value = {"admin/configuration/branch/delete"}, method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasAuthority('configuration_branch_d')")
    public AjaxResponse delTableType(@RequestBody BranchConfig branchConfig, HttpServletRequest request) throws Exception {
        AjaxResponse resp;
        try {
            if (branchConfig.getId() == null || StringUtil.isNullOrEmpty(branchConfig.getId().toString())) {
                return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"参数错误");
            }
            BranchConfig branchConfigDB = branchConfigService.findById(branchConfig.getId());
            if (branchConfigDB == null) {
                return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE, "分支不存在");
            }

            branchConfigService.delete(branchConfigDB);
            return AjaxResponse.success();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return AjaxResponse.failed(AjaxResponse.RESPONSE_STATUS_FAIURE,"可能在其他地方被使用");
        }
    }

    @RequestMapping(value = {"admin/configuration/branch/paging"}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('configuration_branch_r')")
    public
    @ResponseBody
    AjaxPageableResponse pageTypes(Model model, HttpServletRequest request, WhereRequest whereRequest) {
        AjaxPageableResponse resp = new AjaxPageableResponse();
        try {
            PageResult<BranchConfig> pageList = branchConfigService.page(whereRequest);

            List<BranchConfig> branchConfigList = pageList.getList();
            for (BranchConfig branchConfig : branchConfigList) {
                resp.addDataEntry(objectToEntry(branchConfig));
            }
            resp.setRecordsTotal(pageList.getRecordsTotal());
            return resp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return resp;
        }
    }

    //把类转换成entry返回给前端，解耦和
    private Map objectToEntry(BranchConfig branchConfig) {
        Map entry = new HashMap();
        entry.put("id", branchConfig.getId());
        entry.put("name", branchConfig.getName());
        entry.put("subName", branchConfig.getSubName());
        entry.put("description", branchConfig.getDescription());
        entry.put("createTime", branchConfig.getCreateTime());
        entry.put("manager", branchConfig.getManager());
        return entry;
    }

    @RequestMapping(value = {"admin/configuration/branch/list4Select"}, method = RequestMethod.GET)
    @ResponseBody
    public List branchList(HttpServletRequest request) {
        AjaxResponse resp = new AjaxResponse();
        try {
            WhereRequest whereRequest = new WhereRequest();
            whereRequest.setCount(-1);//不分页，查询所有结果
            List<BranchConfig> branchConfigList = branchConfigService.list();
            for (BranchConfig branchConfig : branchConfigList) {
                Map entry = new HashMap();
                entry.put("name", branchConfig.getName()+"-"+branchConfig.getSubName());
                entry.put("value", branchConfig.getId());
                resp.addDataEntry(entry);
            }
            return resp.getRows();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return null;
        }
    }

}
