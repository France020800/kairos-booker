package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.guglielmo.kairosbookerspring.Booker;
import com.guglielmo.kairosbookerspring.Lesson;
import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@BotController
@Slf4j
/**
 * This class rapresent the bot that handles user's command sent through telegram
 */
public class KairosBotRequestHandler implements TelegramMvcController {

    private UserRepository userRepository;


    private Booker booker;

    @Value("${bot.token}")
    private String botToken;

    /**
     * Constructor with autowired fields
     *
     * @param userRepository Repository to store user credentials
     */
    @Autowired
    public KairosBotRequestHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.booker = new Booker();
    }

    @Override
    public String getToken() {
        return botToken;
    }

    /**
     * Method to set the username of a user
     *
     * @param matricola Username
     * @param chat      The rapresentation of the chat with the user
     * @return The outcome of the operation
     */
    @MessageRequest("/matricola {matricola}")
    public String setMatricola(@BotPathVariable("matricola") String matricola, Chat chat) {
        if (isMatricolaValid(matricola)) {
            final User user = userRepository.findByChadId(chat.id())
                    .orElse(User.builder()
                            .matricola(matricola)
                            .chadId(chat.id())
                            .username(chat.username())
                            .build());
            user.setMatricola(matricola);
            userRepository.save(user);
            log.info("Utente salvato {}", user);
            return "Matricola " + matricola + " salvata";
        } else {
            return "Matricola non valida";
        }
    }

    /**
     * Method to set the password of a user
     *
     * @param password Password
     * @param chat     The rapresentation of the chat with the user
     * @return The outcome of the operation
     */
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


    /**
     * Method to display a menu with the lessons to book
     *
     * @param chat The rapresentation of the chat with the user
     * @return The lessons menu
     */
    @MessageRequest("/prenota")
    public BaseRequest getCurses(Chat chat) {
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            final List<Lesson> courses = booker.getCourses(user.getMatricola(), user.getPassword());
            final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Test"));
            courses.forEach(e -> lessonsMenu.addRow(e.getCourseName() + " - " + e.getDate() + " " + e.isBooked()));
            final SendMessage request = new SendMessage(user.getChadId(), "Scegli un corso")
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(lessonsMenu);
            return request;
        }
        return null;
    }

    @MessageRequest("/test")
    public BaseRequest test(Chat chat) {
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            final InlineKeyboardMarkup inlineButtons = new InlineKeyboardMarkup();
            inlineButtons.addRow(new InlineKeyboardButton("Test"));
            inlineButtons.addRow(new InlineKeyboardButton("Test 2"));
            final SendMessage request = new SendMessage(user.getChadId(), "Scegli un corso")
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(inlineButtons);
            return request;
        }
        return null;
    }

    /**
     * Method that given the lesson title books a lesson
     *
     * @param lesson Lesson to book
     * @param chat   The rappresentation of the chat with the user
     * @return The outcome of the operation
     */
    @MessageRequest("{lesson:.*}")
    public String bookLesson(@BotPathVariable("lesson") String lesson, Chat chat) {
        if (isLessonWrongFormat(lesson)) {
            return "Comando non disponibile";
        }
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            booker.book(user.getMatricola(), user.getPassword(), lesson);
            return "Lezione Prenotata";
        }
        return "Impossibile prenotare la lezione, accedi";
    }

    boolean isLessonWrongFormat(String lesson) {
        return !Pattern.matches("([A-Z]* )*- .*", lesson);
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
