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
                    sendMessage(chatId, "Вы выбрали города. Пожалуйста, введите дату поездки в формате DD.MM.YYYY:");
                    DeleteMessageChat(chatId, messageId);
                    break;

                case "back_to_cities":  // Логика возврата назад
                    resetSelectedCities();  // Сбрасываем выбранные города
                    sendMainMenu(chatId, update.getCallbackQuery().getFrom().getFirstName());
                    DeleteMessageChat(chatId, messageId);
                    break;

                case "select_ticket": // Обработка выбора билета
                    int ticketNumber = Integer.parseInt(callBackData.split("_")[2]);
                    selectTicket(chatId, ticketNumber); // Метод для обработки выбора билета
                    break;

                case "select_seat": // Обработка выбора места
                    String selectedSeat = callBackData.split(":")[1];
                    String tripId = callBackData.split(":")[2];
                    String departureTime = callBackData.split(":")[3];
                    confirmSeatSelection(chatId, selectedSeat, tripId, departureTime);
                    DeleteMessageChat(chatId, messageId);
                    break;

                default:
                    sendMessage(chatId, "Sorry, command was not recognized");

            }

        }

    }
    private void selectTicket(long chatId, int ticketNumber) {
        // Здесь вы можете сделать что-то с выбранным билетом
        sendMessage(chatId, "Вы выбрали билет номер " + ticketNumber + ". Спасибо за выбор!");
    }

    private void confirmSeatSelection(long chatId, String selectedSeat, String tripId, String departureTime) {
        // Здесь вы можете реализовать логику подтверждения выбора места
        sendMessage(chatId, "Вы выбрали место номер " + selectedSeat + ".\nTrip ID: " + tripId + "\nDeparture Time: " + departureTime);
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

    private void searchTickets(long chatId, String fromCityId, String toCityId) {


        if (selectedDate == null || !isValidDate(selectedDate)) {
            sendMessage(chatId, "Пожалуйста, введите корректную дату в формате ДД.ММ.ГГГГ.");
            return;
        }


        Map<Integer, String> ticketsInfo = app.findTickets(fromCityId, toCityId, selectedDate);

        StringBuilder ticketDetails = new StringBuilder();

        if (ticketsInfo == null || ticketsInfo.isEmpty()) {
            sendMessage(chatId, "Не найдены билеты на указанную дату.");
        } else {
            // Создаем объект InlineKeyboardMarkup для кнопок
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

            for (Map.Entry<Integer, String> entry : ticketsInfo.entrySet()) {
                String ticketInfo = entry.getValue(); // информация о билете
                int ticketNumber = entry.getKey();

                ticketDetails.append("Билет ").append(ticketNumber).append(":\n").append(ticketInfo).append("\n");

                // Создаем кнопку для выбора билета
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Выбрать билет " + ticketNumber);
                button.setCallbackData("select_ticket_" + ticketNumber); // задаем callback_data

                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                buttons.add(row);
            }

            inlineKeyboardMarkup.setKeyboard(buttons);

            // Отправляем сообщение с кнопками
            sendMessageWithInlineKeyboard(chatId, ticketDetails.toString(), inlineKeyboardMarkup);
            sendMessage(chatId, "В разработке");
        }
    }

    // Метод для отправки сообщения с InlineKeyboard
    private void sendMessageWithInlineKeyboard(long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {

        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void selectTicket(long chatId, String tripId, String departureTime) {
        Map<Integer, String > avaiLableSeats = app.getAvailLableSeats(tripId, departureTime);

        if (avaiLableSeats.isEmpty()){
            sendMessage(chatId, "К сожалению, места не найдены для этого билета.");
        }else {

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

            StringBuilder seatDetails = new StringBuilder("Доступные места:\n");

            for (Map.Entry<Integer, String > entry : avaiLableSeats.entrySet()){

                String seatNumber = entry.getValue();
                int seatIndex = entry.getKey();

                seatDetails.append("Место ").append(seatNumber).append("\n");

                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Выбрать место" + seatNumber);
                button.setCallbackData("select_seat:" + seatNumber + ":" + tripId + ":" + departureTime);

                List<InlineKeyboardButton> row = new ArrayList<>();

                row.add(button);
                buttons.add(row);
            }
            inlineKeyboardMarkup.setKeyboard(buttons);
            sendMessageWithInlineKeyboard(chatId, seatDetails.toString(), inlineKeyboardMarkup);


        }
    }



    // Метод для проверки корректности введенной даты
    private boolean isValidDate(String date) {
        String datePattern = "\\d{2}.\\d{2}.\\d{4}"; // Регулярное выражение для формата DD-MM-YYYY
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
