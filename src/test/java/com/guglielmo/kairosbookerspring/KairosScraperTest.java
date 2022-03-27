package com.guglielmo.kairosbookerspring;

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.guglielmo.kairosbookerspring.api.response.pojo.LessonsResponse;
import com.guglielmo.kairosbookerspring.api.response.pojo.Prenotazioni;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KairosScraperTest {

    private KairosScraper kairosScraper = new KairosScraper();

    @Test
    public void loginAndGetBookingsTest() throws IOException, InterruptedException {
        final List<LessonsResponse> allUserLessons = kairosScraper
                .getBookings("7029444", "Mafaldo2000!");
        assertThat(allUserLessons).isNotEmpty();
    }

    @Test
    public void testGetCookies() throws IOException {
        String username = "7032141";
        String password = "c1p80040";
        final Set<Cookie> loginCookies = kairosScraper.getLoginCookies(username, password);
        log.info("Cookies: {}",loginCookies);
    }

    @Test
    public void book() throws IOException, InterruptedException {
        String username = "7032141";
        String password = "c1p80040";

        final Prenotazioni firstBookableLesson = kairosScraper.getBookings(username, password)
                .stream()
                .map(LessonsResponse::getPrenotazioni)
                .flatMap(Collection::stream)
                .filter(e -> e.getPrenotabile() && !e.getPrenotata())
                .findFirst()
                .orElseThrow();
        log.info("Lezione da prenotare: {}", firstBookableLesson);
        //assertThat(bookerScraper.bookLessons(username, password, bookerScraper.getCodiceFiscale(username, password), List.of(firstBookableLesson))).isTrue();
    }

    @Test
    public void getCodiceFiscale() throws IOException {
        String username = "7032141";
        String password = "c1p80040";

        final String codiceFiscale = kairosScraper.getCodiceFiscale(username, password);
        assertThat(codiceFiscale).isNotNull();
        log.info("Codice fiscale: {}", codiceFiscale);
    }

    @Test
    public void cancelBooking() throws IOException, InterruptedException {
        String username = "7032141";
        String password = "c1p80040";

        final Prenotazioni firstBookableLesson = kairosScraper.getBookings(username, password)
                .stream()
                .map(LessonsResponse::getPrenotazioni)
                .flatMap(Collection::stream)
                .filter(e -> e.getPrenotabile() && e.getPrenotata())
                .skip(3)
                .findFirst()
                .orElseThrow();
        log.info("Lezione da prenotare: {}", firstBookableLesson);
        //assertThat(bookerScraper.cancelBooking(username, password, bookerScraper.getCodiceFiscale(username, password), List.of(firstBookableLesson))).isTrue();
    }

    @Test
    public void testScegliCorsi() throws IOException, InterruptedException {
        String username = "7029444";
        String password = "Mafaldo2000!";
        final List<String> coursesName = kairosScraper.getCoursesName(username, password);
        assertThat(coursesName).isNotEmpty();
        log.info("Courses: {}", coursesName);
    }
}
