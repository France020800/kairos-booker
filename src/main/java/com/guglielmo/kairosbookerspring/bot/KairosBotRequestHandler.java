package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.guglielmo.kairosbookerspring.Booker;
import com.guglielmo.kairosbookerspring.Lesson;
import com.guglielmo.kairosbookerspring.db.chat.ChatHistory;
import com.guglielmo.kairosbookerspring.db.chat.ChatHistoryRepository;
import com.guglielmo.kairosbookerspring.db.lessonToBook.LessonToBook;
import com.guglielmo.kairosbookerspring.db.lessonToBook.LessonToBookRepository;
import com.guglielmo.kairosbookerspring.db.user.User;
import com.guglielmo.kairosbookerspring.db.user.UserRepository;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@BotController
@Slf4j
/**
 * This class rapresent the bot that handles user's command sent through telegram
 */
public class KairosBotRequestHandler implements TelegramMvcController {

    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final LessonToBookRepository lessonToBookRepository;
    private final KairosBotMessanger messanger;


    private Booker booker;

    @Value("${bot.token}")
    private String botToken;

    /**
     * Constructor with autowired fields
     *
     * @param userRepository Repository to store user credentials
     */
    @Autowired
    public KairosBotRequestHandler(UserRepository userRepository, ChatHistoryRepository chatHistoryRepository, LessonToBookRepository lessonToBookRepository) {
        this.userRepository = userRepository;
        this.booker = new Booker();
        this.chatHistoryRepository = chatHistoryRepository;
        messanger = new KairosBotMessanger(userRepository);
        this.lessonToBookRepository = lessonToBookRepository;
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
        log.info(chat.id().toString());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent())
            return "Bentornato su KairosBot!";
        final User user = userRepository.findByChadId(chat.id())
                .orElse(User.builder()
                        .chadId(chat.id())
                        .username(chat.username())
                        .addingMatricola(false)
                        .addingPassword(false)
                        .addingAutoBooking(false)
                        .removingAutoBooking(false)
                        .build());
        userRepository.save(user);
        return "Benvenuto su KairosBot, il bot telegram per prenotare il tuo posto in ateneo!\n\n" +
                "Per inizizare utilizza i comandi:\n" +
                "- /matricola per inserire la tua matricola;\n" +
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
        if (checkCommandRunning(user))
            return "Non puoi usare questo comando adesso!";
        user.setAddingMatricola(true);
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
        if (optionalUser.isEmpty())
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        final User user = optionalUser.get();
        if (checkCommandRunning(user))
            return "Non puoi usare questo comando adesso!";
        user.setAddingPassword(true);
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
        if (optionalUser.isEmpty())
            return new SendMessage(chat.id(), "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start");
        final User user = optionalUser.get();
        if (checkCommandRunning(user))
            return new SendMessage(chat.id(),"Non puoi usare un comando adesso!");
        if (user.getPassword() == null || user.getMatricola() == null)
            return new SendMessage(chat.id(), "Non è stato effettuato il login.\n" +
                    "Inserire /matricola e /password");
        messanger.sendMessageTo(chat.id(), "Ricerca delle lezioni in corso...");
        final List<Lesson> courses = booker.getCourses(user.getMatricola(), user.getPassword());
        final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Lista Corsi"));
        courses.forEach(e -> lessonsMenu.addRow(e.getCourseName() + " - " + e.getDate() + " " + (e.isBooked() ? "[🟢]" : "[🔴]")));
        //updateLessons(courses, user);
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
        logMessage("/auto_prenota", chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (!optionalUser.isPresent())
            return new SendMessage(chat.id(), "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start");
        final User user = optionalUser.get();
        if (checkCommandRunning(user))
            return new SendMessage(chat.id(), "Non puoi usare un comando adesso!");
        if (user.getPassword() == null || user.getMatricola() == null)
            return new SendMessage(chat.id(), "Non è stato effettuato il login.\n" +
                    "Inserire /matricola e /password");
        user.setAddingAutoBooking(true);
        userRepository.save(user);
        messanger.sendMessageTo(chat.id(), "Ricerca delle lezioni in corso...");
        final List<String> courses = booker.getCoursesName(user.getMatricola(), user.getPassword());
        final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Lista Corsi"));
        courses.forEach(e -> lessonsMenu.addRow(e));
        final SendMessage request = new SendMessage(user.getChadId(),
                "Seleziona i corsi da prenotare automaticamente.\n"
                        + "Quando hai finito digita 'FINE' per arrestare il processo di selezione e confermare le tue scelte.")
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyMarkup(lessonsMenu);
        return request;
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
        if (optionalUser.isEmpty())
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        final User user = optionalUser.get();
        if (checkCommandRunning(user))
            return "Non puoi usare un comando adesso!";
        String userData = "I tuoi dati: \n" +
                "- Matricola: " + user.getMatricola() + "\n" +
                "- Password: " + user.getPassword() + "\n" +
                "Procedura di prenotazione automatica: " + (user.isAutoBooking() ? "ON\n" : "OFF\n");
        final List<LessonToBook> lessonsToBook = lessonToBookRepository.findByChatId(chat.id());
        if (!lessonsToBook.isEmpty()) {
            userData += "Corsi abilitati per la prenotazione automatica:\n";
            for (LessonToBook lessonToBook : lessonsToBook) {
                userData += lessonToBook.getCourseName() + "\n";
            }
        }
        return userData + "\n\nQUESTE INFORMAZIONI SONO VISIBILI SOLO A TE";
    }

