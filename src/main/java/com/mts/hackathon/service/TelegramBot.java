package com.mts.hackathon.service;


import com.mts.hackathon.config.BotConfig;
import com.mts.hackathon.config.UserState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final SearchService searchService;
    private final Map<Long, UserState> userStates = new HashMap<>();

    static final String START_TEXT = """
            Привет, это бот-помощник в выборе игры :)
            У нас есть такие команды:
            /genres Подскажет топ 5 игр по жанру
            /games Подскажет информацию по игре
            """;

    static final String ERROR_TEXT = "Error occurred: ";

    @Autowired
    public TelegramBot(BotConfig config, SearchService searchService) {
        this.config = config;
        this.searchService = searchService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Старт бота"));
        listOfCommands.add(new BotCommand("/genres", "Show top 5 games by genre"));
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
            Long chatId = update.getMessage().getChatId();

            UserState currentState = userStates.getOrDefault(chatId, UserState.IDLE);
            switch (currentState) {
                case IDLE:
                    handleIdleState(chatId, messageText);
                    break;
                case AWAITING_GAME_INPUT:
                    handleAwaitingGameInputState(chatId, messageText);
                    break;
                case AWAITING_GENRE_INPUT:
                    handleAwaitingGenreInputState(chatId, messageText);
                    break;
            }
        }
    }

    private void handleIdleState(Long chatId, String messageText) {
        switch (messageText) {
            case "/start":
                prepareAndSendMessage(chatId, START_TEXT);
                userStates.put(chatId, UserState.IDLE);
                break;
            case "/games":
                userStates.put(chatId, UserState.AWAITING_GAME_INPUT);
                prepareAndSendMessage(chatId, "Напиши название интересующей тебя игры");
                break;
            case "/genres":
                userStates.put(chatId, UserState.AWAITING_GENRE_INPUT);
                prepareAndSendMessage(chatId, "Напиши жанр интересующей тебя игры");
                break;
            default:
                prepareAndSendMessage(chatId, "Извини, я тебя не понял");
                break;
        }
    }

    private void handleAwaitingGameInputState(Long chatId, String messageText) {
        userStates.put(chatId, UserState.IDLE);
        prepareAndSendMessage(chatId, searchService.getGameByName(messageText));
    }
    private void handleAwaitingGenreInputState(Long chatId, String messageText) {
        userStates.put(chatId, UserState.IDLE);
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
