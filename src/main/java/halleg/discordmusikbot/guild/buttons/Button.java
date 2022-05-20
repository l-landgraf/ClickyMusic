package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

public abstract class Button {

    protected GuildHandler handler;
    protected String emoji;
    protected String description;
    protected boolean connectedOnly;

    protected abstract void run(Message message, QueuePlayer player, MessageReaction react, Member member);

    public Button(GuildHandler handler, String emoji, boolean connectedOnly, String description) {
        this.handler = handler;
        this.emoji = emoji;
        this.connectedOnly = connectedOnly;
        this.description = description;
    }

    public boolean check(Message message, MessageReaction react, Member member, String emoji) {
        if (!emoji.equalsIgnoreCase(this.emoji)) {
            return false;
        }

        if (this.connectedOnly && !this.handler.isCorrectChannel(member.getVoiceState().getChannel())) {
            return false;
        }

        this.handler.log("executing button: " + react.getReactionEmote().getEmoji());
        run(message, this.handler.getPlayer(), react, member);
        return true;

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
