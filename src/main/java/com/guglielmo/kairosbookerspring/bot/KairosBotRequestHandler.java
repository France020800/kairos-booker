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
     * Method to welcome for new user
     *
     * @param chat The rapresentation of the chat with the user
     */
    @MessageRequest("/start")
    public String welcomeUser(Chat chat) {
        final User user = userRepository.findByChadId(chat.id())
                .orElse(User.builder()
                        .chadId(chat.id())
                        .username(chat.username())
                        .adding_matricola(false)
                        .adding_password(false)
                        .build());
        userRepository.save(user);
        return "Benvenuto su KairosBot, il bot telegram per prenotare il tuo posto in ateneo!";
    }

    /**
     * Method to request the username of a user
     *
     * @param chat The rapresentation of the chat with the user
     */
    @MessageRequest("/matricola")
    public String setMatricola(Chat chat) {
        try {
            final User user = userRepository.findByChadId(chat.id()).get();
            if (user.isAdding_password())
                return "Sono in attesa di una matricola, non puoi usare questo comando adesso!";
            user.setAdding_matricola(true);
            userRepository.save(user);
            return "Inserisci adesso la tua matricola";
        } catch (Exception e) {
            log.info(e.getMessage());
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        }
    }

    /**
     * Method to request the password of a user
     *
     * @param chat The rapresentation of the chat with the user
     */
    @MessageRequest("/password")
    public String setPassword(Chat chat) {
        try {
            final User user = userRepository.findByChadId(chat.id()).get();
            if (user.isAdding_matricola())
                return "Sono in attesa di una matricola, non puoi usare questo comando adesso!";
            user.setAdding_password(true);
            userRepository.save(user);
            return "Inserisci adesso la tua password";
        } catch (Exception e) {
            log.info(e.getMessage());
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        }
    }


    /**
     * Method to display a menu with the lessons to book
     *
     * @param chat The rapresentation of the chat with the user
     * @return The lessons menu
     */
    @MessageRequest("/prenota")
    public BaseRequest getCurses(Chat chat) {
        try {
            final User user = userRepository.findByChadId(chat.id()).get();
            if (user.isAdding_matricola()||user.isAdding_password())
                return new SendMessage(chat.id(), "Sono in attesa di una matricola o password.\n" +
                        "Non puoi usare un comando adesso!");
            final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
            if (optionalUser.isPresent()) {
                // final User user = optionalUser.get();
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
        } catch (Exception e) {
            final SendMessage request = new SendMessage(chat.id(), "Non è stato effettuato il login\n" +
                    "Devi inserire prima una matricola e password");
            return request;
        }
    }

    /**
     * Method to display a menu with the lessons to book
     *
     * @param chat The rapresentation of the chat with the user
     * @return The data of the user
     */
    @MessageRequest("/visualizza informazioni")
    public String getUserData(Chat chat) {
        try {
            final User user = userRepository.findByChadId(chat.id()).get();
            return "I tuoi dati: \n" +
                    "Matricola: " + user.getMatricola() + "\n" +
                    "Passord: " + user.getPassword();
        } catch (Exception e) {
            log.info(e.getMessage());
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        }
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
     * @param message Lesson to book
     * @param chat   The rappresentation of the chat with the user
     * @return The outcome of the operation
     */
    @MessageRequest("{lesson:.*}")
    public String messageManager(@BotPathVariable("lesson") String message, Chat chat) {
        final User user = userRepository.findByChadId(chat.id()).get();
        if (user.isAdding_matricola()) {
            if (isMatricolaValid(message)) {
                user.setMatricola(message);
                user.setAdding_matricola(false);
                userRepository.save(user);
                log.info("Utente salvato {}", user);
                return "Matricola " + message + " salvata";
            } else {
                return "Matricola non valida";
            }
        } else if (user.isAdding_password()) {
            final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
            if (optionalUser.isPresent()) {
//              user.setPassword(passwordEncoder.encode(password));
                user.setPassword(message);
                user.setAdding_password(false);
                userRepository.save(user);
                return "Password cifrata e salvata con successo";
            }
            user.setAdding_password(false);
            userRepository.save(user);
            return "Imposta prima la matricola";
        } else {
            if (isLessonWrongFormat(message)) {
                return "Comando non disponibile";
            }
            final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
            if (optionalUser.isPresent()) {
                booker.book(user.getMatricola(), user.getPassword(), message);
                return "Lezione Prenotata";
            }
            return "Impossibile prenotare la lezione, accedi";
        }
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
