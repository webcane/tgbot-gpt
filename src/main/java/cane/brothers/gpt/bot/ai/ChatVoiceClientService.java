package cane.brothers.gpt.bot.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.model.Model;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatVoiceClientService {

    @Qualifier("openAiVoiceChatClient")
    private final Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> voiceModel;

    public String transcribe(Resource fileResource) {
        var audioResource = new AudioTranscriptionPrompt(fileResource);
        var transcription = voiceModel.call(audioResource).getResult();
        var response = transcription.getOutput();
        log.debug("transcribe voice to text: %s".formatted(response));
        // todo handle errors
        return response;
    }
}
