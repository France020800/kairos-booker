package com.guglielmo.kairosbookerspring.bot.callbacks;

import com.guglielmo.kairosbookerspring.bot.KairosBotMessanger;
import com.guglielmo.kairosbookerspring.db.user.KairosUser;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PasswordCallback implements Callback {

    private KairosUser kairosUser;
    private UserRepository userRepository;
    private KairosBotMessanger messanger;

    public PasswordCallback(KairosUser kairosUser, UserRepository userRepository, KairosBotMessanger messanger) {
        this.kairosUser = kairosUser;
        this.userRepository = userRepository;
        this.messanger = messanger;
    }


    @Override
    public void onResponse(BaseRequest request, BaseResponse baseResponse) {
        final SendResponse sendResponse = (SendResponse) baseResponse;
        String userMessage = sendResponse.message().text();
        kairosUser.setPassword(userMessage);
        userRepository.save(kairosUser);
        log.info("User: {}", kairosUser);
        messanger.sendMessageTo(sendResponse.message().chat().id(), "Password aggiunta con successo");
    }

    @Override
    public void onFailure(BaseRequest request, IOException e) {

    }
}
