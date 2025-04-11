package com.vumc.SpringAIDemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
public class OllamaController {
    private ChatClient chatClient;

    @Value("classpath:/prompts/context_prompt.st")
    private Resource promptTemplateText;

    @Value ("classpath:/docs/timeaway_faq.txt")
    private Resource faqContentResource;

    public OllamaController(OllamaChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    @GetMapping("/{question}")
    public ResponseEntity<String> getAnswer(@PathVariable String question) {

        Message userMessage = new UserMessage(question);
//// System Role
//        String systemText = """
//                You are a helpful AI assistant that specialized in providing help for VUMC users who need to get answers regarding provider time away application.
//                Your name is TimeAway Assistant.
//                Be polite and respectful. Keep the conversation on topic and do not stray from what is available in the file.
//                  """;
//        Message systemMessage = new SystemPromptTemplate(systemText).createMessage();
//
//        //Assistant Role
//        String instructions = """
//                       You are a helpful AI assistant that helps answer Provider Time Away related questions.
//                       You should reply to the user's request with your name and a greeting.
//                       If user's question is outside the Provider Time Away domain, please be polite and say that you can only answer PTA related questions.
//                You should also inquire user's feedback at the end of conversation.
//                        """;
//        AssistantPromptTemplate assistantPromptTemplate = new AssistantPromptTemplate(instructions);
//        Message assistantPromptTemplateMessage = assistantPromptTemplate.createMessage(Map.of("text", question));

//        Prompt prompt = new Prompt(List.of(userMessage, systemMessage, assistantPromptTemplateMessage));
        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateText);

        Map<String, Object> promptMap = new HashMap<String, Object>();
        promptMap.put("question", question);
        promptMap.put("context", faqContentResource);

        Prompt prompt = promptTemplate.create(promptMap);

        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        System.out.println(chatResponse.getMetadata().getPromptMetadata());

        String response = chatResponse.getResult().getOutput().getText();

        return ResponseEntity.ok(response);
    }
}
