package com.guglielmo.kairosbookerspring.bot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KairosBotMessangerTest {


    @Test
    public void testSendAllMessage() {
        new KairosBotMessanger(null).sendMessageToAllUsers("Ciao");
    }


    @Test
    void sendMessageTo() {
    }
}