package dev.badbird.gpt.functions;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonSerialize
@AllArgsConstructor
public class SearchResult {
    private String title;
    private String url;
}
