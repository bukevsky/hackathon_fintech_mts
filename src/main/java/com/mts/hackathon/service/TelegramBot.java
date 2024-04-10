package com.mts.hackathon.service;


import com.mts.hackathon.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;

    static final String START_TEXT = """
            Привет, это бот-помощник в выборе игры :) \n
            У нас есть такие команды: \n
            /genres <какой то текст> \n
            /publisher <какой то текст> \n
            /games <какой то текст> \n
            """;

    static final String ERROR_TEXT = "Error occurred: ";
    
    @Autowired
    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Старт бота"));
        listOfCommands.add(new BotCommand("/genres", "show game with genres"));
        listOfCommands.add(new BotCommand("/publisher", "show publisher games"));
        listOfCommands.add(new BotCommand("/games", "show games"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        switch (messageText) {
            case "/start":
                prepareAndSendMessage(chatId, START_TEXT);
                break;
            case "/publisher":
                break;
            case "/games":
                break;
            case "/genres":
                break;
            default:
                prepareAndSendMessage(chatId, "Извини, такой команды у нас нет :(");
        }
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

}
