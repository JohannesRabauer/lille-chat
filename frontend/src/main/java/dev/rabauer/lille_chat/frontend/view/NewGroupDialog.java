package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import dev.rabauer.lille_chat.frontend.dto.ConversationDto;
import dev.rabauer.lille_chat.frontend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille_chat.frontend.dto.FriendshipDto;
import dev.rabauer.lille_chat.frontend.dto.UserDto;
import dev.rabauer.lille_chat.frontend.service.ConversationClientService;
import dev.rabauer.lille_chat.frontend.service.FriendClientService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NewGroupDialog extends Dialog {

    private final FriendClientService friendClientService;
    private final ConversationClientService conversationClientService;
    private final Consumer<ConversationDto> onGroupCreated;

    public NewGroupDialog(FriendClientService friendClientService,
                          ConversationClientService conversationClientService,
                          Consumer<ConversationDto> onGroupCreated) {
        this.friendClientService = friendClientService;
        this.conversationClientService = conversationClientService;
        this.onGroupCreated = onGroupCreated;

        setHeaderTitle("New Group Conversation");
        setWidth("450px");

        Button closeButton = new Button(VaadinIcon.CLOSE.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        TextField groupName = new TextField("Group Name");
        groupName.setWidthFull();
        groupName.setRequired(true);
        groupName.setPlaceholder("Enter a name for the group...");

        CheckboxGroup<FriendshipDto> friendsSelect = new CheckboxGroup<>();
        friendsSelect.setLabel("Select Friends");
        friendsSelect.setWidthFull();
        friendsSelect.setItemLabelGenerator(f -> f.friend().username());

        try {
            List<FriendshipDto> friends = friendClientService.listFriends();
            friendsSelect.setItems(friends);
        } catch (Exception e) {
            Notification.show("Failed to load friends", 3000, Notification.Position.MIDDLE);
        }

        Button createButton = new Button("Create Group", VaadinIcon.GROUP.create(), e -> {
            String name = groupName.getValue().trim();
            if (name.isBlank()) {
                groupName.setInvalid(true);
                groupName.setErrorMessage("Name is required");
                return;
            }

            Set<FriendshipDto> selected = friendsSelect.getSelectedItems();
            if (selected.isEmpty()) {
                Notification.show("Select at least one friend", 3000, Notification.Position.MIDDLE);
                return;
            }

            List<UUID> participantIds = selected.stream()
                    .map(f -> f.friend().id())
                    .collect(Collectors.toList());

            try {
                ConversationDto group = conversationClientService.createGroup(
                        new CreateGroupConversationRequest(name, participantIds));
                Notification.show("Group created!", 2000, Notification.Position.MIDDLE);
                onGroupCreated.accept(group);
                close();
            } catch (Exception ex) {
                Notification.show("Failed to create group", 3000, Notification.Position.MIDDLE);
            }
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setWidthFull();

        layout.add(groupName, friendsSelect, createButton);
        add(layout);
    }
}
