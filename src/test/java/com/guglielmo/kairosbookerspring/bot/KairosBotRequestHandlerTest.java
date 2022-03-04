package com.guglielmo.kairosbookerspring.bot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class KairosBotRequestHandlerTest {

    @Test
    public void isLessonWrongFormat(){
        final KairosBotRequestHandler kairosBotRequestHandler = new KairosBotRequestHandler(null, null);
        String lesson="CALCOLO NUMERICO - Mercoledì 9 Marzo 2022 false";
        assertThat(kairosBotRequestHandler.isLessonWrongFormat(lesson)).isFalse();
    }

}