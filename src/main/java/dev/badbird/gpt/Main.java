package dev.badbird.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import dev.badbird.gpt.functions.WebGet;
import dev.badbird.gpt.functions.WebSearch;
import dev.badbird.gpt.functions.WikipediaSummary;
import dev.badbird.gpt.util.MalformedJsonFixer;
import io.reactivex.Flowable;
import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0";
    @Getter
    private static OkHttpClient client = new OkHttpClient();
    @Getter
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @SneakyThrows
    public static void main(String[] args) {
        String token = new String(Files.readAllBytes(new File("token.txt").toPath())).trim();
        OpenAiService service = new OpenAiService(token);
        List<ChatFunction> functionList = List.of(ChatFunction.builder()
                .name("web_get")
                .description("Get the contents of a webpage")
                .executor(WebGet.class, WebGet::execute)
                .build(),
                ChatFunction.builder()
                        .name("web_search")
                        .description("Search google")
                        .executor(WebSearch.class, WebSearch::execute)
                        .build(),
                ChatFunction.builder()
                        .name("wikipedia_summary")
                        .description("Get a summary of a wikipedia page")
                        .executor(WikipediaSummary.class, WikipediaSummary::execute)
                        .build(),
                ChatFunction.builder()
                        .name("wikipedia_content")
                        .description("Get the text contents of a wikipedia page")
                        .executor(WikipediaSummary.class, WikipediaSummary::execute)
                        .build()
                );
        FunctionExecutor functionExecutor = new FunctionExecutor(functionList);

        OpenAiService.functionCallMutator = (s) -> {
            try {
                return OpenAiService.getMapper().readTree(s);
            } catch (JsonProcessingException e) {
                String gptFixed = MalformedJsonFixer.fix(s, service);
                try {
                    return OpenAiService.getMapper().readTree(gptFixed);
                } catch (JsonProcessingException ex) {
                    System.err.println("Error parsing function call: " + s);
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        };

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful AI assistant with access to the internet. After searching something, go through the links of interest and read the contents of the linked page.");
        messages.add(systemMessage);
        System.out.print("First Query: ");
        Scanner scanner = new Scanner(System.in);
        ChatMessage firstMsg = new ChatMessage(ChatMessageRole.USER.value(), scanner.nextLine());
        messages.add(firstMsg);

        while (true) {
            try {
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .model("gpt-4-0125-preview")
                        .messages(messages)
                        .functions(functionExecutor.getFunctions())
                        .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                        .n(1)
                        .logitBias(new HashMap<>())
                        .build();
                Flowable<ChatCompletionChunk> flowable = service.streamChatCompletion(chatCompletionRequest);

                AtomicBoolean isFirst = new AtomicBoolean(true);
                ChatMessage chatMessage = service.mapStreamToAccumulator(flowable)
                        .doOnNext(accumulator -> {
                            if (accumulator.isFunctionCall()) {
                                if (isFirst.getAndSet(false)) {
                                    System.out.println("Executing function " + accumulator.getAccumulatedChatFunctionCall().getName() + "...");
                                }
                            } else {
                                if (isFirst.getAndSet(false)) {
                                    System.out.print("Response: ");
                                }
                                if (accumulator.getMessageChunk().getContent() != null) {
                                    System.out.print(accumulator.getMessageChunk().getContent());
                                }
                            }
                        })
                        .doOnComplete(System.out::println)
                        .lastElement()
                        .blockingGet()
                        .getAccumulatedMessage();
                messages.add(chatMessage); // don't forget to update the conversation with the latest response

                if (chatMessage.getFunctionCall() != null) {
                    System.out.println("Trying to execute " + chatMessage.getFunctionCall().getName() + "...");
                    ChatMessage functionResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(chatMessage.getFunctionCall());
                    System.out.println("Executed " + chatMessage.getFunctionCall().getName() + ".");
                    messages.add(functionResponse);
                    continue;
                }

                System.out.print("Next Query: ");
                String nextLine = scanner.nextLine();
                if (nextLine.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }
                messages.add(new ChatMessage(ChatMessageRole.USER.value(), nextLine));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error occurred! Last message was from " + messages.get(messages.size() - 1).getRole());
                System.err.println("Recovering...");
            }
        }
    }
}