package com.example.Spring_Vlastelin_bot.parser;
import java.io.IOException;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TicketParser {
    public static void main(String[] args) {
        // IDs городов
        String fromCityId = "271";
        String toCityId = "268";

        // Ввод даты вручную
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите дату (например, 9.10.2024): ");
        String date = scanner.nextLine();

        // Формируем URL с введенной датой
        String url = String.format("https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/Tickets/Search?departurePointId=%s&destinationPointId=%s&date=%s&sortType=DepartureTime", fromCityId, toCityId, date);

        // Отправляем запрос и парсим результат
        try {
            // Получаем HTML-код страницы
            Document doc = Jsoup.connect(url).get();

            // Находим элементы с классом tickets__item
            Elements tickets = doc.select(".tickets__item");

            // Перебираем каждый элемент билета
            for (Element ticket : tickets) {
                // Извлекаем информацию об отправлении и прибытии
                String departureDate = ticket.select(".tickets__from .tickets__date").text();
                String departureTime = ticket.select(".tickets__from .tickets__time").text();
                String arrivalDate = ticket.select(".tickets__to .tickets__date").text();
                String arrivalTime = ticket.select(".tickets__to .tickets__time").text();

                // Извлекаем стоимость и время в пути
                String price = ticket.select(".tickets__priceNum").text();
                String travelTime = ticket.select(".tickets__Movetime .tickets__timeNum").text();

                // Выводим результаты
                System.out.println("Дата отправления: " + departureDate);
                System.out.println("Время отправления: " + departureTime);
                System.out.println("Дата прибытия: " + arrivalDate);
                System.out.println("Время прибытия: " + arrivalTime);
                System.out.println("Стоимость: " + price);
                System.out.println("Время в пути: " + travelTime);
                System.out.println("---------");
            }
        } catch (IOException e) {
            System.out.println("Ошибка при попытке получить данные: " + e.getMessage());
        }
    }
}


