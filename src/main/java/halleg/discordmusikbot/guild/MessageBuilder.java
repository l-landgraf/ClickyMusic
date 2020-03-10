package halleg.discordmusikbot.guild;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import halleg.discordmusikbot.buttons.Button;
import halleg.discordmusikbot.commands.Command;
import halleg.discordmusikbot.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class MessageBuilder {
	private GuildHandler handler;

	public MessageBuilder(GuildHandler handler) {
		this.handler = handler;
	}

	public MessageEmbed buildNewQueueMessage(Track track) {
		EmbedBuilder eb = new EmbedBuilder();
		AudioTrackInfo info = track.getTrack().getInfo();

		eb.setTitle(info.title, info.uri);
		eb.setDescription("Queued by " + track.getMember().getAsMention());

		if (track.hasThumbnail()) {
			eb.setThumbnail(track.getThumbnail());
		}

		eb.addField("By", info.author, true);
		eb.addField("Length", toTime(info.length), true);
		eb.addField("URI", "`" + info.uri + "`", false);

		String foot = "";
		for (Button button : this.handler.getButtons().getButtons()) {
			foot += button.getEmoji() + " " + button.getDescription() + "\n";
		}

		eb.setFooter(foot);
		return eb.build();
	}

	private String toTime(long length) {
		String sec = Long.toString((length / 1000l) % 60l);
		if (sec.length() < 2) {
			sec = "0" + sec;
		}

		String min = Long.toString((length / 60000));
		if (sec.length() < 2) {
			sec = "0" + sec;
		}

		return min + ":" + sec;
	}

	public void setQueue(Message m) {
		if (!this.handler.reactionPermissionCheck()) {
			return;
		}

		m.addReaction(GuildHandler.REPEAT_EMOJI).queue(null, null);
		m.addReaction(GuildHandler.REMOVE_EMOJI).queue(null, null);
	}

	public void setPlaying(Message m) {
		if (!this.handler.reactionPermissionCheck()) {
			return;
		}

		m.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		m.addReaction(GuildHandler.REPEAT_EMOJI).queue();
		m.addReaction(GuildHandler.RESUME_EMOJI).queue();
		m.addReaction(GuildHandler.PAUSE_EMOJI).queue();
		m.addReaction(GuildHandler.SKIP_EMOJI).queue();

	}

	public void setPlayed(Message m) {
		if (!this.handler.reactionPermissionCheck()) {
			return;
		}

		m.clearReactions(GuildHandler.REMOVE_EMOJI).queue(null, null);
		m.clearReactions(GuildHandler.RESUME_EMOJI).queue(null, null);
		m.clearReactions(GuildHandler.PAUSE_EMOJI).queue(null, null);
		m.clearReactions(GuildHandler.SKIP_EMOJI).queue(null, null);
		m.addReaction(GuildHandler.REPEAT_EMOJI).queue(null, null);
	}

	public void setRemoved(Message m, Member remover) {
		if (!this.handler.reactionPermissionCheck()) {
			return;
		}

		setPlayed(m);

		MessageEmbed embed = m.getEmbeds().get(0);
		EmbedBuilder builder = new EmbedBuilder(embed);
		builder.setColor(Color.RED);
		m.editMessage(builder.build()).queue();
	}

	public MessageEmbed buildNewErrorMessage(String error) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("💀 Error");
		eb.setDescription(error);

		return eb.build();
	}

	public String getURI(Message message) {
		try {
			return message.getEmbeds().get(0).getFields().get(2).getValue().replace("`", "");
		} catch (Exception e) {
			return "";
		}

	}

	public MessageEmbed buildHelpMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Avilable Commands");
		eb.setDescription("All commands have to start with `" + this.handler.getPrefix()
				+ "` and most must be in this channel.\n" + "To start a new track simply write in this channel.\n"
				+ "You can also click the reactions to perform actions.");

		for (Command command : this.handler.getCommands().getCommands()) {
			eb.addField(command.getTip(), command.getDescription(), false);
		}

		return eb.build();
	}

	public MessageEmbed buildInfoMessage(String message) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("🔔 Info");
		eb.setDescription(message);

		return eb.build();
	}
}
