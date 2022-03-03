package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.model.Chat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@BotController
@Slf4j
public class KairosBotRequestHandler implements TelegramMvcController {
    @Autowired
    private UserRepository userRepository;

    @Override
    public String getToken() {
        return "5244556196:AAGhj7N-qcZBjR1_B9rmsaAgIn1rwxlSeYE";
    }

    @MessageRequest("/matricola {matricola}")
    public String setMatricola(@BotPathVariable("matricola") String matricola, Chat chat) {
        if (isMatricolaValid(matricola)) {
            final User user = User.builder()
                    .matricola(matricola)
                    .chadId(chat.id())
                    .username(chat.username())
                    .build();
            userRepository.save(user);
            log.info("Utente salvato {}", user);
            return "Matricola " + matricola + " salvata";
        } else {
            return "Matricola non valida";
        }
    }

    private boolean isMatricolaValid(String matricola) {
        try {
            Long.parseLong(matricola);
        } catch (NumberFormatException ex) {
            return false;
        }
        return matricola.length() == 7;
    }
}
