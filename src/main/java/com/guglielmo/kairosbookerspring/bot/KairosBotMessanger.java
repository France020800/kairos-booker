package com.guglielmo.kairosbookerspring.bot;

import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
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

    private TelegramBot bot;


    @Autowired
    public KairosBotMessanger(UserRepository userRepository){
        this.userRepository=userRepository;
        bot=new TelegramBot("5244556196:AAGhj7N-qcZBjR1_B9rmsaAgIn1rwxlSeYE");
    }


    public void sendMessageToAllUsers(String message) {
        userRepository.findAll()
                .stream()
                .peek(System.out::println)
                .map(User::getChadId)
                .forEach(e -> bot.execute(new SendMessage(e, message)));
    }

    public void sendMessageTo(Long chatId, String message) {
        final SendResponse response = bot.execute(new SendMessage(chatId, message));
    }
}
