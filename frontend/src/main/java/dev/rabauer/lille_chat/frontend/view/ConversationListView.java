package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import dev.rabauer.lille_chat.frontend.dto.ConversationDto;
import dev.rabauer.lille_chat.frontend.dto.ConversationType;
import dev.rabauer.lille_chat.frontend.dto.UserDto;
import dev.rabauer.lille_chat.frontend.service.ConversationClientService;
import dev.rabauer.lille_chat.frontend.service.FriendClientService;
import dev.rabauer.lille_chat.frontend.service.UserClientService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

@SpringComponent
@UIScope
public class ConversationListView extends VerticalLayout {

    private final ConversationClientService conversationClientService;
    private final FriendClientService friendClientService;
    private final UserClientService userClientService;

    private Consumer<ConversationDto> onConversationSelected;
    private UserDto currentUser;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    public ConversationListView(ConversationClientService conversationClientService,
                                FriendClientService friendClientService,
                                UserClientService userClientService) {
        this.conversationClientService = conversationClientService;
        this.friendClientService = friendClientService;
        this.userClientService = userClientService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        try {
            this.currentUser = userClientService.getCurrentUser();
        } catch (Exception e) {
            this.currentUser = null;
        }

        refresh();
    }

    public void setOnConversationSelected(Consumer<ConversationDto> handler) {
        this.onConversationSelected = handler;
    }

    public void refresh() {
        removeAll();
        try {
            List<ConversationDto> conversations = conversationClientService.listConversations();
            if (conversations == null || conversations.isEmpty()) {
                Div empty = new Div();
                empty.addClassName("empty-state");
                empty.setText("No conversations yet");
                add(empty);
                return;
            }
            for (ConversationDto conv : conversations) {
                add(createConversationItem(conv));
            }
        } catch (Exception e) {
            Span error = new Span("Failed to load conversations");
            error.getStyle().set("padding", "var(--lumo-space-m)");
            add(error);
        }
    }

    private Div createConversationItem(ConversationDto conversation) {
        Div item = new Div();
        item.addClassName("conversation-item");

        String title = resolveConversationName(conversation);
        Span name = new Span(title);
        name.getStyle()
                .set("font-weight", "500")
                .set("display", "block");

        item.add(name);

        if (conversation.lastMessage() != null) {
            String preview = conversation.lastMessage().sender().username()
                    + ": " + truncate(conversation.lastMessage().content(), 40);
            Span lastMsg = new Span(preview);
            lastMsg.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("display", "block");

            Span time = new Span(TIME_FORMAT.format(conversation.lastMessage().sentAt()));
            time.getStyle()
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("color", "var(--lumo-tertiary-text-color)")
                    .set("float", "right");

            item.add(lastMsg, time);
        }

        item.addClickListener(e -> {
            if (onConversationSelected != null) {
                onConversationSelected.accept(conversation);
            }
        });

        return item;
    }

    private String resolveConversationName(ConversationDto conversation) {
        if (conversation.type() == ConversationType.DIRECT && currentUser != null) {
            return conversation.participants().stream()
                    .filter(p -> !p.id().equals(currentUser.id()))
                    .map(UserDto::username)
                    .findFirst()
                    .orElse("Direct Chat");
        }
        return conversation.name() != null ? conversation.name() : "Group Chat";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    public FriendClientService getFriendClientService() {
        return friendClientService;
    }

    public UserClientService getUserClientService() {
        return userClientService;
    }

    public ConversationClientService getConversationClientService() {
        return conversationClientService;
    }
}
