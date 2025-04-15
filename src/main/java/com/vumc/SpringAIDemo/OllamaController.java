package com.vumc.SpringAIDemo;

import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ollama")
@CrossOrigin("*")
public class OllamaController {

    private final ChatClient chatClient;

    ChatOptions options;

    @Value("classpath:/prompts/prompt.st")
    private Resource promptTemplate;

    @Value ("classpath:/docs/faq.txt")
    private Resource faqContentResource;

    @Value ("classpath:/docs/system.txt")
    private Resource systemText;

    @Value ("classpath:/docs/assistant.txt")
    private Resource assistantText;

    public OllamaController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        options = ChatOptions.builder()
              .temperature(1.0)
              .topP(0.9)
              .frequencyPenalty(0.5)
              .presencePenalty(0.5)
              .build();

    }

    @GetMapping("/text/{text}")
    public ResponseEntity<String> getAnswer(@PathVariable(value = "text") String text) {

        Message systemMessage = new SystemPromptTemplate(systemText).createMessage();
        Message assistantMessage = new AssistantPromptTemplate(assistantText).createMessage();

        PromptTemplate promptTemplate = new PromptTemplate(this.promptTemplate);
        Message promptMessage = promptTemplate.createMessage(Map.of("question", text, "context", faqContentResource));

        Prompt prompt = new Prompt(List.of(systemMessage, assistantMessage, promptMessage));

        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .options(options)
                .call()
                .chatResponse();

        System.out.println(chatResponse.getMetadata().getUsage().getPromptTokens());

        String response = chatResponse.getResult().getOutput().getText();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Ok");
    }
}
