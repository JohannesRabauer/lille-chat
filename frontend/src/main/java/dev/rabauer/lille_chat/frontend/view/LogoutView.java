package dev.rabauer.lille_chat.frontend.view;

import com.vaadin.flow.component.html.Anchor;
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
        title.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lille-text)");

        Paragraph message = new Paragraph("You have been logged out successfully.");
        message.getStyle().set("color", "var(--lille-text-secondary)");

        Anchor loginLink = new Anchor("/", "Return to Login");
        loginLink.getStyle()
                .set("color", "var(--lille-primary)")
                .set("text-decoration", "none")
                .set("font-weight", "500")
                .set("margin-top", "1rem");

        add(title, message, loginLink);
    }
}
