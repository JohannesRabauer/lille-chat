package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("logged-out")
public class LogoutView extends VerticalLayout {

    public LogoutView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Logged Out");
        Paragraph message = new Paragraph("You have been logged out successfully.");
        
        add(title, message);
    }
}
