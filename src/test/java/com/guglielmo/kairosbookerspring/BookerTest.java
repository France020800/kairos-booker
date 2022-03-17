package com.guglielmo.kairosbookerspring;

import org.junit.Test;
import org.openqa.selenium.Cookie;

import static org.assertj.core.api.Assertions.assertThat;

public class BookerTest {

    @Test
    public void loginAndGetBookings() {
        assertThat(new Booker().loginAndGetBookings("7032141", "c1p80040")).isNotEmpty();
    }

    @Test
    public void login() {
        final Cookie cookie = new Booker().getSessionCookie("7032141", "c1p80040");
        System.out.println(cookie);
        assertThat(cookie.getName()).isEqualTo("PHPSESSID");
    }
}