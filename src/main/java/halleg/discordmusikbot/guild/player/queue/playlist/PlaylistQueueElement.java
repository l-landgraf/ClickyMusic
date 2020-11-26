package halleg.discordmusikbot.guild.player.queue.playlist;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import halleg.discordmusikbot.guild.player.queue.QueueStatus;
import halleg.discordmusikbot.guild.player.tracks.Track;
import halleg.discordmusikbot.guild.player.tracks.TrackPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class PlaylistQueueElement<L extends TrackPlaylist> extends QueueElement {
	protected L playlist;
	protected List<Integer> plannedShuffle;
	protected List<Integer> plannedNormal;

	protected int currentTrack;
	protected boolean shuffle;

	public PlaylistQueueElement(Player player, L playlist) {
		super(player);
		this.playlist = playlist;
		this.currentTrack = 0;

		this.plannedShuffle = IntStream.range(0, this.playlist.getTotal()).boxed().collect(Collectors.toList());
		Collections.shuffle(this.plannedShuffle);

		this.plannedNormal = IntStream.range(0, this.playlist.getTotal()).boxed().collect(Collectors.toList());
	}

	@Override
	public MessageEmbed buildMessageEmbed(QueueStatus status) {
		super.buildMessageEmbed(status);
		EmbedBuilder eb = new EmbedBuilder();

		addPlaylistRow(eb);
		if (status.getKeepLoading()) {
			addNowPlayingRow(eb);
			addCommingUpRows(eb);
		}

		return eb.build();
	}

	protected void updateMessage() {
		if (this.message == null) {
			return;
		}
		this.message.editMessage(buildMessageEmbed(this.status)).queue(new Consumer<>() {
			@Override
			public void accept(Message message) {
				PlaylistQueueElement.this.message = message;
			}
		});
	}

	protected void addPlaylistRow(EmbedBuilder eb) {
		eb.setTitle(this.playlist.getTitle(), this.playlist.getURI());
		eb.setDescription("Queued by " + this.playlist.getMember().getAsMention());
		if (this.playlist.getThumbnail() != null) {
			eb.setThumbnail(this.playlist.getThumbnail());
		}

		if (this.playlist.getAuthor() != null) {
			eb.addField("Playlist By", this.playlist.getAuthor(), true);
		}

		eb.addField("Songs", this.playlist.getTotal() + "", true);

		//add padding after songs
		if (this.playlist.getAuthor() == null) {
			eb.addField("", "", true);
		}

		eb.addField("", "", true);
	}

	protected void addNowPlayingRow(EmbedBuilder eb) {
		if (this.currentTrack < this.playlist.getTotal()) {
			eb.setThumbnail(getCurrentTrack().getThumbnail());
			String title = "";

			if (this.status == QueueStatus.QUEUED) {
				title = "First Song";
			} else {
				title = "Now Playing";
			}

			String moreShuffle = "";
			eb.addField(title, getCurrentTrack().getTitleEmbedLink() + moreShuffle, true);
			eb.addField("By", getCurrentTrack().getAuthorEmbedLink(), true);
			eb.addField("Length", getCurrentTrack().getLength(), true);
		}
	}

	protected void addCommingUpRows(EmbedBuilder eb) {
		String s = "";
		List<Integer> plan = getPlanedSongs();
		int counter = 0;
		for (int i = this.currentTrack + 1; i < plan.size(); i++) {
			if (counter >= GuildHandler.PLAYLIST_PREVIEW_MAX) {
				break;
			}
			counter++;
			String title = this.playlist.getTrack(plan.get(i)).getTitleEmbedLink();
			s += plan.get(i) + ". " + title + "\n";
		}

		int songsLeft = (this.playlist.getTotal() - this.currentTrack + 1);
		System.out.println(songsLeft);
		if (songsLeft > 0) {
			if (this.shuffle) {
				s += "**-- Shuffling " + songsLeft + " More --**";
			} else {
				s += "**-- " + songsLeft + " More --**";
			}

		}

		if (counter > 0) {
			eb.addField("Comming up", s, false);
		}
	}

	protected int getSong(int songNr) {
		try {
			return getPlanedSongs().get(songNr);
		} catch (IndexOutOfBoundsException e) {
			return -1;
		}
	}

	protected List<Integer> getPlanedSongs() {
		if (this.shuffle) {
			return getPlanedShuffle();
		} else {
			return getPlanedNormal();
		}
	}

	public List<Integer> getPlanedNormal() {
		return this.plannedNormal;
	}

	public List<Integer> getPlanedShuffle() {
		return this.plannedShuffle;
	}

	protected void nextInternal() {
		this.currentTrack++;
		int planned = getSong(this.currentTrack);
		if (planned < 0) {
			super.onSkip();
			return;
		}
		this.status = QueueStatus.PLAYING;


		playCurrent();
	}

	protected void prevInternal() {
		this.currentTrack--;
		int planned = getSong(this.currentTrack);
		if (planned < 0) {
			this.currentTrack = 0;
			planned = getSong(this.currentTrack);
		}
		this.status = QueueStatus.PLAYING;


		playCurrent();
	}

	protected void playCurrent() {
		this.player.playTrack(getCurrentTrack().getTrack());
		updateMessage();
	}

	@Override
	public void onShuffle() {
		this.shuffle = !this.shuffle;
		updateMessage();
	}

	@Override
	public void onQueued() {
		super.onQueued();
		this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
		this.message.addReaction(GuildHandler.REMOVE_EMOJI).queue();
	}

	@Override
	public synchronized void onPlaying() {
		playCurrent();
		super.onPlaying();
		this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
		this.message.addReaction(GuildHandler.BACK_EMOJI).queue();
		this.message.addReaction(GuildHandler.RESUME_PAUSE_EMOJI).queue();
		this.message.addReaction(GuildHandler.SKIP_EMOJI).queue();
		this.message.addReaction(GuildHandler.SHUFFLE_EMOJI).queue();
		this.message.addReaction(GuildHandler.REMOVE_ALL_EMOJI).queue();
	}

	@Override
	public void onPlayed() {
		super.onPlayed();
		updateMessage();
		this.message.clearReactions(GuildHandler.REMOVE_ALL_EMOJI).queue();
		this.message.clearReactions(GuildHandler.SHUFFLE_EMOJI).queue();
		this.message.clearReactions(GuildHandler.SKIP_EMOJI).queue();
		this.message.clearReactions(GuildHandler.RESUME_PAUSE_EMOJI).queue();
		this.message.clearReactions(GuildHandler.BACK_EMOJI).queue();
		this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
	}

	@Override
	public void onResumePause() {
		this.player.togglePaused();
	}

	@Override
	public void onEnded() {
		nextInternal();
	}

	@Override
	public void onSkip() {
		nextInternal();
	}

	@Override
	public void onBack() {
		super.onBack();
		prevInternal();
	}

	@Override
	public void onDelete() {
		super.onDelete();
		updateMessage();
		this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
		this.player.removeElement(this);
	}

	@Override
	public void onDeletePlaylist() {
		super.onDelete();
		this.player.nextTrack();
	}

	@Override
	public void runPlay(int i) {
		int planned = getSong(i);
		if (planned < 0) {
			this.player.getHandler().sendErrorMessage("SongNr out of Bounds.");
			return;
		}
		this.currentTrack = i;
		playCurrent();
	}

	public Track getCurrentTrack() {
		return this.playlist.getTrack(this.currentTrack);
	}

	public int getTotal() {
		return this.playlist.getTotal();
	}

}
