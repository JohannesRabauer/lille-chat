package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import dev.rabauer.lille_chat.frontend.dto.ChatMessageDto;
import dev.rabauer.lille_chat.frontend.dto.ConversationDto;
import dev.rabauer.lille_chat.frontend.dto.ConversationType;
import dev.rabauer.lille_chat.frontend.dto.SendMessageRequest;
import dev.rabauer.lille_chat.frontend.dto.UserDto;
import dev.rabauer.lille_chat.frontend.service.ChatMessageClientService;
import dev.rabauer.lille_chat.frontend.service.SseClientService;
import dev.rabauer.lille_chat.frontend.service.UserClientService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@SpringComponent
@UIScope
public class ChatView extends VerticalLayout {

    private final ChatMessageClientService chatMessageClientService;
    private final SseClientService sseClientService;
    private final UserClientService userClientService;

    private final H3 conversationHeader = new H3("Select a conversation");
    private final VerticalLayout messageList = new VerticalLayout();
    private final TextField messageInput = new TextField();
    private final Button sendButton = new Button(VaadinIcon.PAPERPLANE.create());
    private final Scroller scroller;

    private ConversationDto currentConversation;
    private UserDto currentUser;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    public ChatView(ChatMessageClientService chatMessageClientService,
                    SseClientService sseClientService,
                    UserClientService userClientService) {
        this.chatMessageClientService = chatMessageClientService;
        this.sseClientService = sseClientService;
        this.userClientService = userClientService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        conversationHeader.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("margin", "0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        messageList.setPadding(true);
        messageList.setSpacing(true);
        messageList.setWidthFull();

        scroller = new Scroller(messageList);
        scroller.setSizeFull();
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        messageInput.setPlaceholder("Type a message...");
        messageInput.setWidthFull();
        messageInput.setEnabled(false);

        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setEnabled(false);
        sendButton.addClickListener(e -> sendMessage());
        messageInput.addKeyPressListener(Key.ENTER, e -> sendMessage());

        HorizontalLayout inputBar = new HorizontalLayout(messageInput, sendButton);
        inputBar.setWidthFull();
        inputBar.setPadding(true);
        inputBar.setSpacing(true);
        inputBar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        inputBar.expand(messageInput);

        add(conversationHeader, scroller, inputBar);
        setFlexGrow(1, scroller);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        try {
            currentUser = userClientService.getCurrentUser();
        } catch (Exception e) {
            currentUser = null;
        }
        if (attachEvent.getUI() != null) {
            sseClientService.subscribe(attachEvent.getUI(), this::onSseMessage);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        sseClientService.unsubscribe(detachEvent.getUI());
    }

    public void openConversation(ConversationDto conversation) {
        this.currentConversation = conversation;

        String title;
        if (conversation.type() == ConversationType.DIRECT && currentUser != null) {
            title = conversation.participants().stream()
                    .filter(p -> !p.id().equals(currentUser.id()))
                    .map(UserDto::username)
                    .findFirst()
                    .orElse("Direct Chat");
        } else {
            title = conversation.name() != null ? conversation.name() : "Group Chat";
        }
        conversationHeader.setText(title);

        messageInput.setEnabled(true);
        sendButton.setEnabled(true);

        loadMessages();
    }

    private void loadMessages() {
        messageList.removeAll();
        if (currentConversation == null) return;

        try {
            List<ChatMessageDto> messages = chatMessageClientService.getMessages(
                    currentConversation.id(), 0, 100);
            for (ChatMessageDto msg : messages) {
                addMessageToList(msg);
            }
            scrollToBottom();
        } catch (Exception e) {
            messageList.add(new Span("Failed to load messages"));
        }
    }

    private void sendMessage() {
        if (currentConversation == null) return;
        String text = messageInput.getValue().trim();
        if (text.isBlank()) return;

        try {
            chatMessageClientService.sendMessage(currentConversation.id(), new SendMessageRequest(text));
            messageInput.clear();
            loadMessages();
        } catch (Exception e) {
            // Notification could be added here
        }
    }

    private void onSseMessage(ChatMessageDto message) {
        if (currentConversation != null && message.conversationId().equals(currentConversation.id())) {
            addMessageToList(message);
            scrollToBottom();
        }
    }

    private void addMessageToList(ChatMessageDto message) {
        boolean isOwn = currentUser != null && message.sender().id().equals(currentUser.id());

        Div bubble = new Div();
        bubble.getStyle()
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("max-width", "70%")
                .set("word-wrap", "break-word");

        if (isOwn) {
            bubble.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("margin-left", "auto");
        } else {
            bubble.getStyle()
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("margin-right", "auto");
        }

        Span senderName = new Span(message.sender().username());
        senderName.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-s)");

        Span content = new Span(message.content());
        content.getStyle().set("display", "block");

        Span time = new Span(TIME_FORMAT.format(message.sentAt()));
        time.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("display", "block")
                .set("text-align", "right");

        bubble.add(senderName, content, time);

        HorizontalLayout row = new HorizontalLayout(bubble);
        row.setWidthFull();
        row.setPadding(false);
        if (isOwn) {
            row.setJustifyContentMode(JustifyContentMode.END);
        }
        messageList.add(row);
    }

    private void scrollToBottom() {
        scroller.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }
}
