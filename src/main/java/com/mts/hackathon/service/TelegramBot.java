package com.mts.hackathon.service;


import com.mts.hackathon.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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
@Service
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final SearchService searchService;

    private enum UserState {
        IDLE, AWAITING_START_MESSAGE
    }

    private UserState currentState = UserState.IDLE;

    static final String START_TEXT = """
            Привет, это бот-помощник в выборе игры :) \n
            У нас есть такие команды: \n
            /genres <какой то текст> \n
            /publisher <какой то текст> \n
            /games <какой то текст> \n
            """;

    static final String ERROR_TEXT = "Error occurred: ";

    @Autowired
    public TelegramBot(BotConfig config, SearchService searchService) {
        this.config = config;
        this.searchService = searchService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Старт бота"));
        listOfCommands.add(new BotCommand("/topGames", "Show top 5 games by critic"));
        listOfCommands.add(new BotCommand("/games", "Show games"));
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            switch (currentState) {
                case IDLE:
                    handleIdleState(update.getMessage().getChatId(), messageText);
                    break;
                case AWAITING_START_MESSAGE:
                    handleAwaitingStartMessageState(update.getMessage().getChatId(), messageText);
                    break;
            }
        }
    }

    private void handleIdleState(Long chatId, String messageText) {
        switch (messageText) {
            case "/start":
                prepareAndSendMessage(chatId, START_TEXT);
                currentState = UserState.IDLE;
                break;
            case "/games":
                currentState = UserState.AWAITING_START_MESSAGE;
                prepareAndSendMessage(chatId, "Напиши название интересующей тебя игры");
                break;
            case "/genres":
                currentState = UserState.AWAITING_START_MESSAGE;
                prepareAndSendMessage(chatId, "Напиши жанр интересующей тебя игры");
            default:
                prepareAndSendMessage(chatId, "Извини, такой команды нет :(");
        }
    }

    private void handleAwaitingStartMessageState(Long chatId, String messageText) {
        currentState = UserState.IDLE;
        prepareAndSendMessage(chatId, searchService.getGameByName(messageText));
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

}
