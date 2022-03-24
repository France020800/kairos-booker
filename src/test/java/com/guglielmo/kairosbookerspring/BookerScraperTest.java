package com.guglielmo.kairosbookerspring;

import com.guglielmo.kairosbookerspring.api.response.pojo.LessonsResponse;
import com.guglielmo.kairosbookerspring.api.response.pojo.Prenotazioni;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BookerScraperTest {

    private BookerScraper bookerScraper = new BookerScraper();

    @Test
    public void loginAndGetBookingsTest() throws IOException, InterruptedException {
        final List<LessonsResponse> allUserLessons = bookerScraper
                .loginAndGetBookings("7032141", "c1p80040");
        assertThat(allUserLessons).isNotEmpty();
    }

    @Test
    public void book() throws IOException, InterruptedException {
        String username = "7032141";
        String password = "c1p80040";

        final Prenotazioni firstBookableLesson = bookerScraper.loginAndGetBookings(username, password)
                .stream()
                .map(LessonsResponse::getPrenotazioni)
                .flatMap(Collection::stream)
                .filter(e -> e.getPrenotabile() && !e.getPrenotata())
                .findFirst()
                .orElseThrow();
        log.info("Lezione da prenotare: {}", firstBookableLesson);
        assertThat(bookerScraper.bookLessons(username, password, bookerScraper.getCodiceFiscale(username, password), List.of(firstBookableLesson))).isTrue();
    }

    @Test
    public void getCodiceFiscale() throws IOException {
        String username = "7032141";
        String password = "c1p80040";

        final String codiceFiscale = bookerScraper.getCodiceFiscale(username, password);
        assertThat(codiceFiscale).isNotNull();
        log.info("Codice fiscale: {}", codiceFiscale);
    }

    @Test
    public void cancelBooking() throws IOException, InterruptedException {
        String username = "7032141";
        String password = "c1p80040";

        final Prenotazioni firstBookableLesson = bookerScraper.loginAndGetBookings(username, password)
                .stream()
                .map(LessonsResponse::getPrenotazioni)
                .flatMap(Collection::stream)
                .filter(e -> e.getPrenotabile() && e.getPrenotata())
                .skip(3)
                .findFirst()
                .orElseThrow();
        log.info("Lezione da prenotare: {}", firstBookableLesson);
        assertThat(bookerScraper.cancelBooking(username, password, bookerScraper.getCodiceFiscale(username, password), List.of(firstBookableLesson))).isTrue();
    }
}
