package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.rabauer.lille_chat.frontend.dto.UserDto;
import dev.rabauer.lille_chat.frontend.service.UserClientService;

public class MainLayout extends AppLayout {

    private final UserClientService userClientService;
    private final ChatView chatView;
    private final ConversationListView conversationListView;

    private UserDto currentUser;

    public MainLayout(UserClientService userClientService,
                      ChatView chatView,
                      ConversationListView conversationListView) {
        this.userClientService = userClientService;
        this.chatView = chatView;
        this.conversationListView = conversationListView;

        try {
            this.currentUser = userClientService.getCurrentUser();
        } catch (Exception e) {
            this.currentUser = null;
        }

        conversationListView.setOnConversationSelected(chatView::openConversation);

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Lille Chat");
        logo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        HorizontalLayout header = new HorizontalLayout();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.add(new DrawerToggle(), logo);

        if (currentUser != null) {
            Span userInfo = new Span(currentUser.username());
            userInfo.addClassName("user-badge");
            userInfo.getStyle().set("margin-left", "auto");

            Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create(), e -> {
                getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
            });
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            header.add(userInfo, logoutButton);
        }

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        Button friendsButton = new Button("Friends", VaadinIcon.USERS.create(), e -> {
            FriendsDialog dialog = new FriendsDialog(
                    conversationListView.getFriendClientService(),
                    conversationListView.getUserClientService(),
                    conversationListView.getConversationClientService(),
                    conversation -> {
                        chatView.openConversation(conversation);
                        conversationListView.refresh();
                    }
            );
            dialog.open();
        });
        friendsButton.setWidthFull();

        Button newGroupButton = new Button("New Group", VaadinIcon.GROUP.create(), e -> {
            NewGroupDialog dialog = new NewGroupDialog(
                    conversationListView.getFriendClientService(),
                    conversationListView.getConversationClientService(),
                    conversation -> {
                        chatView.openConversation(conversation);
                        conversationListView.refresh();
                    }
            );
            dialog.open();
        });
        newGroupButton.setWidthFull();

        HorizontalLayout actions = new HorizontalLayout(friendsButton, newGroupButton);
        actions.addClassName("drawer-actions");
        actions.setWidthFull();
        actions.setPadding(true);

        drawerContent.add(actions, conversationListView);
        drawerContent.setFlexGrow(1, conversationListView);

        addToDrawer(drawerContent);
        setPrimarySection(Section.DRAWER);
    }

    public UserDto getCurrentUser() {
        return currentUser;
    }
}
