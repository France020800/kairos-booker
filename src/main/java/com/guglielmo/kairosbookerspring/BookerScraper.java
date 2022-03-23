package com.guglielmo.kairosbookerspring;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.List;

public class BookerScraper {

    private final WebClient webClient;

    public BookerScraper() {
        this.webClient = new WebClient();
        final WebClientOptions options = webClient.getOptions();
        options.setJavaScriptEnabled(true);
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
        HtmlPage loginPage = login.click();

        // Insert username e password
        final HtmlForm loginForm = loginPage.querySelector("body > div > div > div > div.column.one > form");
        loginForm.getInputByName("j_username").setValueAttribute(username);
        loginForm.getInputByName("j_password").setValueAttribute(password);

        // Login button
        final HtmlElement loginButton = loginForm.querySelector("body > div > div > div > div.column.one > form > div:nth-child(5) > button");
        final HtmlPage optionsPage = loginButton.click();
        System.out.println("****************************** " + optionsPage.getTitleText() + " ******************************");

        // Prenota e gestisci il tuo posto a lezione button
        final HtmlElement bookingButton = optionsPage.querySelector("#menu_container > div:nth-child(1) > div > div.colored-box-section-1 > a > div");
        final HtmlPage lessonPage = bookingButton.click();

        final HtmlElement bookingsDiv = lessonPage.querySelector("#prenotazioni_container");

        final List<DomNode> bookingList = bookingsDiv.getByXPath("//div[contains(@class, 'date')]");

        return bookingList;
    }

}
