package com.guglielmo.kairosbookerspring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookerTest {

    @Test
    void getCourses() {
        new Booker().getCourses("7032141","c1p80040");
    }
}