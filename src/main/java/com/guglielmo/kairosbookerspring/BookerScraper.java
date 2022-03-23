package com.guglielmo.kairosbookerspring;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.guglielmo.kairosbookerspring.api.response.pojo.LessonsResponse;
import com.guglielmo.kairosbookerspring.api.response.pojo.Prenotazioni;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BookerScraper {

    private final WebClient webClient;

    public BookerScraper() {
        this.webClient = new WebClient();
        final WebClientOptions options = webClient.getOptions();
        options.setJavaScriptEnabled(false);
        options.setCssEnabled(false);
    }

    protected List<DomNode> loginAndGetBookings(String username, String password) throws IOException, InterruptedException {
        String kairosFormPage = "https://kairos.unifi.it/agendaweb/index.php?view=login&include=login&from=prenotalezione&from_include=prenotalezione&_lang=en";
        String kairosBookingPage = "https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione&_lang=it";

        final HtmlPage page = webClient.getPage(kairosFormPage);

        // Click conditions buttons
        String privacySliderSelector = "#main-content > div.main-content-body > div.container > div:nth-child(2) > div.col-lg-6 > div > div:nth-child(5) > div.col-xs-3 > label > span";
        String rulesSliderSelector = "#main-content > div.main-content-body > div.container > div:nth-child(2) > div.col-lg-6 > div > div:nth-child(6) > div.col-xs-3 > label > span";
        final HtmlElement privacySlider = page.querySelector(privacySliderSelector);
        final HtmlElement rulesSlider = page.querySelector(rulesSliderSelector);
        privacySlider.click();
        rulesSlider.click();

        // Click login button
        final HtmlElement login = page.querySelector("#oauth_btn");
        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage loginPage = login.click();

        // Insert username e password
        final HtmlForm loginForm = loginPage.querySelector("body > div > div > div > div.column.one > form");
        loginForm.getInputByName("j_username").setValueAttribute(username);
        loginForm.getInputByName("j_password").setValueAttribute(password);

        // Login button
        final HtmlElement loginButton = loginForm.querySelector("body > div > div > div > div.column.one > form > div:nth-child(5) > button");
        final HtmlPage optionsPage = loginButton.click();

        final Set<Cookie> cookies = webClient.getCookieManager().getCookies();

        HttpResponse<String> response = Unirest.get("https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione&_lang=it")
                .header("Connection", "keep-alive")
                .header("Pragma", "no-cache")
                .header("Cache-Control", "no-cache")
                .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"macOS\"")
                .header("DNT", "1")
                .header("Upgrade-Insecure-Requests", "1")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.83 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-User", "?1")
                .header("Sec-Fetch-Dest", "document")
                .header("Referer", "https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione_home&_lang=it")
                .header("Accept-Language", "en-US,en;q=0.9,it;q=0.8,ja;q=0.7")
                .header("Cookie", cookies.stream().map(e -> e.getName() + "=" + e.getValue()).collect(Collectors.joining(";")))
                .header("sec-gpc", "1")
                .asString();

        String jsonLine = response.getBody().lines().filter(e -> e.trim().startsWith("var lezioni_prenotabili")).collect(Collectors.joining());
        jsonLine = jsonLine.substring(" \t\t\tvar lezioni_prenotabili = JSON.parse(".length());
        jsonLine = jsonLine.substring(0, jsonLine.length() - 4);

        final LessonsResponse[] lessonsResponses = new Gson().fromJson(jsonLine, LessonsResponse[].class);

        final List<Prenotazioni> allLessons = Arrays.stream(lessonsResponses).map(LessonsResponse::getPrenotazioni).flatMap(Collection::stream).collect(Collectors.toList());

        log.info("Lessons: {}", allLessons);

//        final List<DomNode> bookingList = bookingsDiv.querySelectorAll(".col-md-6");

        return null;
    }

}
