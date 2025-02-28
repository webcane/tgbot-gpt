package cane.brothers.gpt.bot.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiVoiceService {

    private final OpenAiAudioTranscriptionModel voiceModel;

    public String transcribe(Resource fileResource) {
        var response = voiceModel.call(fileResource);
        log.debug("transcribe voice to text: %s".formatted(response));
        // todo handle errors
        return response;
    }
}
