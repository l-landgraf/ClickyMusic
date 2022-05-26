package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ButtonManager {
    private GuildHandler handler;

    public ButtonManager(GuildHandler handler) {
        this.handler = handler;
    }

    public void handleEvent(ButtonInteractionEvent event, QueuePlayer player) {
        for (MyButton b : MyButton.values()) {
            String eventId = event.getComponent().getId();
            if (b.getId().equals(eventId)) {
                event.reply("").queue();
                b.execute(event, player);
                return;
            }
        }
    }
}
