package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.commands.CommandType;
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
                event.deferEdit().queue();

                if (b.getType() == CommandType.SAME_CHANNEL && !this.handler.isSameAudioChannel(event.getMember().getVoiceState().getChannel())) {
                    this.handler.log("not reacting to Button due to Comannd type " + b.getType() + ".");
                    return;
                } else if (b.getType() == CommandType.FREE && !this.handler.isFreeAudioChannel(event.getMember().getVoiceState().getChannel())) {
                    this.handler.log("not reacting to Button due to Comannd type " + b.getType() + ".");
                    return;
                }

                this.handler.log("executing Button " + b.name() + ".");
                b.execute(event, player);
                return;
            }
        }

    }
}
