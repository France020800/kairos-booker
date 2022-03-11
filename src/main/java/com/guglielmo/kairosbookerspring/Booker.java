package com.guglielmo.kairosbookerspring;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
    private ChromeOptions chromeOptions;

    public Booker(){
        this.chromeOptions=new ChromeOptions();
        this.chromeOptions.addArguments("--headless");
    }


    private List<WebElement> loginAndGetBookings(String username, String passsword) {
        String kairosFormPage = "https://kairos.unifi.it/agendaweb/index.php?view=login&include=login&from=prenotalezione&from_include=prenotalezione&_lang=en";
        String kairosBookingPage = "https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione&_lang=it";


        driver.get(kairosFormPage);


        //Click conditions buttons
        String privacySliderSelector = "#main-content > div.main-content-body > div.container > div:nth-child(2) > div.col-lg-6 > div > div:nth-child(5) > div.col-xs-3 > label > span";
        String rulesSliderSelector = "#main-content > div.main-content-body > div.container > div:nth-child(2) > div.col-lg-6 > div > div:nth-child(6) > div.col-xs-3 > label > span";
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
        wait.until(ExpectedConditions.titleContains("Prenota il tuo posto"));
        driver.get(kairosBookingPage);

        final WebElement bookingsDiv = driver.findElement(By.cssSelector("#prenotazioni_container"));

        final List<WebElement> bookingsList = bookingsDiv.findElements(By.className("col-md-6"));
        return bookingsList;
    }

    public List<Lesson> getCourses(String username, String password) {
        driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        final List<WebElement> bookingsList = loginAndGetBookings(username, password);

        final List<Lesson> lessonsList = new LinkedList<>();

        for (WebElement booking : bookingsList) {
            final WebElement bookingDate = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-header > span.box-header-big"));
            final List<WebElement> coursesNameList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final WebElement bookingInfo = booking.findElement(By.xpath("//div[2]/div/div[2]"));
//          final WebElement courseName = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final List<WebElement> bookingsStatusList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));
//          final WebElement bookingRoom = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > b"));

            for (WebElement courseName : coursesNameList) {
                final Lesson lesson = createLesson(bookingDate, coursesNameList, bookingsStatusList, courseName);
                lessonsList.add(lesson);
                log.info("Booking date: " + bookingDate.getText() + "\n" +
                        "Booking info: " + bookingInfo.getText() + "\n");
            }
        }

        driver.close();
        return lessonsList;
    }


    public List<Lesson> book(String username, String password, String lesson) {
        driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());

        final List<WebElement> bookingsList = loginAndGetBookings(username, password);
        final List<Lesson> lessonsList = new LinkedList<>();

        WebElement lessonToBook = null;
        for (WebElement booking : bookingsList) {
            final WebElement bookingDate = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-header > span.box-header-big"));
            final List<WebElement> coursesNameList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final List<WebElement> bookingsStatusList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));
            for (WebElement courseName : coursesNameList) {
                final Lesson lessonObject = createLesson(bookingDate, coursesNameList, bookingsStatusList, courseName);
                boolean isBooked = !bookingsStatusList.get(coursesNameList.indexOf(courseName)).getText().isEmpty();
                if (lesson.equals(courseName.getText() + " - " + bookingDate.getText() + " " + (isBooked ? "[🟢]" : "[🔴]"))) {
                    lessonToBook = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > a")).get(coursesNameList.indexOf(courseName));
                    lessonToBook.click();
                    if (isBooked) {
                        final WebElement confirmButton = wait
                                .until(ExpectedConditions
                                        .elementToBeClickable(By
                                                .cssSelector("#popup_conferma_buttons_row > button.btn.normal-button.custom-btn-confirm")));
                        confirmButton.click();
                        lessonObject.setBooked(false);
                    } else {
                        lessonObject.setBooked(true);

                    }
                }
                lessonsList.add(lessonObject);
            }
        }

        driver.close();
        return lessonsList;
    }

    public int autoBook(String username, String password, String lessonToBook) {
        driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());

        int numberOfBookings = 0;
        final List<WebElement> bookingsList = loginAndGetBookings(username, password);
        for (WebElement booking : bookingsList) {
            final List<WebElement> coursesNameList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final List<WebElement> bookingsStatusList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));
            for (WebElement courseName :  coursesNameList) {
                boolean notBooked = bookingsStatusList.get(coursesNameList.indexOf(courseName)).getText().isEmpty();
                if (courseName.equals(lessonToBook) && notBooked) {
                    WebElement lesson = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > a")).get(coursesNameList.indexOf(courseName));
                    lesson.click();
                    numberOfBookings++;
                }
            }
        }
        return numberOfBookings;
    }

    private Lesson createLesson(WebElement bookingDate, List<WebElement> coursesNameList, List<WebElement> bookingsStatusList, WebElement courseName) {
        return Lesson.builder()
                .courseName(courseName.getText())
                .date(bookingDate.getText())
                .isBooked(!bookingsStatusList.get(coursesNameList.indexOf(courseName)).getText().isEmpty())
                .build();
    }

}