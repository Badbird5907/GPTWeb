package dev.badbird.gpt.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.badbird.gpt.Main;
import lombok.Data;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonSerialize
public class WebSearch {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The query to search for")
    private String query;

    @SneakyThrows
    public List<SearchResult> execute() {
        System.out.println("[SEARCH] - Searching for " + query);
        String url = "https://www.google.com/search?q=" + query;
        Document document = Jsoup.connect(url).userAgent(Main.USER_AGENT).get();
        // #rso > div:nth-child(8) > div > div
        Elements elements = document.select("#rso > div > div > div");
        // String html = elements.html();
        // File file = new File("search-" + System.currentTimeMillis()+ ".html");
        // file.createNewFile();
        // Files.write(file.toPath(), html.getBytes());
        List<SearchResult> results = new ArrayList<>();
        for (Element resultDiv : elements) {
            // get the second child div nth-child(1)
            // div > span > a
            System.out.println(resultDiv.html());
            Element description = resultDiv.select("div:nth-child(1)").first();
            Element childDivOne = resultDiv.selectFirst("div").selectFirst("span").selectFirst("a");
            if (childDivOne == null) {
                childDivOne = resultDiv.selectFirst("div").selectFirst("div").selectFirst("span").selectFirst("a");
            }
            if (childDivOne == null) {
                // System.out.println("[SEARCH] - No child div found! Data:");
                // System.out.println(resultDiv.html());
                continue;
            }
            String href = childDivOne.attr("href");
            Element h3 = childDivOne.selectFirst("h3");
            String title = h3.text();
            results.add(new SearchResult(title, href, description.text()));
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
