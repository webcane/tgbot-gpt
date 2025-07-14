package cane.brothers.gpt.bot.telegram.info;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "tgbot-info", url = "${tgbot.info.url}${tgbot.token}")
public interface TgBotInfoClient {

    @RequestMapping(method = RequestMethod.GET, value = "/getMe")
    TgBotInfo getInfo();
}
