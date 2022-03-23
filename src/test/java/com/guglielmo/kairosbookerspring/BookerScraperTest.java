package com.guglielmo.kairosbookerspring;

import com.gargoylesoftware.htmlunit.html.DomNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

@Slf4j
public class BookerScraperTest {

    @Test
    public void loginAndGetBookingsTest() throws IOException, InterruptedException {
        BookerScraper booker = new BookerScraper();
//        Booker bookeSelenium = new Booker();
        List<DomNode> lessons = booker.loginAndGetBookings("7032141", "c1p80040");
//        List<WebElement> lessonsSelenium = bookeSelenium.loginAndGetBookings("", "");
        log.info("Lessons: {}", lessons);
    }
}
