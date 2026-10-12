package dio.budgeting.infrastructure.ai;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class VoiceAssistantService {
    private final TranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final TextToSpeechModel textToSpeechModel;

    public VoiceAssistantService(TranscriptionModel transcriptionModel,
                                 ChatClient.Builder chatClientBuilder,
                                 TransactionTools transactionTools,
                                 @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                                 TextToSpeechModel textToSpeechModel) throws IOException {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(transactionTools)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] respondTo(Resource voiceCommand) {
        var command = transcriptionModel.transcribe(voiceCommand);
        var reply = chatClient.prompt().user(command).call().content();
        return textToSpeechModel.call(reply);
    }
}