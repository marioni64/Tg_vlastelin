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
    public Map<Integer, String > findTickets(String fromCityId, String toCityId, String date) {

        Map<Integer, String > ticketDetails = new HashMap<>();



        String searchUrl = String.format("https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/Tickets/Search?departurePointId=%s&destinationPointId=%s&date=%s&sortType=DepartureTime", fromCityId, toCityId, date);

        System.out.println(searchUrl);
        try {

            Document searchResults = Jsoup.connect(searchUrl).get();
            Elements tickets = searchResults.select("div.tickets__item");


            int count = 1;
            for(Element ticket : tickets){

                String departureDate = ticket.select(".tickets__from .tickets__date").text();
                String departureTime = ticket.select(".tickets__from .tickets__time").text();
                String arrivalDate = ticket.select(".tickets__to .tickets__date").text();
                String arrivalTime = ticket.select(".tickets__to .tickets__time").text();
                String priceChen = ticket.select(".tickets__priceNum").text();
                String travelTime = ticket.select(".tickets__Movetime .tickets__timeNum").text();

                if (departureDate.isEmpty() || departureTime.isEmpty() || arrivalDate.isEmpty() || arrivalTime.isEmpty() || priceChen.isEmpty() || travelTime.isEmpty()) {
                    continue; // Пропускаем билет, если отсутствуют данные
                }

                int price = Integer.parseInt(priceChen.replaceAll("[^0-9]", ""));

                String ticketInfo = "Отправление: " + departureDate + " " + departureTime + "\n" +
                        "Прибытие: " + arrivalDate + " " + arrivalTime + "\n" +
                        "Стоимость: " + price + " рублей\n" +
                        "Время в пути: " + travelTime + "\n" +
                        "--------------\n";

                ticketDetails.put(count, ticketInfo);
                count ++;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при поиске билетов: " + e.getMessage());
            ticketDetails.put(0, "Ошибка при поиске билетов.");
        }
        return ticketDetails;
    }



    public Map<Integer, String > getAvailLableSeats( String tripId, String departureTime){
        Map<Integer, String > availLableSeats = new HashMap<>();
        String seatUrl = String.format("https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/Tickets/TripInfo?tripId=%s&departureTime=%s", tripId, departureTime);

        try{
            Document seatDocument = Jsoup.connect(seatUrl).get();
            Elements seats = seatDocument.select(".scheme_tile");

            int count = 1;
            for (Element seat : seats){
                String seatNumber = seat.text();

                if (!seat.className().contains("sold")){
                    availLableSeats.put(count, seatNumber);
                    count ++ ;
                }
            }

        } catch (IOException e) {
            System.err.println("Error for get scheme" + e.getMessage());
        }
        return availLableSeats;

    }

}

