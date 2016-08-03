package com.myee.tarot.web.files.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.myee.djinn.dto.PushResourceDTO;
import com.myee.djinn.dto.ResourceDTO;
import com.myee.djinn.dto.ResponseData;
import com.myee.djinn.endpoint.OrchidService;
import com.myee.djinn.rpc.bootstrap.ServerBootstrap;
import com.myee.tarot.core.Constants;
import com.myee.tarot.core.util.ajax.AjaxResponse;
import com.myee.tarot.core.util.ajax.AjaxPageableResponse;
import com.myee.tarot.merchant.domain.MerchantStore;
import com.myee.tarot.web.apiold.BusinessException;
import com.myee.tarot.web.files.vo.FileItem;
import com.myee.tarot.web.files.vo.PushDTO;
import com.myee.tarot.web.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Martin on 2016/4/21.
 */
@Controller
public class PushController {

    @Value("${cleverm.push.dirs}")
    private String DOWNLOAD_HOME;
    @Value("${cleverm.push.http}")
    private String DOWNLOAD_HTTP;

    @Autowired
    private ServerBootstrap serverBootstrap;

    @RequestMapping(value = "admin/file/search" , method = RequestMethod.POST)
    @ResponseBody
    public AjaxPageableResponse searchResource(@RequestParam("node") String parentNode, HttpServletRequest request) {
//        if ("/".equals(parentNode)) {
//            FileItem root = new FileItem();
//            root.setPath("/");
////            root.setResTypeName("目录");
//            return new AjaxPageableResponse(Arrays.<Object>asList(root));
//        }
        MerchantStore store = (MerchantStore)request.getSession().getAttribute(Constants.ADMIN_STORE);
        File template = getResFile(100L, parentNode);
        Map<String, FileItem> resMap = Maps.newLinkedHashMap();
        listFiles(template, resMap, 100L, store.getId());
        if (100L != store.getId()) {
            File dir = getResFile(store.getId(), parentNode);
            listFiles(dir, resMap, store.getId(), store.getId());
        }
        return new AjaxPageableResponse(Lists.<Object>newArrayList(resMap.values()));
    }

    @RequestMapping("admin/file/create")
    @ResponseBody
    public FileItem createResource( @RequestParam(value="file",required = false) CommonsMultipartFile file, @RequestParam("entityText") String entityText) throws IllegalStateException, IOException {
        FileItem vo = JSON.parseObject(entityText, FileItem.class);
        Long orgID = vo.getSalt();
        File dest = FileUtils.getFile(DOWNLOAD_HOME, Long.toString(orgID), vo.getPath(), vo.getCurrPath());
        if (!dest.exists()) {
            dest.mkdirs();
        }
        if (file != null && !file.isEmpty()) {
            String fileName = file.getFileItem().getName();
            File desFile = new File(dest+File.separator+fileName);
            desFile.createNewFile();
            file.transferTo(desFile);
        }
        if (!StringUtil.isNullOrEmpty(vo.getContent(), true)) {
            String name = vo.getName();
            File desFile = new File(dest+File.separator+name);
            desFile.createNewFile();
            FileUtils.writeStringToFile(desFile, vo.getContent());
        }
        return vo;
    }

