package com.example.Spring_Vlastelin_bot.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TicketParser {  // Добавляем класс

    public static void main(String[] args) throws IOException {
        // Подключаемся к веб-странице
        Document document = Jsoup.connect("https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                .get();

        // Выбор всех элементов с классом tickets__item
        Elements tickets = document.select("div.tickets__item");

        // Обработка каждого билета
        for (Element ticket : tickets) {
            // Извлечение времени отправления и прибытия
            String departureDate = ticket.select("div.tickets__from div.tickets__date").text();
            String departureTime = ticket.select("div.tickets__from div.tickets__time").text();
            String arrivalDate = ticket.select("div.tickets__to div.tickets__date").text();
            String arrivalTime = ticket.select("div.tickets__to div.tickets__time").text();

            // Извлечение стоимости и времени в пути
            String priceText = ticket.select("div.tickets__priceNum").text();
            String travelTime = ticket.select("div.tickets__Movetime .tickets__timeNum").text();

            // Преобразование стоимости в целое число, удаляя символы
            int price = Integer.parseInt(priceText.replaceAll("[^0-9]", ""));

            // Печать информации о билете
            System.out.println("Отправление: " + departureDate + " " + departureTime);
            System.out.println("Прибытие: " + arrivalDate + " " + arrivalTime);
            System.out.println("Стоимость: " + price + " рублей");
            System.out.println("Время в пути: " + travelTime);
            System.out.println("--------------");
        }
    }
}

