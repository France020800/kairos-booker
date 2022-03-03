package com.guglielmo.kairosbookerspring.bot;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;

@BotController
public class KairosBot implements TelegramMvcController {
    @Override
    public String getToken() {
        return null;
    }
}
