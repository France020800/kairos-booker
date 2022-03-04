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
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BotController
@Slf4j
public class KairosBotRequestHandler implements TelegramMvcController {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private Booker booker;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    public KairosBotRequestHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.booker = new Booker();
    }

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

    @MessageRequest("{lesson:.*}")
    public String bookLesson(@BotPathVariable("lesson") String lesson, Chat chat) {
        if (isLessonWrongFormat(lesson)) {
            return "Comando non disponibile";
        }
        String courseName = getCourseName(lesson);
        final Optional<User> optionalUser = userRepository.findByChadId(chat.id());
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            booker.book(user.getMatricola(), user.getPassword(), lesson);
            return "Lezione Prenotata";
        }
        return "Impossibile prenotare la lezione, accedi";
    }

    String getCourseName(String lesson) {
        final Matcher matcher = Pattern.compile("([A-Z]* )*").matcher(lesson);
        if (matcher.find()) {
            return matcher.group(0).trim();
        }
        throw new IllegalArgumentException("Lesson format not valid");
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
