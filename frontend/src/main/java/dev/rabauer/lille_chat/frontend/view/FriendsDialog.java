package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import dev.rabauer.lille_chat.frontend.dto.ConversationDto;
import dev.rabauer.lille_chat.frontend.dto.FriendshipDto;
import dev.rabauer.lille_chat.frontend.dto.UserDto;
import dev.rabauer.lille_chat.frontend.service.ConversationClientService;
import dev.rabauer.lille_chat.frontend.service.FriendClientService;
import dev.rabauer.lille_chat.frontend.service.UserClientService;

import java.util.List;
import java.util.function.Consumer;

public class FriendsDialog extends Dialog {

    private final FriendClientService friendClientService;
    private final UserClientService userClientService;
    private final ConversationClientService conversationClientService;
    private final Consumer<ConversationDto> onConversationOpened;

    private final VerticalLayout friendsContent = new VerticalLayout();
    private final VerticalLayout pendingContent = new VerticalLayout();

    public FriendsDialog(FriendClientService friendClientService,
                         UserClientService userClientService,
                         ConversationClientService conversationClientService,
                         Consumer<ConversationDto> onConversationOpened) {
        this.friendClientService = friendClientService;
        this.userClientService = userClientService;
        this.conversationClientService = conversationClientService;
        this.onConversationOpened = onConversationOpened;

        setHeaderTitle("Friends");
        setWidth("500px");
        setHeight("600px");

        Button closeButton = new Button(VaadinIcon.CLOSE.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // Add friend section
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search users...");
        searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        VerticalLayout searchResults = new VerticalLayout();
        searchResults.setPadding(false);
        searchResults.setSpacing(false);

        Button searchButton = new Button("Search", e -> {
            String query = searchField.getValue().trim();
            if (query.length() < 2) return;
            searchResults.removeAll();
            try {
                List<UserDto> users = userClientService.searchUsers(query);
                if (users.isEmpty()) {
                    searchResults.add(new Span("No users found"));
                } else {
                    for (UserDto user : users) {
                        searchResults.add(createSearchResultItem(user));
                    }
                }
            } catch (Exception ex) {
                searchResults.add(new Span("Search failed"));
            }
        });
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout searchBar = new HorizontalLayout(searchField, searchButton);
        searchBar.setWidthFull();
        searchBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        searchBar.expand(searchField);

        VerticalLayout addSection = new VerticalLayout(new H3("Add Friend"), searchBar, searchResults);
        addSection.setPadding(true);
        addSection.setSpacing(true);

        mainLayout.add(addSection, new Hr());

        // Tabs for friends / pending
        Tab friendsTab = new Tab("Friends");
        Tab pendingTab = new Tab("Pending Requests");
        Tabs tabs = new Tabs(friendsTab, pendingTab);
        tabs.setWidthFull();

        friendsContent.setPadding(true);
        friendsContent.setSpacing(false);
        pendingContent.setPadding(true);
        pendingContent.setSpacing(false);
        pendingContent.setVisible(false);

        tabs.addSelectedChangeListener(event -> {
            friendsContent.setVisible(event.getSelectedTab() == friendsTab);
            pendingContent.setVisible(event.getSelectedTab() == pendingTab);
        });

        mainLayout.add(tabs, friendsContent, pendingContent);

        add(mainLayout);

        loadFriends();
        loadPendingRequests();
    }

    private void loadFriends() {
        friendsContent.removeAll();
        try {
            List<FriendshipDto> friends = friendClientService.listFriends();
            if (friends.isEmpty()) {
                friendsContent.add(new Span("No friends yet. Search and add someone!"));
                return;
            }
            for (FriendshipDto friendship : friends) {
                friendsContent.add(createFriendItem(friendship));
            }
        } catch (Exception e) {
            friendsContent.add(new Span("Failed to load friends"));
        }
    }

    private void loadPendingRequests() {
        pendingContent.removeAll();
        try {
            List<FriendshipDto> pending = friendClientService.listPendingRequests();
            if (pending.isEmpty()) {
                pendingContent.add(new Span("No pending requests"));
                return;
            }
            for (FriendshipDto request : pending) {
                pendingContent.add(createPendingItem(request));
            }
        } catch (Exception e) {
            pendingContent.add(new Span("Failed to load requests"));
        }
    }

    private HorizontalLayout createFriendItem(FriendshipDto friendship) {
        Span name = new Span(friendship.friend().username());
        name.getStyle().set("font-weight", "500");

        Button chatButton = new Button("Chat", VaadinIcon.COMMENT.create(), e -> {
            try {
                ConversationDto conv = conversationClientService.getOrCreateDirect(friendship.friend().id());
                onConversationOpened.accept(conv);
                close();
            } catch (Exception ex) {
                Notification.show("Failed to open conversation", 3000, Notification.Position.MIDDLE);
            }
        });
        chatButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout row = new HorizontalLayout(name, chatButton);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        row.expand(name);
        row.getStyle().set("padding", "var(--lumo-space-xs) 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        return row;
    }

    private HorizontalLayout createPendingItem(FriendshipDto request) {
        Span name = new Span(request.friend().username());
        name.getStyle().set("font-weight", "500");

        Button acceptButton = new Button("Accept", e -> {
            try {
                friendClientService.acceptRequest(request.id());
                Notification.show("Accepted!", 2000, Notification.Position.MIDDLE);
                loadFriends();
                loadPendingRequests();
            } catch (Exception ex) {
                Notification.show("Failed to accept", 3000, Notification.Position.MIDDLE);
            }
        });
        acceptButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);

        Button declineButton = new Button("Decline", e -> {
            try {
                friendClientService.declineRequest(request.id());
                Notification.show("Declined", 2000, Notification.Position.MIDDLE);
                loadPendingRequests();
            } catch (Exception ex) {
                Notification.show("Failed to decline", 3000, Notification.Position.MIDDLE);
            }
        });
        declineButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        HorizontalLayout row = new HorizontalLayout(name, acceptButton, declineButton);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        row.expand(name);
        row.getStyle().set("padding", "var(--lumo-space-xs) 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        return row;
    }

    private HorizontalLayout createSearchResultItem(UserDto user) {
        Span name = new Span(user.username());
        name.getStyle().set("font-weight", "500");

        Span email = new Span(user.email());
        email.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout info = new VerticalLayout(name, email);
        info.setPadding(false);
        info.setSpacing(false);

        Button addButton = new Button("Add", VaadinIcon.PLUS.create(), e -> {
            try {
                friendClientService.sendRequest(user.id());
                Notification.show("Friend request sent!", 2000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Failed to send request", 3000, Notification.Position.MIDDLE);
            }
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout row = new HorizontalLayout(info, addButton);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        row.expand(info);
        row.getStyle().set("padding", "var(--lumo-space-xs) 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        return row;
    }
}
