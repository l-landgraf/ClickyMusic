package halleg.discordmusikbot;

import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DisconnectListener extends ListenerAdapter {
    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println("Disconnected. shutting down.");
        System.exit(1);
    }
}
