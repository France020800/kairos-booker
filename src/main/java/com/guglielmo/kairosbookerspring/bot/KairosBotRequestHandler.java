package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.guglielmo.kairosbookerspring.Booker;
import com.guglielmo.kairosbookerspring.Lesson;
import com.guglielmo.kairosbookerspring.db.chat.ChatHistory;
import com.guglielmo.kairosbookerspring.db.chat.ChatHistoryRepository;
import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@BotController
@Slf4j
/**
 * This class rapresent the bot that handles user's command sent through telegram
 */
public class KairosBotRequestHandler implements TelegramMvcController {

    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;


    private Booker booker;

    @Value("${bot.token}")
    private String botToken;

    /**
     * Constructor with autowired fields
     *
     * @param userRepository Repository to store user credentials
     */
    @Autowired
    public KairosBotRequestHandler(UserRepository userRepository, ChatHistoryRepository chatHistoryRepository) {
        this.userRepository = userRepository;
        this.booker = new Booker();
        this.chatHistoryRepository = chatHistoryRepository;
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
        logMessage("/start",chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent())
            return "Bentornato su KairosBot!";
        final User user = userRepository.findByChadId(chat.id())
                .orElse(User.builder()
                        .chadId(chat.id())
                        .username(chat.username())
                        .adding_matricola(false)
                        .adding_password(false)
                        .build());
        userRepository.save(user);
        return "Benvenuto su KairosBot, il bot telegram per prenotare il tuo posto in ateneo!\n\n" +
                "Per inizizare utilizza i comandi:\n" +
                "- /matricola per inserire la tua matricola;\n " +
                "- /password per inserire la tua password;\n" +
                "Una volta effettuato il login utilizza il comando /prenota per prenotare il tuo posto a lezione.";
    }

    /**
     * Method to request the username of a user
     *
     * @param chat The rapresentation of the chat with the user
     */
    @MessageRequest("/matricola")
    public String setMatricola(Chat chat) {
        logMessage("/matricola",chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        final User user = optionalUser.get();
        if (user.isAdding_password())
            return "Sono in attesa di una matricola, non puoi usare questo comando adesso!";
        user.setAdding_matricola(true);
        userRepository.save(user);
        return "Inserisci adesso la tua matricola";
    }

    /**
     * Method to request the password of a user
     *
     * @param chat The rapresentation of the chat with the user
     */
    @MessageRequest("/password")
    public String setPassword(Chat chat) {
        logMessage("/password",chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        final User user = optionalUser.get();
        if (user.isAdding_matricola())
            return "Sono in attesa di una matricola, non puoi usare questo comando adesso!";
        user.setAdding_password(true);
        userRepository.save(user);
        return "Inserisci adesso la tua password";
    }

    /**
     * Method to display a menu with the lessons to book
     *
     * @param chat The rapresentation of the chat with the user
     * @return The lessons menu
     */
    @MessageRequest("/prenota")
    public BaseRequest getCurses(Chat chat) {
        logMessage("/prenota",chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());

        if (!optionalUser.isPresent())
            return new SendMessage(chat.id(), "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start");
        final User user = optionalUser.get();
        if (user.isAdding_matricola() || user.isAdding_password())
            return new SendMessage(chat.id(), "Sono in attesa di una matricola o password.\n" +
                    "Non puoi usare un comando adesso!");
        if (user.getPassword() == null || user.getMatricola() == null)
            return new SendMessage(chat.id(), "Non è stato effettuato il login.\n" +
                    "Inserire /matricola e /password");
        final List<Lesson> courses = booker.getCourses(user.getMatricola(), user.getPassword());
        final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Lista Corsi"));
        courses.forEach(e -> lessonsMenu.addRow(e.getCourseName() + " - " + e.getDate() + " " + (e.isBooked() ? "[🟢]" : "[🔴]")));
        updateLessons(courses, user);
        final SendMessage request = new SendMessage(user.getChadId(), "Scegli un corso")
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyMarkup(lessonsMenu);
        return request;
    }

    /**
     * Method to display a menu with the lessons to book
     * and set the properties to start automatic booking
     *
     * @param chat The representation of the chat with the user
     * @return The lessons menu
     */
    @MessageRequest("/auto_prenota")
    public BaseRequest startAutoBooking(Chat chat) {
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return new SendMessage(chat.id(), "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start");
        optionalUser.get().setAuto_booking(true);
        return getCurses(chat);
    }

    /**
     * Method to display a menu with the lessons to book
     *
     * @param chat The rapresentation of the chat with the user
     * @return The data of the user
     */
    @MessageRequest("/dati")
    public String getUserData(Chat chat) {
        logMessage("/dati", chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        final User user = optionalUser.get();
        return "I tuoi dati: \n" +
                "Matricola: " + user.getMatricola() + "\n" +
                "Password: " + user.getPassword() + "\n\n" +
                "QUESTE INFORMAZIONI SONO VISIBILI SOLO A TE";
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
     * Method to remove one user
     *
     * @param chat The rapresentation of the chat with the user
     * @return The data of the user
     */
    @MessageRequest("/logout")
    public String logout(Chat chat) {
        logMessage("/logout", chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return "Utente non loggato";
        else {
            userRepository.delete(optionalUser.get());
            return "Credenziali eliminate";
        }
    }

    /**
     * Method that given the lesson title books a lesson
     *
     * @param message Lesson to book
     * @param chat    The rappresentation of the chat with the user
     * @return The outcome of the operation
     */
    @MessageRequest("{lesson:.*}")
    public BaseRequest messageManager(@BotPathVariable("lesson") String message, Chat chat) {
        logMessage(message, chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return new SendMessage(chat.id(), "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start");
        final User user = optionalUser.get();

        // Adding matricola
        if (user.isAdding_matricola()) {
            if (isMatricolaValid(message)) {
                user.setMatricola(message);
                user.setAdding_matricola(false);
                userRepository.save(user);
                log.info("Utente salvato {}", user);
                return new SendMessage(chat.id(), "Matricola " + message + " salvata");
            } else {
                return new SendMessage(chat.id(), "Matricola non valida");
            }
        }

        // Adding password
        else if (user.isAdding_password()) {
//          user.setPassword(passwordEncoder.encode(password));
            user.setPassword(message);
            user.setAdding_password(false);
            userRepository.save(user);
            return new SendMessage(chat.id(), "Password cifrata e salvata con successo");
        }

        // Start autoBooking procedure
        else if (user.isAuto_booking()) {
            // TODO - Create a autoBooking method that periodically book the selected courses
            return null;
        }

        // Choosing and book lesson
        else {
            if (isLessonWrongFormat(message)) {
                return new SendMessage(chat.id(), "Comando non disponibile");
            }
            final List<Lesson> courses = booker.book(user.getMatricola(), user.getPassword(), message);
            final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Lista Corsi"));
            courses.forEach(e -> lessonsMenu.addRow(e.getCourseName() + " - " + e.getDate() + " " + (e.isBooked() ? "[🟢]" : "[🔴]")));
            updateLessons(courses, user);
            final SendMessage request = new SendMessage(user.getChadId(), "Lezione prenotata")
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyMarkup(lessonsMenu);
            return request;
        }
    }

    private String logMessage(String message, Long chatId) {
        chatHistoryRepository.save(ChatHistory.builder()
                .chadId(chatId)
                .message(message)
                .timestamp(new Timestamp(new Date().getTime()))
                .build());
        log.info("Saved message: {}", message);
        return message;
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

    private void updateLessons(List<Lesson> lessons, User user) {
        user.setLessons(lessons);
        userRepository.save(user);
    }
}