    /**
     * Method to remove one user
     *
     * @param chat The rapresentation of the chat with the user
     * @return The data of the user
     */
    @MessageRequest("/stop")
    public String autoPrenotaStopped(Chat chat) {
        logMessage("/stop", chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isEmpty())
            return "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start";
        final User user = optionalUser.get();
        if (checkCommandRunning(user))
            return "Non puoi usare un comando adesso!";
        if (user.isAutoBooking()) {
            user.setAutoBooking(false);
            userRepository.save(user);
            return "Sistema di prenotazione automatica arrestato";
        } else {
            return "Sistema di prenotazione automatica non attivo";
        }
    }

    /**
     * Method to remove one user
     *
     * @param chat The rapresentation of the chat with the user
     * @return The data of the user
     */
    @MessageRequest("/rimuovi_corso")
    public BaseRequest autoBookingCoursesRemove(Chat chat) {
        logMessage("/rimuovi_corso", chat.id());
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isEmpty())
            return new SendMessage(chat.id(), "Utente non registrato!\n" +
                    "Per favore reinizializza il bot con il comando /start");
        final User user = optionalUser.get();
        if (checkCommandRunning(user)) {
            return new SendMessage(chat.id(), "Non puoi usare un comando adesso!");
        }
        if (user.getMatricola() == null || user.getPassword() == null)
            return new SendMessage(chat.id(), "Non è stato effettuato il login.\n" +
                    "Inserire /matricola e /password");
        user.setRemovingAutoBooking(true);
        userRepository.save(user);
        final List<LessonToBook> lessonsToBook = lessonToBookRepository.findByChatId(chat.id());
        final List<String> courses = new LinkedList<>();
        lessonsToBook.forEach(e -> courses.add(e.getCourseName()));
        final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Lista Corsi"));
        courses.forEach(e -> lessonsMenu.addRow(e));
        return new SendMessage(chat.id(),
                "Seleziona i corsi da rimuovere dalla prenotazione automatica.\n"
                        + "Quando hai finito digita 'FINE' per arrestare il processo di selezione e confermare le tue scelte.")
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyMarkup(lessonsMenu);
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
            if (checkCommandRunning(optionalUser.get()))
                return "Non puoi utilizzare un comando adesso!";
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
        if (user.isAddingMatricola()) {
            if (isMatricolaValid(message)) {
                user.setMatricola(message);
                user.setAddingMatricola(false);
                userRepository.save(user);
                log.info("Utente salvato {}", user);
                return new SendMessage(chat.id(), "Matricola " + message + " salvata");
            } else {
                return new SendMessage(chat.id(), "Matricola non valida");
            }
        }

