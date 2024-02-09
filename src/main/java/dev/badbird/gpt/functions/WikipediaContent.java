package dev.badbird.gpt.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.badbird.gpt.Main;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@JsonSerialize
public class WikipediaContent {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The title of the page")
    private String title;

    @Data
    @JsonSerialize
    @AllArgsConstructor
    public static class ContentResult {
        private String title;
        private String content;
    }

    @SneakyThrows
    public ContentResult execute() {
        String url = "https://en.wikipedia.org/api/rest_v1/page/html/" + title;
        Document document = Jsoup.connect(url).userAgent(Main.USER_AGENT).get();
        String content = document.select("body").text();
        return new ContentResult(title, content);
    }
}
