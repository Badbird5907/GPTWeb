package dev.badbird.gpt.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.badbird.gpt.Main;
import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.Call;
import okhttp3.Request;

@JsonSerialize
public class WikipediaSummary {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The title of the page")
    private String title;

    @Data
    @JsonSerialize
    @AllArgsConstructor
    public static class SummaryResult {
        private String title;
        private String displayTitle;
        private String description;
        private String extract;
    }

    public SummaryResult execute() {
        // https://en.wikipedia.org/api/rest_v1/page/summary/Computer
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + title;
        Call call = Main.getClient().newCall(new Request.Builder().url(url).build());
        try {
            okhttp3.Response response = call.execute();
            if (response.code() != 200) {
                return new SummaryResult(title, null, null, "Error: " + response.code() + " " + response.message());
            }
            String body = response.body().string();
            return Main.getGson().fromJson(body, SummaryResult.class);
        } catch (Exception e) {
            return new SummaryResult(title, null, null, "Error: " + e.getMessage());
        }
    }
}
