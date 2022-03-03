package com.guglielmo.kairosbookerspring;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@Slf4j
public class Booker {

    private WebDriver driver;

    public Booker() {
        driver = new ChromeDriver();

    }
    public void book(String username, String passsword) {

        String kairosFormPage = "https://kairos.unifi.it/agendaweb/index.php?view=login&include=login&from=prenotalezione&from_include=prenotalezione&_lang=en";
        String kairosBookingPage = "https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione&_lang=it";


        driver.get(kairosFormPage);


        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());


        //Click conditions buttons
        String privacySliderSelector = "#main-content > div.container > div:nth-child(2) > div.col-lg-6 > div > div:nth-child(5) > div.col-xs-3 > label > span";
        String rulesSliderSelector = "#main-content > div.container > div:nth-child(2) > div.col-lg-6 > div > div:nth-child(6) > div.col-xs-3 > label > span";
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(privacySliderSelector))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(rulesSliderSelector))).click();

        //Click login button
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#oauth_btn"))).click();

        final WebElement usernameInput = driver.findElement(By.cssSelector("#username"));
        final WebElement passwordInput = driver.findElement(By.cssSelector("#password"));

        //Insert username and password
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(passsword);

        //Login button
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("body > div > div > div > div.column.one > form > div:nth-child(5) > button"))).click();
        wait.until(ExpectedConditions.titleContains("Agenda Web"));
        driver.get(kairosBookingPage);

        final WebElement bookingsDiv = driver.findElement(By.cssSelector("#prenotazioni_container"));

        final List<WebElement> bookingsList = bookingsDiv.findElements(By.cssSelector(".row"));

        for (WebElement booking : bookingsList) {
            final WebElement bookingDate = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-header > span.box-header-big"));
            final WebElement bookingInfo = booking.findElement(By.xpath("//div[2]/div/div[2]"));
//            final WebElement courseName = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
//            final WebElement bookingRoom = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > b"));


            log.info("Booking date: " + bookingDate.getText() + "\n" +
                    "Booking info: " + bookingInfo.getText() + "\n" );
        }


//        driver.quit();

    }

}
