package com.guglielmo.kairosbookerspring;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

@Slf4j
/**
 * This class contains the booking logic
 */
public class Booker {

    private WebDriver driver;
    private WebDriverWait wait;

    private List<WebElement> loginAndGetBookings(String username, String passsword) {
        String kairosFormPage = "https://kairos.unifi.it/agendaweb/index.php?view=login&include=login&from=prenotalezione&from_include=prenotalezione&_lang=en";
        String kairosBookingPage = "https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione&_lang=it";


        driver.get(kairosFormPage);


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
        return bookingsList;
    }

    public List<Lesson> getCourses(String username, String passsword) {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        final List<WebElement> bookingsList = loginAndGetBookings(username, passsword);

        final List<Lesson> lessonsList = new LinkedList<>();

        for (WebElement booking : bookingsList) {
            final WebElement bookingDate = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-header > span.box-header-big"));
            final WebElement bookingInfo = booking.findElement(By.xpath("//div[2]/div/div[2]"));
            final WebElement courseName = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final WebElement bookingStatus = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));
//            final WebElement bookingRoom = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > b"));


            final Lesson lesson = Lesson.builder()
                    .courseName(courseName.getText())
                    .date(bookingDate.getText())
                    .isBooked(!bookingStatus.getText().isEmpty())
                    .build();

            lessonsList.add(lesson);
            log.info("Booking date: " + bookingDate.getText() + "\n" +
                    "Booking info: " + bookingInfo.getText() + "\n");
        }


        driver.close();
        return lessonsList;
    }


    public void book(String username, String password, String lesson) {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());

        final List<WebElement> bookingsList = loginAndGetBookings(username, password);

        final WebElement lessonToBook = bookingsList.stream().filter(e -> {
            final WebElement bookingDate = e.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-header > span.box-header-big"));
            final WebElement courseName = e.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final WebElement bookingStatus = e.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));
            return lesson.equals(courseName.getText() + " - " + bookingDate.getText() + " " + !bookingStatus.getText().isEmpty());
        }).findFirst().orElseThrow();

        //Click the booking button
        wait.until(ExpectedConditions.elementToBeClickable(lessonToBook.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > a")))).click();
        driver.close();
    }

}
