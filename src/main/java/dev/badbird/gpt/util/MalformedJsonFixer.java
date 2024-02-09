package dev.badbird.gpt.util;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.service.OpenAiService;

import java.util.HashMap;
import java.util.List;

public class MalformedJsonFixer {
    public static String fix(String json, OpenAiService service) { // gpt inception type crap
        List<ChatMessage> messages = List.of(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), "Fix the malformed json. Only output the fixed json, no other text."),
                new ChatMessage(ChatMessageRole.USER.value(), json)
        );
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0125")
                .messages(messages)
                .n(1)
                .build();
        ChatCompletionChoice chatCompletionChoice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        return chatCompletionChoice.getMessage().getContent();
    }
}
