package com.guglielmo.kairosbookerspring.bot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KairosBotMessangerTest {

    @Autowired
    private KairosBotMessanger kairosBotMessanger;

    @Test
    public void testSendAllMessage(){
        kairosBotMessanger.sendMessageToAllUsers("Ciao");
    }


}