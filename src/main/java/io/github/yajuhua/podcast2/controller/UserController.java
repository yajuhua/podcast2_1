package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.common.constant.JwtClaimsConstant;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.exception.CertificateFileException;
import io.github.yajuhua.podcast2.common.exception.SubNotFoundException;
import io.github.yajuhua.podcast2.common.exception.UserException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.JwtProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.CertUtils;
import io.github.yajuhua.podcast2.common.utils.JwtUtil;
import io.github.yajuhua.podcast2.common.utils.NetWorkUtils;
import io.github.yajuhua.podcast2.mapper.ExtendMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.dto.ApiDocStatusDTO;
import io.github.yajuhua.podcast2.pojo.dto.UserLoginDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.ApiTokenVO;
import io.github.yajuhua.podcast2.pojo.vo.UserLoginVO;
import io.github.yajuhua.podcast2.service.ExtendService;
import io.github.yajuhua.podcast2.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Api(tags = "用户相关接口")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SubMapper subMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private Gson gson;
    @Autowired
    private Alist alist;
    @Autowired
    private ExtendService extendService;
    public static AddressFilter addressFilterTmp;//临时存放区

    /**
     * 用户登录
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.debug("用户登录:{}",userLoginDTO);
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        if(username == null || username.length() > 30 || password == null || password.length() > 30){
            throw new UserException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        User user = userService.login(userLoginDTO);

        //生成JWT令牌
        Map<String, Object> claims = new HashMap<>();

        claims.put(JwtClaimsConstant.UUID,userService.getExtendInfo().getUuid());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .username(user.getUsername())
                .token(token)
                .tokenName(jwtProperties.getUserTokenName())
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 登出
     * @return
     */
    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<String> logout(){
        return Result.success();
    }


    /**
     * 修改用户名和密码
     * @param userLoginDTO
     * @return
     */
    @ApiOperation("修改用户名和密码")
    @PostMapping("/change")
    @Transactional
    public Result change(@RequestBody UserLoginDTO userLoginDTO){
        if (userLoginDTO == null){
            throw new UserException(MessageConstant.USERNAME_OR_PASSWORD_NULL);
        }
        User user = new User();
        user.setUsername(userLoginDTO.getUsername());
        user.setPassword(userLoginDTO.getPassword());
        userMapper.update(user);
        return Result.success();
    }


    /**
     * 更新自定义附件域名
     * @param domain
     * @return
     */
    @ApiOperation("更新自定义附件域名")
    @PostMapping("/enclosureDomain")
    @Transactional
    public Result enclosureDomain(@RequestParam String domain){
        if (domain == null || domain.contains(" ")){
            throw new UserException(MessageConstant.DOMAIN_NULL);
        }
        User user = new User();
        user.setHostname(domain);
        userMapper.update(user);
        return Result.success();
    }

    /**
     * 获取自定义附件域名
     * @return
     */
    @ApiOperation("获取自定义附件域名")
    @GetMapping("/enclosureDomain")
    @Transactional
    public Result getEnclosureDomain(){
        List<User> list = userMapper.list();
        if (list.isEmpty()){
            return Result.success();
        }
        String hostname = list.get(0).getHostname();
        return Result.success(hostname);
    }

    /**
     * 删除自定义附件域名
     * @return
     */
    @ApiOperation("删除自定义附件域名")
    @DeleteMapping("/enclosureDomain")
    @Transactional
    public Result delEnclosureDomain(){
        User user = new User();
        user.setHostname("none");//表示没有自定义域名
        userMapper.update(user);
        return Result.success();
    }


    /**
     * 订阅数据导出
     * @param uuids
     * @return
     */
    @ApiOperation("订阅数据导出")
    @GetMapping("/dataExport")
    public Result<List<DataExport>> dataExport(@RequestParam List<String> uuids){
        log.info("数据导出:{}",uuids);
        List<DataExport> dataExportList = new ArrayList<>();
        for (String uuid : uuids) {
            Sub sub = subMapper.selectByUuid(uuid);
            if (sub == null){
                throw new SubNotFoundException(MessageConstant.SUB_NOT_FOUND_FAILED);
            }
            List<Extend> extendList = extendMapper.selectByUuid(uuid);
            dataExportList.add(new DataExport(sub,extendList));
        }
        return Result.success(dataExportList);
    }


    /**
     * 数据导入
     * @param dataExportList
     * @return
     */
    @ApiOperation("数据导入")
    @PostMapping("/dataImport")
    @Transactional
    public Result dataImport(@RequestBody List<DataExport> dataExportList){
        log.info("数据导入");
        List<Sub> subList = subMapper.list();
        int noExist = dataExportList.stream().filter(dataExport -> {
            for (Sub sub : subList) {
                if (sub.getUuid().equals(dataExport.getSub().getUuid())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList()).size();

        //如果存在就无法添加
        if (noExist > 0){
            throw new UserException(MessageConstant.SUB_EXIST);
        }
        //添加到数据库
        for (DataExport export : dataExportList) {
            subMapper.addSub(export.getSub());
            extendService.batchExtend(export.getExtendList());
        }
        return Result.success();
    }

    /**
     * 是否有证书
     * @return
     */
    @ApiOperation("证书列表")
    @GetMapping("/cert")
    public Result certificateVO(){
        List<Boolean> list = new ArrayList<>();
        boolean hasSsl = userMapper.list().get(0).getHasSsl();
        if (hasSsl){
            list.add(true);
        }
        return Result.success(list);
    }

    /**
     * 添加证书和密钥
     * @return
     */
    @ApiOperation("添加证书和密钥")
    @Transactional
    @PostMapping("/cert")
    public Result addCert(@RequestParam("files") MultipartFile[] files) throws Exception {
        log.info("上传证书和密钥文件:{}",files);

        if(files == null || files.length < 2){
            throw new CertificateFileException(MessageConstant.CRT_OR_KEY_FILE_NULL);
        }

        MultipartFile cert = null;
        MultipartFile key = null;
        for (MultipartFile file : files) {
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            if (ext.equals("crt")){
                cert = file;
            }
            if(ext.equals("key")){
                key = file;
            }
        }

        if (cert == null || key == null){
            throw new CertificateFileException(MessageConstant.CRT_OR_KEY_FILE_NULL);
        }

        //拷贝流
        InputStream certInputStream = cert.getInputStream();
        InputStream keyInputStream = key.getInputStream();

        ByteArrayOutputStream certByteArrayOutputStream = CertUtils.cloneInputStream(certInputStream);
        ByteArrayOutputStream keyByteArrayOutputStream = CertUtils.cloneInputStream(keyInputStream);

        InputStream certInputStream2 = new  ByteArrayInputStream(certByteArrayOutputStream.toByteArray());
        InputStream keyInputStream2 = new  ByteArrayInputStream(keyByteArrayOutputStream.toByteArray());

        InputStream certInputStream3 = new  ByteArrayInputStream(certByteArrayOutputStream.toByteArray());
        InputStream keyInputStream3 = new  ByteArrayInputStream(keyByteArrayOutputStream.toByteArray());

        boolean isCertFile = CertUtils.isCertFile(certInputStream2);
        boolean isKeyFile = CertUtils.isKeyFile(keyInputStream2);

        if (isKeyFile && isCertFile){
            //将内容写入文件
            FileOutputStream certOut = new FileOutputStream(dataPathProperties.getCertificatePath());
            FileOutputStream keyOut = new FileOutputStream(dataPathProperties.getCertificatePrivateKeyPath());
            IOUtils.copyLarge(certInputStream3,certOut);
            IOUtils.copyLarge(keyInputStream3,keyOut);
            certOut.flush();
            keyOut.flush();
            certOut.close();
            keyOut.close();
            //数据库is_ssl
            User user = new User();
            user.setIsSsl(true);
            user.setHasSsl(true);
            userMapper.update(user);
        }else {
            throw new CertificateFileException(MessageConstant.CRT_OR_KEY_FILE_NULL);
        }

        return Result.success();
    }

    /**
     * 删除证书或密钥
     * @return
     */
    @ApiOperation("删除证书或密钥")
    @DeleteMapping("/cert")
    @Transactional
    public Result deleteCert(){
        User user = userMapper.list().get(0);
        if (user.getHasSsl()){
            File key = new File(dataPathProperties.getCertificatePrivateKeyPath());
            File cert = new File(dataPathProperties.getCertificatePath());
            if (key.exists() && cert.exists()){
                try {
                    FileUtils.forceDelete(cert);
                    FileUtils.forceDelete(key);
                } catch (IOException e) {
                    throw new CertificateFileException(MessageConstant.CERT_OR_KEY_FILE_DELETE_ERR);
                }
            }
        }
        user.setHasSsl(false);
        user.setIsSsl(false);
        userMapper.update(user);

        return Result.success();
    }

    /**
     * 开关ssl
     * @param status
     * @return
     */
    @ApiOperation("开关ssl")
    @PostMapping("/switchSsl")
    @Transactional
    public Result switchSsl(@RequestParam Boolean status){
        if (status != null){
            User user = userMapper.list().get(0);
            if (!user.getHasSsl()){
                throw new CertificateFileException(MessageConstant.SSL_IS_NULL);
            }
            user.setIsSsl(status);
            userMapper.update(user);
        }else{
            throw new CertificateFileException(MessageConstant.SWITCH_SSL_STATUS_NULL);
        }
        return Result.success();
    }

    /**
     * 获取ssl状态
     * @return
     */
    @ApiOperation("获取ssl状态")
    @GetMapping("/sslStatus")
    public Result isSsl(){
        return Result.success(userMapper.list().get(0).getIsSsl());
    }

    /**
     * 更新登录页面访问路径
     * @param path
     * @return
     */
    @ApiOperation("更新登录页面访问路径")
    @PostMapping("/path")
    public Result updatePath(@RequestParam String path){
        if (path == null){
            userService.deleteExtendInfo(ExtendInfo.builder().path("").build());
        }else {
            userService.updateExtendInfo(ExtendInfo.builder().path(path).build());
        }
        return Result.success();
    }


    /**
     * 移除登录页面访问路径
     * @return
     */
    @ApiOperation("移除登录页面访问路径")
    @DeleteMapping("/path")
    public Result removePath(){
        updatePath(null);
        return Result.success();
    }

    /**
     * 获取面板访问路径
     * @return
     */
    @ApiOperation("获取面板访问路径")
    @GetMapping("/path")
    public Result getPath(){
        return Result.success(userService.getExtendInfo().getPath());
    }

    /**
     * 获取github加速站
     * @return
     */
    @ApiOperation("获取github加速站")
    @GetMapping("/github")
    public Result getGithubProxyUrl(){
        return Result.success(userService.getExtendInfo().getGithubProxyUrl());
    }

    /**
     * 更新github加速站
     * @return
     */
    @ApiOperation("更新github加速站")
    @PostMapping("/github")
    public Result updateGithubProxyUrl(@RequestParam String githubProxyUrl){
        userService.updateExtendInfo(ExtendInfo.builder().githubProxyUrl(githubProxyUrl).build());
        return Result.success();
    }

    /**
     * 删除github加速站
     * @return
     */
    @ApiOperation("删除github加速站")
    @DeleteMapping("/github")
    public Result deleteGithubProxyUrl(){
        userService.deleteExtendInfo(ExtendInfo.builder().githubProxyUrl("").build());
        return Result.success();
    }

    /**
     * 获取插件仓库链接
     * @return
     */
    @ApiOperation("获取插件仓库链接")
    @GetMapping("/plugin")
    public Result getPluginUrl(){
        return Result.success(userService.getExtendInfo().getPluginUrl());
    }

    /**
     * 更新插件仓库链接
     * @return
     */
    @ApiOperation("更新插件仓库链接")
    @PostMapping("/plugin")
    public Result updatePluginUrl(@RequestParam String pluginUrl){
        userService.updateExtendInfo(ExtendInfo.builder().pluginUrl(pluginUrl).build());
        return Result.success();
    }

    /**
     * 删除插件仓库链接
     * @return
     */
    @ApiOperation("删除插件仓库链接")
    @DeleteMapping("/plugin")
    public Result deletePluginUrl(){
        userService.deleteExtendInfo(ExtendInfo.builder().pluginUrl("").build());
        return Result.success();
    }

    /**
     * 获取Alist状态
     * @return
     */
    @ApiOperation("获取Alist状态")
    @GetMapping("/alist/status")
    public Result aListStatus(){
        AlistInfo alistInfo = userService.getExtendInfo().getAlistInfo();
        if (alistInfo.isOpen() && alist.isConnect()){
            return Result.success();
        } else if (alistInfo.isOpen() && !alist.isConnect()) {
            return Result.error("Alist连接失败");
        } else if (!alistInfo.isOpen()) {
            return Result.error("Alist未开启");
        }
        return Result.success();
    }

    /**
     * 更新Alist配置
     * @return
     */
    @ApiOperation("更新Alist配置")
    @PostMapping("/alist/update")
    public Result aListUpdate(@RequestBody AlistInfo submitAlist){
        AlistInfo alistInfo = submitAlist;
        if (alistInfo.getUsername().length() == 0){
            alistInfo.setUsername(null);
        }
        if (alistInfo.getPassword().length() == 0){
            alistInfo.setPassword(null);
        }
        if (alistInfo.getPath().length() == 0){
            alistInfo.setPath(null);
        }
        if (alistInfo.getUrl().length() == 0){
            alistInfo.setUrl(null);
        }
        if (alistInfo.isOpen()){
            userService.updateExtendInfo(ExtendInfo.builder().alistInfo(alistInfo).build());
            //重新登录
            alistInfo = userService.getExtendInfo().getAlistInfo();
            alist.setUrl(alistInfo.getUrl());
            alist.setPath(alistInfo.getPath());
            alist.setUsername(alistInfo.getUsername());
            alist.setPassword(alistInfo.getPassword());
            try {
                alist.refreshToken();
                alist.existPath();
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }else {
            userService.updateExtendInfo(ExtendInfo.builder().alistInfo(AlistInfo.builder().open(false).build()).build());
            return Result.success();
        }
        return Result.success();
    }

    /**
     * 获取alist配置信息
     * @return
     */
    @ApiOperation("获取alist配置信息")
    @GetMapping("/alist/info")
    public Result<AlistInfo> aListInfo(){
        AlistInfo alistInfo = userService.getExtendInfo().getAlistInfo();

        try {
            alistInfo.setUsername(alistInfo.getUsername().length()>0?"******":alistInfo.getUsername());
            alistInfo.setPassword(alistInfo.getPassword().length()>0?"******":alistInfo.getPassword());
        } catch (Exception e) {
            return Result.success(alistInfo);
        }
        return Result.success(alistInfo);
    }

    /**
     * 添加黑名单
     * @return
     */
    @ApiOperation("更新黑白名单")
    @PostMapping("/addressFilter/update")
    public Result updateWhiteBlacklist(@RequestBody AddressFilter addressFilter){
        List<String> blacklist = addressFilter.getBlacklist();
        List<String> whitelist = addressFilter.getWhitelist();

        //校验格式是否合法
        for (String b : blacklist) {
            boolean validIp = NetWorkUtils.isValidIp(b);
            boolean validIpRange = NetWorkUtils.isValidIpRange(b);
            if (!validIp && !validIpRange){
               return Result.error("输入的IP地址格式不对：" + b);
            }
        }
        for (String w : whitelist) {
            boolean validIp = NetWorkUtils.isValidIp(w);
            boolean validIpRange = NetWorkUtils.isValidIpRange(w);
            if (!validIp && !validIpRange){
               return Result.error("输入的IP地址格式不对：" + w);
            }
        }
        ExtendInfo update = ExtendInfo.builder().addressFilter(addressFilter).build();
        userService.updateExtendInfo(update);
        //可以直接访问变量获取
        addressFilterTmp = addressFilter;
        return Result.success();
    }

    /**
     * 获取地址过滤
     * @return
     */
    @ApiOperation("获取黑白名单")
    @GetMapping("/addressFilter")
    public Result<AddressFilter> getAddressFilter(){
        return Result.success(userService.getExtendInfo().getAddressFilter());
    }

    /**
     * 获取apiToken
     * @return
     */
    @ApiOperation("获取apiToken")
    @GetMapping("/apiToken")
    public Result<String> getApiToken(){
        try {
            List<User> users = userMapper.list();
            return Result.success(users.get(0).getApiToken());
        } catch (Exception e) {
            log.error("无法获取apiToken: {}",e.getMessage());
            return Result.error("无法获取apiToken");
        }
    }

    /**
     * 是否有apiToken
     * @return
     */
    @ApiOperation("是否有apiToken")
    @GetMapping("/hasApiToken")
    public Result<Boolean> hasApiToken(){
        String data = getApiToken().getData();
        if (getApiToken().getCode() == 1
             && data != null
             && !data.equalsIgnoreCase("none")
             && !data.isEmpty()){
           return Result.success(true);
       }else {
         return Result.success(false);
     }
    }

    /**
     * 创建apiToken
     * @return
     */
    @ApiOperation("创建apiToken")
    @GetMapping("/createApiToken")
    public Result<String> createApiToken(){
        String uuid = UUID.randomUUID().toString();
        try {
            User user = userMapper.list().get(0);
            user.setApiToken(uuid);
            userMapper.update(user);
        } catch (Exception e) {
            log.error("apiToken创建失败: {}",e.getMessage());
            return Result.error("apiToken创建失败");
        }
        return Result.success(uuid);
    }

    /**
     * 删除apiToken
     * @return
     */
    @ApiOperation("删除apiToken")
    @DeleteMapping("/apiToken")
    public Result deleteApiToken(){
        try {
            User user = userMapper.list().get(0);
            user.setApiToken("none");
            userMapper.update(user);
        } catch (Exception e) {
            log.error("apiToken删除失败: {}",e.getMessage());
            return Result.error("apiToken删除失败");
        }
        return Result.success();
    }

    /**
     * 获取apiToken信息
     * @return
     */
    @ApiOperation("获取apiToken信息")
    @GetMapping("/apiTokenInfo")
    public Result<ApiTokenVO> getApiTokenInfo(){
        ApiTokenVO apiTokenVO = ApiTokenVO.builder()
                .hasApiToken(hasApiToken().getData())
                .apiToken(getApiToken().getData())
                .build();
        return Result.success(apiTokenVO);
    }

    /**
     * 获取机器人消息
     * @return
     */
    @ApiOperation("获取机器人信息")
    @GetMapping("/botInfo")
    public Result<BotInfo> getBotInfo(){
        BotInfo botInfo = userService.getBotInfo();
        return Result.success(botInfo);
    }

    /**
     * 更新机器人信息
     * @param botInfo
     * @return
     */
    @ApiOperation("更新机器人信息")
    @PutMapping("/botInfo")
    public Result updateBotInfo(@RequestBody BotInfo botInfo){
        userService.updateBotInfo(botInfo);
        return Result.success();
    }

    /**
     * 获取api文档状态
     * @return
     */
    @ApiOperation("获取api文档状态")
    @GetMapping("/apiDocStatus")
    public Result<Boolean> getApiDocStatus(){
        Boolean apiDoc = userMapper.list().get(0).getApiDoc();
        return Result.success(apiDoc);
    }

    /**
     * 更新api文档开启和关闭状态
     * @param apiDocStatus
     * @return
     */
    @ApiOperation("更新api文档状态")
    @PostMapping("/apiDocStatus")
    public Result updateApiDocStatus(@RequestBody ApiDocStatusDTO apiDocStatus){
        userMapper.update(User.builder().apiDoc(apiDocStatus.getStatus()).build());
        return Result.success();
    }



}
