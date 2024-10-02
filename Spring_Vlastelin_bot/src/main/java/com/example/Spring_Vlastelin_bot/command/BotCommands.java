package com.example.Spring_Vlastelin_bot.command;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;

public class BotCommands {
    public List<BotCommand> setBotCommands() {

        List<BotCommand> botCommands = new ArrayList<>();


        botCommands.add(new BotCommand("/start", "Запустить бота"));
        botCommands.add(new BotCommand("/help", "Получить помощь"));
        botCommands.add(new BotCommand("/menu", "Показать главное меню"));


        return botCommands;

        }
    }