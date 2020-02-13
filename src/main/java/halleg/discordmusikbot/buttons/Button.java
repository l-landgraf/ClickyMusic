package halleg.discordmusikbot.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

public abstract class Button {

	protected GuildHandler handler;
	protected String emoji;
	protected String description;

	protected abstract void run(Message message, MessageReaction react, Member member);

	public Button(GuildHandler handler, String emoji, String description) {
		this.handler = handler;
		this.emoji = emoji;
		this.description = description;
	}

	public boolean check(Message message, MessageReaction react, Member member, String emoji) {
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
