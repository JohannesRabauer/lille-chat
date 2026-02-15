package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
public class MainView extends VerticalLayout {

    public MainView(ChatView chatView) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        add(chatView);
    }
}
