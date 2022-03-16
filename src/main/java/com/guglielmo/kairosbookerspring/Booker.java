package com.guglielmo.kairosbookerspring;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

@Slf4j
/**
 * This class contains the booking logic
 */
public class Booker {

    private WebDriverWait wait;
    private ChromeOptions chromeOptions;

    public Booker() {
        this.chromeOptions = new ChromeOptions();
        this.chromeOptions.addArguments("--headless");
    }


    List<WebElement> loginAndGetBookings(String username, String passsword) {
//        getSessionCookie(username,passsword);
        WebDriver driver = initBrowser(new Cookie("PHPSESSID", "gere13urslboqkfn6r4ju6o984"));

        String kairosBookingPage = "https://kairos.unifi.it/agendaweb/index.php?view=prenotalezione&include=prenotalezione&_lang=it";
        driver.get(kairosBookingPage);

        final WebElement bookingsDiv = driver.findElement(By.cssSelector("#prenotazioni_container"));

        final List<WebElement> bookingsList = bookingsDiv.findElements(By.className("col-md-6"));
        return bookingsList;
    }

    private WebDriver initBrowser(Cookie sessionCookie) {
        WebDriver driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        driver.get("https://kairos.unifi.it/agendaweb/");
        driver.manage().deleteAllCookies();
        driver.manage().addCookie(sessionCookie);
        return driver;
    }

    @NotNull
    Cookie getSessionCookie(String username, String passsword) {
        WebDriver driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        String kairosFormPage = "https://kairos.unifi.it/agendaweb/index.php?view=login&include=login&from=prenotalezione&from_include=prenotalezione&_lang=en";

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
        final Set<Cookie> cookies = driver.manage().getCookies();
        driver.close();
        return cookies.stream().filter(e -> e.getName().equals("PHPSESSID")).findFirst().get();
    }

    public List<Lesson> getCourses(String username, String password) {
        WebDriver driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        final List<WebElement> bookingsList = loginAndGetBookings(username, password);

        final List<Lesson> lessonsList = new LinkedList<>();

        for (WebElement booking : bookingsList) {
            final WebElement bookingDate = booking.findElement(By.cssSelector("div.col-md-6 > div > div.colored-box-header > span.box-header-big"));
            final List<WebElement> coursesNameList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final WebElement bookingInfo = booking.findElement(By.xpath("//div[2]/div/div[2]"));
            final List<WebElement> bookingsStatusList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));

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
        WebDriver driver = new ChromeDriver(chromeOptions);
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

    public int autoBook(String username, String password, List<String> lessonsToBook) {
        WebDriver driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());

        int numberOfBookings = 0;
        final List<WebElement> bookingsList = loginAndGetBookings(username, password);
        for (WebElement booking : bookingsList) {
            final List<WebElement> coursesNameList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            final List<WebElement> bookingsStatusList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.attendance-course-detail"));
            for (WebElement courseName : coursesNameList) {
                boolean isNotBooked = bookingsStatusList.get(coursesNameList.indexOf(courseName)).getText().isEmpty();
                if (lessonsToBook.contains(courseName.getText()) && isNotBooked) {
                    WebElement lesson = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > a")).get(coursesNameList.indexOf(courseName));
                    lesson.click();
                    final WebElement confirmButton = wait
                            .until(ExpectedConditions
                                    .elementToBeClickable(By
                                            .cssSelector("#popup_conferma_buttons_row > button")));
                    confirmButton.click();
                    numberOfBookings++;
                }
            }
        }
        driver.close();
        return numberOfBookings;
    }

    public List<String> getCoursesName(String username, String password) {
        WebDriver driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds());
        final List<WebElement> bookingsList = loginAndGetBookings(username, password);

        final List<String> coursesName = new LinkedList<>();
        for (WebElement booking : bookingsList) {
            final List<WebElement> coursesNameList = booking.findElements(By.cssSelector("div.col-md-6 > div > div.colored-box-section-1 > span.libretto-course-name"));
            for (WebElement courseName : coursesNameList) {
                coursesName.add(courseName.getText());
            }
        }
        driver.close();
        return new LinkedList<>(new LinkedHashSet<>(coursesName));
    }

    private Lesson createLesson(WebElement bookingDate, List<WebElement> coursesNameList, List<WebElement> bookingsStatusList, WebElement courseName) {
        return Lesson.builder()
                .courseName(courseName.getText())
                .date(bookingDate.getText())
                .isBooked(!bookingsStatusList.get(coursesNameList.indexOf(courseName)).getText().isEmpty())
                .build();
    }

}