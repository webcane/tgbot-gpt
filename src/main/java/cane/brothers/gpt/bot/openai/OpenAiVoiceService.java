package cane.brothers.gpt.bot.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiVoiceService {

    private final OpenAiAudioTranscriptionModel voiceModel;

    public String transcribe(Resource fileResource) {
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(fileResource);
        AudioTranscriptionResponse response = voiceModel.call(transcriptionRequest);
        log.debug(response.getResult().toString());
        // todo handle errors
        return response.getResult().getOutput();
    }
}
