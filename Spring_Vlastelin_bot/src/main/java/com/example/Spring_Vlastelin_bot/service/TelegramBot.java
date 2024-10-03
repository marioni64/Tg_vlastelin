package com.example.Spring_Vlastelin_bot.service;

import com.example.Spring_Vlastelin_bot.command.BotCommands;
import com.example.Spring_Vlastelin_bot.config.BotConfig;
import com.example.Spring_Vlastelin_bot.parser.App;
import com.sun.jdi.event.StepEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    App app = new App();
    final BotConfig config;

    private String selectedFromCity = null;
    private String selectedToCity = null;
    private String selectedDate;

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
                    if (isValidDate(messageText)) {
                        selectedDate = messageText;
                        searchTickets(chatId, selectedFromCity, selectedToCity);
                    } else {
                        sendMessage(chatId, "Sorry, command was not recognized");
                    }

            }
        } else if (update.hasCallbackQuery()) {
            //Если нажата кнопка

            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            switch (callBackData.split(":")[0]){
                case "search_command":
                    sendMessageWithButtonsCity(chatId, "Выберите город отправления: ", getCityButtons("from"));
                    DeleteMessageChat(chatId, messageId);
                    break;

                case "other_command":
                    sendMessage(chatId, "Вы выбрали другое действие");
                    DeleteMessageChat(chatId, messageId);
                    break;

                case "from":
                    selectedFromCity = callBackData.split(":")[1];
                    sendMessageWithButtonsCity(chatId, "Теперь выберите город назначения", getCityButtons("to"));
                    DeleteMessageChat(chatId, messageId);
                    break;

                case "to":
                    selectedToCity = callBackData.split(":")[1];
                    sendMessage(chatId, "Вы выбрали города. Пожалуйста, введите дату поездки в формате YYYY.MM.DD:");
                    DeleteMessageChat(chatId, messageId);
                    break;

                case "back_to_cities":  // Логика возврата назад
                    resetSelectedCities();  // Сбрасываем выбранные города
                    sendMainMenu(chatId, update.getCallbackQuery().getFrom().getFirstName());
                    DeleteMessageChat(chatId, messageId);
                    break;

                default:
                    sendMessage(chatId, "Sorry, command was not recognized");

            }

        }

    }
    private void resetSelectedCities() {
        selectedFromCity = null;
        selectedToCity = null;
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
            e.printStackTrace();

        }
    }

    // Приветствие
    private void startCommandReceived(long chatId, String name){
        String answer = "Привет, " + name + ", я для поиска билетов компании Vlastelin, нажми на меню, чтобы увидеть мои опции";


        sendMessage(chatId, answer);
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

    private void searchTickets(long chatId, String fromCityId, String toCityId){
        if (selectedDate == null) {
            sendMessage(chatId, "Пожалуйста, введите дату.");
            return;

        }
        String ticketsInfo = app.findTickets(fromCityId, toCityId, selectedDate);
        sendMessage(chatId, ticketsInfo);
    }

    // Метод для проверки корректности введенной даты
    private boolean isValidDate(String date) {
        String datePattern = "\\d{4}.\\d{2}.\\d{2}"; // Регулярное выражение для формата YYYY-MM-DD
        return date.matches(datePattern);
    }



    public void sendMessageWithButtonsCity(long chatId, String text, InlineKeyboardMarkup keyboardMarkup){

        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    public InlineKeyboardMarkup getCityButtons(String type){

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> KeyBoard = new ArrayList<>();

        Map<String, String> cities = app.getCities();

        for (Map.Entry<String, String> entry :cities.entrySet()){

            String cityId = entry.getKey();
            String cityName = entry.getValue();

            InlineKeyboardButton cityButton = new InlineKeyboardButton();
            cityButton.setText(cityName);
            cityButton.setCallbackData(type + ":" + cityId);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(cityButton);
            KeyBoard.add(row);
        }

        // Добавляем кнопку "Назад"
        InlineKeyboardButton backButton = new InlineKeyboardButton();


        backButton.setText("⬅\uFE0F Вернуться назад ⬅\uFE0F");  // Текст кнопки
        backButton.setCallbackData("back_to_cities");  // CallbackData для кнопки назад, чтобы очистить города

        List<InlineKeyboardButton> rowBack = new ArrayList<>();
        rowBack.add(backButton);
        KeyBoard.add(rowBack);

        inlineKeyboardMarkup.setKeyboard(KeyBoard);
        return inlineKeyboardMarkup;
    }






    public void DeleteMessageChat(long chatId, int messageId){

        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(String.valueOf(chatId));

        try{
            execute(deleteMessage);

        } catch (TelegramApiException e){
            log.error("Error delete message: " + e.getMessage());
            e.printStackTrace();
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
