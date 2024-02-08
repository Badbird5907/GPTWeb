package dev.badbird.gpt.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@JsonSerialize
public class WebSearch {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The query to search for")
    private String query;

    @SneakyThrows
    public List<SearchResult> execute() {
        System.out.println("[SEARCH] - Searching for " + query);
        String url = "https://www.google.com/search?q=" + query;
        Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0").get();
        // document.querySelectorAll("#rso > div > div > div > div > div > div > span > a")
        Elements elements = document.select("#rso > div > div > div > div > div > div > span > a");
        List<SearchResult> results = new ArrayList<>();
        for (Element element : elements) {
            String href = element.attr("href");
            Element h3 = element.selectFirst("h3");
            String title = h3.text();
            results.add(new SearchResult(title, href));
        }
        System.out.println("[SEARCH] - Found " + results.size() + " results");
        if (results.isEmpty()) {
            System.out.println("[SEARCH] - No results found! Data:");
            System.out.println(document.html());
        }
        for (SearchResult result : results) {
            System.out.println("[SEARCH] - " + result.getTitle() + " - " + result.getUrl());
        }
        return results;
    }
}
