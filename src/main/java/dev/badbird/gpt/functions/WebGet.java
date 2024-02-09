package dev.badbird.gpt.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.badbird.gpt.Main;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

@JsonSerialize
public class WebGet {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The url to get")
    private String url;

    @SneakyThrows
    public WebGetResult execute() {
        try {
            System.out.println("[GET] - Getting " + url);
            // send a get request to the url and return the result
            Document document = Jsoup.connect(url).userAgent(Main.USER_AGENT).get();
            Elements body = document.select("body");
            // remove any useless elements such as headers and footers
            body.select("header").remove();
            body.select("footer").remove();
            // remove any hidden elements
            body.select("[style*=display:none]").remove();
            body.select("[style*=visibility:hidden]").remove();
            body.select("[style*=opacity:0]").remove();
            String text = body.text();
            System.out.println(text);
            return new WebGetResult(text);
        } catch (IOException e) {
            System.err.println("[GET] - Error: " + e.getMessage());
            return new WebGetResult("Error: " + e.getMessage());
        }
    }
}
