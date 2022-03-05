package com.guglielmo.kairosbookerspring.bot;

import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KairosBotMessanger {

    private UserRepository userRepository;

    private TelegramBot bot;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    public KairosBotMessanger(UserRepository userRepository){
        this.userRepository=userRepository;
        bot=new TelegramBot(botToken);
    }


    public void sendMessageToAllUsers(String message) {
        userRepository.findAll()
                .stream()
                .peek(System.out::println)
                .map(User::getChadId)
                .forEach(e -> bot.execute(new SendMessage(e, message)));
    }
}
