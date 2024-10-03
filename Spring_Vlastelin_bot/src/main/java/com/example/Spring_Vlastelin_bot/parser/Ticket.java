package com.example.Spring_Vlastelin_bot.parser;

public class Ticket {

    private String departureTime; // Время отправления
    private String arrivalTime; // Время прибытия
    private int price; // Цена
    private int availableSeats; // Свободные места

    public Ticket(String departureTime, String arrivalTime, int price, int availableSeats) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    // Геттеры для доступа к полям
    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public int getPrice() {
        return price;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

}
