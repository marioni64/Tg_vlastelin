package com.example.Spring_Vlastelin_bot.service;

import com.example.Spring_Vlastelin_bot.command.BotCommands;
import com.example.Spring_Vlastelin_bot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    final BotConfig config;

    public TelegramBot(BotConfig config){
        this.config = config;
    }



    @Override
    public void onUpdateReceived(Update update) {


        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    helpCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/menu":
                    sendMainMenu(chatId, update.getMessage().getChat().getFirstName());
                    break;

                default:
                    sendMessage(chatId, "Sorry, command was not recognized");

            }
        } else if (update.hasCallbackQuery()) {
            //Если нажата кнопка

            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callBackData){
                case "search_command":
                    sendTwoMenu(chatId);
                    break;
                case "other_command":
                    sendMessage(chatId, "Вы выбрали другое действие");
                    break;
                case "back_to_main_menu":
                    sendMainMenu(chatId, update.getCallbackQuery().getFrom().getFirstName());
                    break;
                default:
                    sendMessage(chatId, "Sorry, command was not recognized");

            }

        }

    }
    private void helpCommandReceived(long chatId, String name){
        String answer = "Помощь в использовании бота:\n" +
                "/start - Запуск бота\n" +
                "/menu - Показать меню";

        sendMessage(chatId, answer);
    }

    // Первое меню кнопок( Панель управления )
    private void sendMainMenu(long chatId, String firstName) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие:");

        // Создаем клавиатуру
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Первая кнопка - Поиск
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton searchButton = new InlineKeyboardButton();
        searchButton.setText("Поиск билетов");
        searchButton.setCallbackData("search_command");
        rowInline1.add(searchButton);

        // Вторая кнопка - Другое действие
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton otherButton = new InlineKeyboardButton();
        otherButton.setText("Другое действие");
        otherButton.setCallbackData("other_command");
        rowInline2.add(otherButton);

        // Добавляем кнопки в список
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);


        // Устанавливаем клавиатуру
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        // Отправляем сообщение
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }


    // Возвращение после нажатия обратной кнопки
    private void sendTwoMenu(long chatId){

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Меню поиска:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("Вернуться назад");
        backButton.setCallbackData("back_to_main_menu");
        rowInLine1.add(backButton);

        rowsInline.add(rowInLine1);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try{
            execute(message);
        }catch (TelegramApiException e){
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }

        BotCommands commands = new BotCommands();
        commands.setBotCommands();
    }



    // Приветствие
    private void startCommandReceived(long chatId, String name){
        String answer = "Привет, " + name + ", я для поиска билетов компании Vlastelin";


        sendMessage(chatId, answer);
    }


    // Предназначен для отправки текстовых сообщений в чат в Telegram через моего бота
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }
        catch (TelegramApiException e){

        }
    }


    //Метод для меню по "/"
    public void initBotCommands() {
        BotCommands botCommands = new BotCommands();
        List<BotCommand> commandsList = botCommands.setBotCommands();

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commandsList);

        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error("Ошибка при установке команд: " + e.getMessage());
        }
    }



    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }
}
