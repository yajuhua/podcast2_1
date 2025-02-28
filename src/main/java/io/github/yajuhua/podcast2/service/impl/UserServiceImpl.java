package io.github.yajuhua.podcast2.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.exception.AccountNotFoundException;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.dto.UserLoginDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Gson gson;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO) {
        User user = userMapper.getByUsername(userLoginDTO.getUsername());

        if (user == null){
            //说明账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (!user.getPassword().equals(userLoginDTO.getPassword())){
            //密码错误
            throw new AccountNotFoundException(MessageConstant.PASSWORD_ERROR);
        }
        //返回实体对象
        return user;
    }

    @Override
    public ExtendInfo getExtendInfo() {
        List<User> list = userMapper.list();
        if (list == null || list.isEmpty()){
            throw new AccountNotFoundException("找不到用户信息");
        }
        User user = list.get(0);
        ExtendInfo extendInfo = null;
        try {
            extendInfo = gson.fromJson(user.getUuid(), ExtendInfo.class);
            if (extendInfo.getAlistInfo() == null){
                extendInfo.setAlistInfo(AlistInfo.builder().build());
            }
            if (extendInfo.getAddressFilter() == null){
                extendInfo.setAddressFilter(AddressFilter.builder().blacklist(new ArrayList<>()).whitelist(new ArrayList<>()).build());
            }
        } catch (Exception e) {
            extendInfo = ExtendInfo.builder()
                    .uuid(UUID.randomUUID().toString())
                    .alistInfo(AlistInfo.builder().build())
                    .addressFilter(AddressFilter.builder().blacklist(new ArrayList<>()).whitelist(new ArrayList<>()).build())
                    .build();
        }
        return extendInfo;
    }

    @Override
    public void updateExtendInfo(ExtendInfo extendInfo) {
        ExtendInfo update = getExtendInfo();
        if (extendInfo.getAlistInfo() != null){
            AlistInfo before = getExtendInfo().getAlistInfo();
            AlistInfo after = extendInfo.getAlistInfo();
            if (after.getUrl() != null){
                before.setUrl(after.getUrl());
            }
            if (after.getPath() != null){
                before.setPath(after.getPath());
            }
            if (after.getUsername() != null){
                before.setUsername(after.getUsername());
            }
            if (after.getPassword() != null){
                before.setPassword(after.getPassword());
            }
            before.setOpen(after.isOpen());
            update.setAlistInfo(before);
        }
        if (extendInfo.getPath() != null){
            update.setPath(extendInfo.getPath());
        }
        if (extendInfo.getPluginUrl() != null){
            update.setPluginUrl(extendInfo.getPluginUrl());
        }
        if (extendInfo.getGithubProxyUrl() != null){
            update.setGithubProxyUrl(extendInfo.getGithubProxyUrl());
        }
        if (extendInfo.getUuid() != null){
            update.setUuid(extendInfo.getUuid());
        }
        if (extendInfo.getAddressFilter() != null){
            AddressFilter addressFilter = extendInfo.getAddressFilter();
            if (addressFilter.getWhitelist() != null){
                //白名单
                update.getAddressFilter().setWhitelist(addressFilter.getWhitelist());
            }
            if (addressFilter.getBlacklist() != null){
                //黑名单
                update.getAddressFilter().setBlacklist(addressFilter.getBlacklist());
            }
        }
        String extendInfoJson = gson.toJson(update);
        userMapper.update(User.builder().uuid(extendInfoJson).build());
    }

    /**
     * 为""时删除该字段信息
     * @param extendInfo
     */
    @Override
    public void deleteExtendInfo(ExtendInfo extendInfo) {
        ExtendInfo update = getExtendInfo();
        if (extendInfo.getAlistInfo() != null){
            update.setAlistInfo(null);
        }
        if (extendInfo.getPath() != null){
            update.setPath(null);
        }
        if (extendInfo.getPluginUrl() != null){
            update.setPluginUrl(null);
        }
        if (extendInfo.getGithubProxyUrl() != null){
            update.setGithubProxyUrl(null);
        }
        if (extendInfo.getUuid() != null){
            update.setUuid(null);
        }
        String extendInfoJson = gson.toJson(update);
        userMapper.update(User.builder().uuid(extendInfoJson).build());
    }

    @Override
    public void updateBotInfo(BotInfo botInfoAfter) {
        BotInfo cBotInfo = getBotInfo();
        if (cBotInfo != null){
            if (botInfoAfter.getIsOpen() != null){
                cBotInfo.setIsOpen(botInfoAfter.getIsOpen());
            }
            if (botInfoAfter.getToken() != null){
                cBotInfo.setToken(botInfoAfter.getToken());
            }
            if (botInfoAfter.getUsername() != null){
                cBotInfo.setUsername(botInfoAfter.getUsername());
            }
            if (botInfoAfter.getProxy() != null){
                cBotInfo.setProxy(botInfoAfter.getProxy());
            }
            //更新
            userMapper.update(User.builder().botInfo(gson.toJson(cBotInfo)).build());
        }else {
            userMapper.update(User.builder().botInfo(gson.toJson(botInfoAfter)).build());
        }

    }

    @Override
    public BotInfo getBotInfo() {
        try {
            String botInfoJsonStr = userMapper.list().get(0).getBotInfo();
            BotInfo botInfo = gson.fromJson(botInfoJsonStr, BotInfo.class);
            if (botInfo == null){
                botInfo = new BotInfo();
                botInfo.setIsOpen(false);
            }
            return botInfo;
        } catch (JsonSyntaxException e) {
            return new BotInfo();
        }
    }
}
