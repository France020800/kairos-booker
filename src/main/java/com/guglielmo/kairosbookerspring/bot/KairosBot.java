package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@BotController
public class KairosBot implements TelegramMvcController {
    @Autowired
    private UserRepository userRepository;
    @Override
    public String getToken() {
        return "5244556196:AAGhj7N-qcZBjR1_B9rmsaAgIn1rwxlSeYE";
    }

    @MessageRequest("/matricola {username:[0-9]{7}}")
    public String setMatricola(@BotPathVariable("username") String username) {
        userRepository.save(User.builder().username(username).build());
        return "Matricola "+username+" salvata";
    }
}
