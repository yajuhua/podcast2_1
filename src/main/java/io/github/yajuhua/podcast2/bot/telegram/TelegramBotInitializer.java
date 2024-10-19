package io.github.yajuhua.podcast2.bot.telegram;
import io.github.yajuhua.podcast2.controller.SubController;
import io.github.yajuhua.podcast2.pojo.entity.BotInfo;
import io.github.yajuhua.podcast2.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.net.URL;

/**
 * Telegram Bot 注册类
 */
@Slf4j
@Component
public class TelegramBotInitializer {

    @Autowired
    private UserService userService;
    @Autowired
    private SubController subController;

    @PostConstruct
    public void registerBot() throws Exception {
        BotInfo botInfo = null;
        try {
            botInfo = userService.getBotInfo();
        } catch (Exception e) {
            log.warn("用户信息未初始化: {}",e.getMessage());
            return;
        }
        if (botInfo != null
                && botInfo.getIsOpen() != null
                && botInfo.getIsOpen()
                && botInfo.getUsername() != null
                && botInfo.getToken() != null){
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            try {
                log.info("开始注册TelegramBot...");
                DefaultBotOptions botOptions = getBotOptions(botInfo);
                TGBot tgBot = new TGBot(botOptions, botInfo.getToken(), botInfo.getUsername(),subController);
                telegramBotsApi.registerBot(tgBot); // 注册 bot
            } catch (TelegramApiException e) {
               log.error("注册TelegramBot失败",e);
            }
        }
    }

    /**
     * 配置bot选项
     * @param botInfo
     * @return
     * @throws Exception
     */
    private DefaultBotOptions getBotOptions(BotInfo botInfo) throws Exception {
        String proxyUrl = botInfo.getProxy();
        DefaultBotOptions botOptions = new DefaultBotOptions();
        if (proxyUrl != null && !proxyUrl.isEmpty()){
            URL url = new URL(proxyUrl);
            String host = url.getHost();
            int port = url.getPort();
            String protocol = url.getProtocol().toUpperCase();
            botOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(protocol));
            botOptions.setProxyHost(host);
            botOptions.setProxyPort(port);
            return botOptions;
        }else {
           return botOptions;
        }
    }
}

