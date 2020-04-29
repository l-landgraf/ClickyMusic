package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

public abstract class Button {

    protected GuildHandler handler;
    protected String emoji;
    protected String description;
    protected boolean connectedOnly;

    protected abstract void run(Message message, MessageReaction react, Member member);

    public Button(GuildHandler handler, String emoji, boolean connectedOnly, String description) {
        this.handler = handler;
        this.emoji = emoji;
        this.connectedOnly = connectedOnly;
        this.description = description;
    }

    public boolean check(Message message, MessageReaction react, Member member, String emoji) {
        if (message.getChannel().getIdLong() != this.handler.getChannel().getIdLong()) {
            return false;
        }


        try {

            if (this.connectedOnly && this.handler.getPlayer().getConnectedChannel() == null) {
                return false;
            }

            if (member.getVoiceState().getChannel() == null) {
                return false;
            }
            if (this.handler.getPlayer().getConnectedChannel() != null && member.getVoiceState().getChannel().getIdLong() != this.handler.getPlayer().getConnectedChannel().getIdLong()) {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }

        if (emoji.equalsIgnoreCase(this.emoji)) {
            this.handler.log("executing button: " + react.getReactionEmote().getEmoji());
            run(message, react, member);
            return true;
        }
        return false;
    }

    public GuildHandler getHandler() {
        return this.handler;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public String getDescription() {
        return this.description;
    }

}
