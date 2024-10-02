package com.example.Spring_Vlastelin_bot.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {

        Document document = Jsoup.connect("https://xn--64-6kcadcgv0a4axp4bhes.xn--p1ai/").get();

        Elements PostTitleElements = document.getElementsByAttributeValue("item", "url");
        PostTitleElements.forEach(postTitleElements -> System.out.println(postTitleElements.attr("title") + "|" + postTitleElements.attr("href")));

    }
}
