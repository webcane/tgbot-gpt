package cane.brothers.gpt.bot.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiVoiceService {

    private final OpenAiAudioTranscriptionModel voiceModel;

    public String transcribe(Resource fileResource) {
        var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.JSON)
                .build();
        var transcriptionRequest = new AudioTranscriptionPrompt(fileResource, transcriptionOptions);
        var response = voiceModel.call(transcriptionRequest);
        log.debug("transcribe voice to text: %s".formatted(response.getResult().getOutput()));
        // todo handle errors
        return response.getResult().getOutput();
    }
}
