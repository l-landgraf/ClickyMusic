package halleg.discordmusikbot.guild.player.queue;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SingleQueueElement extends QueueElement {
	private Track track;

	public SingleQueueElement(QueuePlayer player, Track track) {
		super(player);
		this.track = track;
	}

	@Override
	public MessageEmbed buildMessageEmbed(QueueStatus status) {
		super.buildMessageEmbed(status);
		EmbedBuilder eb = new EmbedBuilder();
		try {
			eb.setTitle(this.track.getTitle(), this.track.getURI());
		} catch (IllegalArgumentException e) {
			eb.setTitle(this.track.getTitle());
		}
		eb.setDescription("Queued by " + this.track.getMember().getAsMention());

		try {

			eb.setThumbnail(this.track.getThumbnail());
		} catch (IllegalArgumentException e) {
		}

		eb.addField("By", this.track.getAuthorEmbedLink(), true);
		eb.addField("Length", this.track.getLength(), true);
		return eb.build();
	}

	@Override
	public void onQueued() {
		super.onQueued();
		this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
		this.message.addReaction(GuildHandler.REMOVE_EMOJI).queue();
	}

	@Override
	public void onPlaying() {
		super.onPlaying();
		this.player.playTrack(this.track.getTrack());
		this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
		this.message.addReaction(GuildHandler.BACK_EMOJI).queue();
		this.message.addReaction(GuildHandler.RESUME_PAUSE_EMOJI).queue();
		this.message.addReaction(GuildHandler.SKIP_EMOJI).queue();
	}

	@Override
	public void onPlayed() {
		super.onPlayed();
		this.message.clearReactions(GuildHandler.SKIP_EMOJI).queue();
		this.message.clearReactions(GuildHandler.RESUME_PAUSE_EMOJI).queue();
		this.message.clearReactions(GuildHandler.BACK_EMOJI).queue();
		this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		this.message.clearReactions(GuildHandler.REMOVE_ALL_EMOJI).queue();
		this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
	}

	@Override
	public void onResumePause() {
		this.player.togglePaused();
	}

	@Override
	public void onEnded() {
		super.onEnded();
		this.player.nextTrack();
	}

	@Override
	public void onDelete() {
		super.onDelete();
		this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		this.player.removeElement(this);
	}

	@Override
	public void onBack() {
		super.onBack();
		this.player.playTrack(this.track.getTrack());
	}

	@Override
	public void onShuffle() {
		throw new IllegalStateException();
	}

	@Override
	public void onDeletePlaylist() {
		throw new IllegalStateException();
	}
}
