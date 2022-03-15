package com.guglielmo.kairosbookerspring.bot;

import com.guglielmo.kairosbookerspring.db.user.KairosUser;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KairosBotMessanger {

    private UserRepository userRepository;

    @Value("${bot.token}")
    private String botToken;

    private TelegramBot bot;

    @Autowired
    public KairosBotMessanger(UserRepository userRepository){
        this.userRepository=userRepository;
        bot=new TelegramBot(botToken);
    }


    public void sendMessageToAllUsers(String message) {
        userRepository.findAll()
                .stream()
                .peek(System.out::println)
                .map(KairosUser::getChadId)
                .forEach(e -> bot.execute(new SendMessage(e, message)));
    }

    public void sendMessageTo(Long chatId, String message) {
        final SendResponse response = bot.execute(new SendMessage(chatId, message));
    }
}
