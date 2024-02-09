package dev.badbird.gpt.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;

@JsonSerialize
public class GetDateTime {
    @JsonProperty(required= true)
    @JsonPropertyDescription("The format to use for the date and time (java SimpleDateFormat)")
    private String format;


    public DateTimeResult execute() {
        return new DateTimeResult(new SimpleDateFormat(format).format(System.currentTimeMillis()));
    }

}
