package dev.rabauer.lille_chat.frontend;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;

@Push
@Theme("lille-chat")
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("Lille Chat");
        settings.addMetaTag("viewport", "width=device-width, initial-scale=1.0");
        settings.addLink("preconnect", "https://fonts.googleapis.com");
        settings.addLink("preconnect", "https://fonts.gstatic.com");
        settings.addLink("stylesheet", "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");
    }
}
