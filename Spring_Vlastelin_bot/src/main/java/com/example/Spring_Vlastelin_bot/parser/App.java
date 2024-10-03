package com.example.Spring_Vlastelin_bot.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class App {
    public Map<String, String> getCities() {
        Map<String, String> cities = new HashMap<>();

        try {
            Document document = Jsoup.connect("https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();

            Element selectDeparture = document.select("select[name=departurePointId]").first();

            if (selectDeparture != null) {
                Elements options = selectDeparture.getElementsByTag("option");

                for (Element option : options) {
                    String cityId = option.attr("value");
                    String cityName = option.text();
                    cities.put(cityId, cityName);
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка при получении списка городов: " + e.getMessage());
        }

        return cities;
    }

    // Метод для парсинга данных о билетах
    public String findTickets(String fromCityId, String toCityId, String date) {
        StringBuilder result = new StringBuilder();

        try {
            // Подключаемся к странице с билетами
            String searchUrl = "https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/Tickets/Search?departurePointId=%s&destinationPointId=%s&date=%s&sortType=DepartureTime"  + fromCityId +
            "&destinationPointId=" + toCityId + "&date=" + date + "&sortType=DepartureTime";
            Document searchResults = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();

            Elements tickets = searchResults.select("div.tickets__item");

            if (tickets.isEmpty()) {
                return "Нет доступных билетов на указанную дату.";
            }

            for (Element ticket : tickets) {
                String departureDate = ticket.select("div.tickets__from div.tickets__date").text();
                String departureTime = ticket.select("div.tickets__from div.tickets__time").text();
                String arrivalDate = ticket.select("div.tickets__to div.tickets__date").text();
                String arrivalTime = ticket.select("div.tickets__to div.tickets__time").text();
                String priceText = ticket.select("div.tickets__priceNum").text();
                String travelTime = ticket.select("div.tickets__Movetime .tickets__timeNum").text();

                // Проверка на наличие данных
                if (departureDate.isEmpty() || departureTime.isEmpty() || arrivalDate.isEmpty() || arrivalTime.isEmpty() || priceText.isEmpty() || travelTime.isEmpty()) {
                    continue; // Пропускаем билет, если отсутствуют данные
                }

                int price = Integer.parseInt(priceText.replaceAll("[^0-9]", ""));

                result.append("Отправление: ").append(departureDate).append(" ").append(departureTime).append("\n")
                        .append("Прибытие: ").append(arrivalDate).append(" ").append(arrivalTime).append("\n")
                        .append("Стоимость: ").append(price).append(" рублей\n")
                        .append("Время в пути: ").append(travelTime).append("\n")
                        .append("--------------\n");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при поиске билетов: " + e.getMessage());
            return "Ошибка при поиске билетов.";
        }

        return result.toString();
    }
}
