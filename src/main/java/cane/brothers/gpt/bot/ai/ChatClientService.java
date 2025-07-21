package cane.brothers.gpt.bot.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatClientService {

    @Qualifier("openAiChatClient")
    private final ChatClient openAiChatClient;

    @Qualifier("geminiChatClient")
    private final ChatClient geminiChatClient;

    // TODO gemini chat client

    public String call(String userMessage) {
        try {
//            return openAiChatClient.prompt()
            return geminiChatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("open-ai error", ex);
            return "An error occurred while processing the request to the <b>open-ai</b> service.";
        }
    }


}
