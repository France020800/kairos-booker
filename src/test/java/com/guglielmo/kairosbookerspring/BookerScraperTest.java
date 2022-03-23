package com.guglielmo.kairosbookerspring;

import com.guglielmo.kairosbookerspring.api.response.pojo.Prenotazioni;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BookerScraperTest {

    private BookerScraper bookerScraper = new BookerScraper();

    @Test
    public void loginAndGetBookingsTest() throws IOException, InterruptedException {
        final List<Prenotazioni> allUserLessons = bookerScraper
                .loginAndGetBookings("7032141", "c1p80040");
        assertThat(allUserLessons).isNotEmpty();
    }
}
