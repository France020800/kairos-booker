package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.guglielmo.kairosbookerspring.Booker;
import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@BotController
@Slf4j
public class KairosBotRequestHandler implements TelegramMvcController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String botToken="5244556196:AAGhj7N-qcZBjR1_B9rmsaAgIn1rwxlSeYE";

    @Override
    public String getToken() {
        return botToken;
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

    @MessageRequest("/password {password}")
    public String setPassword(@BotPathVariable("password") String password, Chat chat) {
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
//            user.setPassword(passwordEncoder.encode(password));
            user.setPassword(password);
            userRepository.save(user);
            return "Password cifrata e salvata con successo";
        }
        return "Imposta prima la matricola";
    }


    @MessageRequest("/prenota")
    public BaseRequest book(Chat chat) {
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            new Booker().book(user.getMatricola(),user.getPassword());
            final ReplyKeyboardMarkup testMenu = new ReplyKeyboardMarkup(new KeyboardButton("Test"));
            final SendMessage request = new SendMessage(user.getChadId(), "")
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(testMenu);
            return request;
        }
        return null;
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