    @RequestMapping(value = "admin/file/delete", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public AjaxResponse deleteResource(@RequestParam("salt") Long orgID, @RequestParam("path") String path, HttpServletRequest request) {
        File resFile = getResFile(orgID, path);
        boolean flag = false;
        Map map = new HashMap();
        AjaxResponse ajaxResponse = new AjaxResponse();
        if(resFile.isDirectory()) {
            if(getFiles(resFile)) {
                flag = false;
                map.put("message",flag);
                ajaxResponse.addDataEntry(map);
                return ajaxResponse;
            }
        }
        boolean isCopy = copyToRecycle(resFile);//复制文件到回收站
        if(isCopy) { //复制成功后执行删除
            MerchantStore store = (MerchantStore) request.getSession().getAttribute(Constants.ADMIN_STORE);
            if (store.getId() != orgID) {
                flag = false;
            } else {
                if (resFile.exists()) {
                    FileUtils.deleteQuietly(resFile);
                    flag = true;
                }
            }

        }
        map.put("message",flag);
        ajaxResponse.addDataEntry(map);
        return ajaxResponse;
    }

    @RequestMapping("admin/content/get")
    @ResponseBody
    public String getContentText(Long orgID, String absPath) {
        File resFile = getResFile(orgID, absPath);
        if (!resFile.exists()) {
            return "";
        }
        if (resFile.length() > 4096L) {
            throw new BusinessException("超过文本读取大小限制。");
        }
        try {
            String value = FileUtils.readFileToString(resFile, "utf-8");
            return value;
        } catch (IOException ie) {
            throw new BusinessException(ie);
        }
    }

    @RequestMapping(value = "admin/file/download" , method = RequestMethod.POST)
    @ResponseBody
    public AjaxResponse exportResource(@RequestParam("salt") Long orgID,@RequestParam("path") String path, HttpServletRequest request, HttpServletResponse response) {
        MerchantStore store = (MerchantStore)request.getSession().getAttribute(Constants.ADMIN_STORE);
        if (store.getId() != orgID) {
            return null;
        }
        String url = DOWNLOAD_HTTP + orgID.toString() + File.separator + path;
        AjaxResponse ajaxResponse = new AjaxResponse().success();
        Map map = new HashMap();
        map.put("url",url);
        ajaxResponse.addDataEntry(map);
        return ajaxResponse;
    }

    private void listFiles(File parentFile, Map<String, FileItem> resMap, Long orgID, Long storeId) {
        if (!parentFile.exists() || !parentFile.isDirectory() || null == parentFile.listFiles()) {
            return;
        }
        String prefix = FilenameUtils.concat(DOWNLOAD_HOME, Long.toString(orgID));
//        String prefixHttp = FilenameUtils.concat(DOWNLOAD_HTTP, Long.toString(orgID));
        for (File file : parentFile.listFiles()) {
            FileItem fileItem = FileItem.toResourceModel(file, orgID, storeId);
            fileItem.setPath(trimStart(fileItem.getPath(), prefix));
            fileItem.setUrl(DOWNLOAD_HTTP+orgID+File.separator+fileItem.getPath().replace("\\", "/"));
            resMap.put(file.getName(), fileItem);
        }
    }

    private File getResFile(Long orgID, String absPath) {
        return FileUtils.getFile(DOWNLOAD_HOME, Long.toString(orgID), absPath);
    }

    String trimStart(String absPath, String prefix) {
        String result = absPath;
        if (result.startsWith(prefix)) {
            result = result.substring(prefix.length() + 1);
        }
        return result;
    }

    @RequestMapping(value = "admin/file/push", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResponse pushResource(@Valid @RequestBody PushDTO pushDTO) {
        AjaxResponse resp = new AjaxResponse();
        PushResourceDTO dto = new PushResourceDTO();
        dto.setUniqueNo(pushDTO.getUniqueNo());
        dto.setAppId(pushDTO.getAppId());
        dto.setTimeout(pushDTO.getTimeout());
        OrchidService eptService = null;
        try {
            eptService = serverBootstrap.getClient(OrchidService.class, pushDTO.getUniqueNo());
        } catch (Exception e) {
            return AjaxResponse.failed(-1, "连接客户端错误");
        }
        if(eptService == null){
            return AjaxResponse.failed(-2, "获取接口出错");
        }
        try {
            dto.setContent(JSON.parseArray(pushDTO.getContent(), ResourceDTO.class));
        }catch (Exception e){
            return AjaxResponse.failed(-3, "推送内容格式错误");
        }
        ResponseData rd = null;
        try {
            rd = eptService.sendNotification(dto);
        } catch (Exception e) {
            return AjaxResponse.failed(-4, "客户端不存在");
        }
        if(rd != null && rd.isSuccess()) {
            return AjaxResponse.success();
        } else {
            return AjaxResponse.failed(-5, "发送失败，客户端出错");
        }
    }

    @RequestMapping(value = "admin/table/push", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResponse pushTable(String tableStr, String mbNum) {
        String tableStrTest = "{\"id\":1,\"tableZone\":{\"id\":1,\"name\":\"A区\"},\"description\":\"测试2\",\"name\":\"测试1\",\"tableType\":{\"id\":2,\"name\":\"大桌1\"}}";
        String mbNumStr = "Gaea-23#";
        mbNum = mbNumStr;
        OrchidService eptService = null;
        ResponseData rd = null;
        AjaxResponse resp = new AjaxResponse();
        try {
//            eptService = serverBootstrap.getClient(OrchidService.class, mbNum);
            String pushTableStr = JSONObject.toJSONString(tableStrTest);
//            rd = eptService.sendNotification(pushTableStr);
            if(rd != null) {
                resp = AjaxResponse.success();
            } else {
                resp = AjaxResponse.failed(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    final static Charset charset = Charset.forName("UTF8");

    static String toClientUUID(long shopId) {
        String rawId = String.format("shopId:%08d", shopId);
        return Base64.encodeBase64String(rawId.getBytes(charset));
    }

    /**
     * 复制文件至回收站
     * @param file
     * @return
     */
    public boolean copyToRecycle(File file) {
        try {
            if (file.exists()) {
                String tempFilePath = file.getPath().replaceAll("\\\\", "/");//把路径中的反斜杠替换成斜杠
                String tempDownloadPath = DOWNLOAD_HOME.replaceAll("\\\\","/")+"/";//准备用于替换成url的下载文件夹路径
                String tempTargetPath = (DOWNLOAD_HOME + File.separator + "deleted" + File.separator).replaceAll("\\\\","/");
                String targetPath = tempFilePath.replaceAll(tempDownloadPath, tempTargetPath);
                targetPath = targetPath.replaceAll("/","\\\\");//把路径转回linux兼容
                if (file.isFile()) {
                    // Destination directory
                    File dir = new File(targetPath);
                    File parentPath = new File(dir.getParent());
                    parentPath.mkdirs();
                    // Move file to new directory
                    boolean success = file.renameTo(dir);
                } else if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        copyToRecycle(files[i]);
                    }
                }
                return true;
            } else {
                System.out.println("所删除的文件不存在！" + '\n');
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("unable to delete the folder!");
        }
        return false;
    }

    /*
     * 通过递归得到某一路径下所有的目录及其文件
    */
    static boolean getFiles(File root){
//        File root = new File(filePath);
        File[] files = root.listFiles();
        boolean flag = false;
        for(File file : files){
            if(file.isDirectory()){
                /*
                 * 递归调用
                */
                getFiles(file);
            } else{
                flag = true;
                break;
            }
        }
        return flag;
    }
}