        // Adding password
        else if (user.isAddingPassword()) {
//          user.setPassword(passwordEncoder.encode(password));
            user.setPassword(message);
            user.setAddingPassword(false);
            userRepository.save(user);
            return new SendMessage(chat.id(), "Password cifrata e salvata con successo");
        }

        // Start autoBooking procedure
        else if (user.isAddingAutoBooking()) {
            if (message.equals("FINE")) {
                user.setAddingAutoBooking(false);
                user.setAutoBooking(true);
                userRepository.save(user);
                return new SendMessage(chat.id(), "Procedura di auto prenotazione attivata!");
            }
            LessonToBook lessonToBook = LessonToBook
                    .builder()
                    .courseName(message)
                    .chatId(chat.id())
                    .build();
            lessonToBookRepository.save(lessonToBook);
            return new SendMessage(chat.id(), "Corso " + message + " aggiunto correttamente!\n" +
                    "Per terminare digita 'FINE', altrimenti scegli un altro corso.");
        }

        // Removing courses from auto booking
        else if (user.isRemovingAutoBooking()) {
            if (message.equals("FINE")) {
                user.setRemovingAutoBooking(false);
                userRepository.save(user);
                return new SendMessage(chat.id(),"Operazione completata!");
            }
            final LessonToBook lessonToBook = LessonToBook
                    .builder()
                    .courseName(message)
                    .chatId(chat.id())
                    .build();
            lessonToBookRepository.delete(lessonToBook);
            return new SendMessage(chat.id(), "Corso " + message + " rimosso correttamente!\n" +
                    "Per terminare digita 'FINE', altrimenti scegli un altro corso.");
        }

        // Choosing and book lesson
        else {
            if (isLessonWrongFormat(message)) {
                return new SendMessage(chat.id(), "Comando non disponibile");
            }
            messanger.sendMessageTo(chat.id(), "Elaborazione...");
            final List<Lesson> courses = booker.book(user.getMatricola(), user.getPassword(), message);
            final ReplyKeyboardMarkup lessonsMenu = new ReplyKeyboardMarkup(new KeyboardButton("Lista Corsi"));
            courses.forEach(e -> lessonsMenu.addRow(e.getCourseName() + " - " + e.getDate() + " " + (e.isBooked() ? "[🟢]" : "[🔴]")));
            //updateLessons(courses., user);
            final Lesson lesson = courses.stream().filter(e -> (e.getCourseName() + " - " + e.getDate() + " " + (!e.isBooked() ? "[🟢]" : "[🔴]")).equals(message)).findFirst().get();
            final SendMessage request = new SendMessage(user.getChadId(), lesson.isBooked() ? "Lezione prenotata" : "Prenotazione annullata")
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

    private boolean checkCommandRunning(User user) {
        return user.isRemovingAutoBooking() || user.isAddingAutoBooking() || user.isAddingMatricola() || user.isAddingPassword();
    }

    @Async
    @Scheduled(fixedDelay = 30000)
    private void autoBooking() {
        userRepository.findAll().forEach(u -> {
            if (u.isAutoBooking()) {
                final List<LessonToBook> lessonsToBook = lessonToBookRepository.findByChatId(u.getChadId());
                final List<String> lessonsName = new LinkedList<>();
                lessonsToBook.forEach(e -> lessonsName.add(e.getCourseName()));
                messanger.sendMessageTo(u.getChadId(), "Ho prenotato " + booker.autoBook(u.getMatricola(), u.getPassword(), lessonsName) + " lezioni!");
            }
        });
    }
}
