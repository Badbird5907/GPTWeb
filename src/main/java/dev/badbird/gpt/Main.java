package dev.badbird.gpt;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import dev.badbird.gpt.functions.WebGet;
import dev.badbird.gpt.functions.WebSearch;
import io.reactivex.Flowable;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        String token = new String(Files.readAllBytes(new File("token.txt").toPath())).trim();
        OpenAiService service = new OpenAiService(token);
        ChatFunction searchFn = ChatFunction.builder()
                .name("web_search")
                .description("Search google")
                .executor(WebSearch.class, WebSearch::execute)
                .build();
        ChatFunction getFn = ChatFunction.builder()
                .name("web_get")
                .description("Get the contents of a webpage")
                .executor(WebGet.class, WebGet::execute)
                .build();
        List<ChatFunction> functionList = List.of(getFn, searchFn);
        FunctionExecutor functionExecutor = new FunctionExecutor(functionList);

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
                        .model("gpt-4")
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
                throw new RuntimeException(e);
            }
        }
    }
}