package cane.brothers.gpt.bot.ai;

import cane.brothers.gpt.bot.telegram.settings.GptModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatClientSelectorService {

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ApplicationContext applicationContext;

    public ChatClient getClientByModel(GptModel model) {
        String beanName = getBeanName(model);
        if (beanName == null) {
            throw new IllegalArgumentException("Unknown model: " + model);
        }
        return chatClientProvider.stream()
                .filter(client -> isModelBean(beanName, client))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(beanName + " ChatClient is not available. Check configuration and API key."));
    }


    public boolean hasClientForModel(GptModel model) {
        String beanName = getBeanName(model);
        return chatClientProvider.stream().anyMatch(client -> isModelBean(beanName, client));
    }

    String getBeanName(GptModel model) {
        return model.name().toLowerCase() + "ChatClient" ;
    }

    boolean isModelBean(String beanName, ChatClient chatClient) {
        var bean = applicationContext.getBean(beanName);
        return bean == chatClient;
    }
}
