package halleg.discordmusikbot.buttons;

import java.util.ArrayList;
import java.util.List;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

public class ButtonManager {
	private List<Button> buttons;
	private GuildHandler handler;

	public ButtonManager(GuildHandler handler) {
		this.handler = handler;
		this.buttons = new ArrayList<Button>();

		this.buttons.add(new Button(handler, GuildHandler.REPEAT_EMOJI, "queue song again") {
			@Override
			protected void run(Message message, MessageReaction react, Member member) {
				this.handler.getPlayer().play(this.handler.getBuilder().getURI(message), member);
			}
		});

		this.buttons.add(new Button(handler, GuildHandler.REMOVE_EMOJI, "remove from queue") {
			@Override
			protected void run(Message message, MessageReaction react, Member member) {
				this.handler.getPlayer().removeElement(message, member);
			}
		});

		this.buttons.add(new Button(handler, GuildHandler.RESUME_EMOJI, "resume player") {
			@Override
			protected void run(Message message, MessageReaction react, Member member) {
				this.handler.getPlayer().setPaused(false);
			}
		});

		this.buttons.add(new Button(handler, GuildHandler.PAUSE_EMOJI, "pause player") {
			@Override
			protected void run(Message message, MessageReaction react, Member member) {
				this.handler.getPlayer().setPaused(true);
			}
		});

		this.buttons.add(new Button(handler, GuildHandler.SKIP_EMOJI, "skip current song") {
			@Override
			protected void run(Message message, MessageReaction react, Member member) {
				this.handler.getPlayer().nextTrack();
			}
		});
	}

	public boolean handleReaction(Message message, MessageReaction react, Member member) {
		for (MessageReaction r : message.getReactions()) {
			if (r.getReactionEmote().getEmoji().equalsIgnoreCase(react.getReactionEmote().getEmoji())) {
				if (!r.isSelf()) {
					return false;
				} else {
					break;
				}
			}
		}

		for (Button but : this.buttons) {
			if (but.check(message, react, member, react.getReactionEmote().getEmoji())) {
				return true;
			}
		}
		return false;
	}

	public List<Button> getButtons() {
		return this.buttons;
	}
}
