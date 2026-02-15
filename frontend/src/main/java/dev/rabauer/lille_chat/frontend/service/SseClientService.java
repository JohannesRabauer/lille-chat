package dev.rabauer.lille_chat.frontend.service;

import com.vaadin.flow.component.UI;
import dev.rabauer.lille_chat.frontend.dto.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class SseClientService {

    private static final Logger log = LoggerFactory.getLogger(SseClientService.class);

    private final WebClient webClient;

    private final Map<UI, SseSubscription> subscriptions = new ConcurrentHashMap<>();

    public SseClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void subscribe(UI ui, Consumer<ChatMessageDto> onMessage) {
        unsubscribe(ui);

        Disposable disposable = webClient.get()
                .uri("/api/sse/subscribe")
                .retrieve()
                .bodyToFlux(ChatMessageDto.class)
                .subscribe(
                        message -> ui.access(() -> onMessage.accept(message)),
                        error -> log.warn("SSE stream error", error),
                        () -> log.debug("SSE stream completed")
                );

        subscriptions.put(ui, new SseSubscription(disposable, onMessage));
    }

    public void unsubscribe(UI ui) {
        SseSubscription existing = subscriptions.remove(ui);
        if (existing != null) {
            existing.disposable().dispose();
        }
    }

    private record SseSubscription(Disposable disposable, Consumer<ChatMessageDto> onMessage) {
    }
}
