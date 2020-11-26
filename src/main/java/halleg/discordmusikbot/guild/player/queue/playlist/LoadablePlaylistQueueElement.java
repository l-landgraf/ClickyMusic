package halleg.discordmusikbot.guild.player.queue.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.loader.PlaylistTrackLoadHandler;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
import halleg.discordmusikbot.guild.player.tracks.LoadableTrack;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class LoadablePlaylistQueueElement extends PlaylistQueueElement<LoadablePlaylist> {
	private static final int PRELOAD_MAX = 5;
	protected GuildHandler handler;

	public LoadablePlaylistQueueElement(GuildHandler handler, AudioTrack firstTrack, String title, String author, String thumbnail, String uri, Member member, String[] sources, String[] images) {
		super(handler.getPlayer(), new LoadablePlaylist(title, author, thumbnail, uri, member, sources, images));
		this.handler = handler;
		this.playlist.getTrack(0).setTrack(firstTrack);
		loadPlannedTracks();
	}

	public void loadTrack(int trackNr, AudioTrack track, boolean update) {
		System.out.println(track.getInfo().title + " update: " + update);
		this.playlist.getTrack(trackNr).setTrack(track);
		if (update) {
			updateMessage();
		}
	}

	protected void loadPlannedTracks() {
		loadPlannedTracks(true);
		loadPlannedTracks(false);
	}

	protected void loadPlannedTracks(boolean shuffle) {
		List<Integer> planned = new ArrayList<>();
		if (shuffle) {
			planned = getPlanedShuffle();
		} else {
			planned = getPlanedNormal();
		}

		for (int i = this.currentTrack + 1; i < planned.size() && i < PRELOAD_MAX + this.currentTrack + 1; i++) {
			if (!this.playlist.getTrack(planned.get(i)).isLoaded()) {
				loadTrack(planned.get(i), this.shuffle == shuffle && i < PRELOAD_MAX + this.currentTrack + 1);
			}
		}
	}

	protected void loadTrack(int trackNr, boolean update) {
		this.handler.log("preloading Song Nr. " + trackNr + " " + update);
		LoadableTrack track = this.playlist.getTrack(trackNr);
		PlaylistTrackLoadHandler loader = new PlaylistTrackLoadHandler(this.handler, track.getSource(), track.getMember(),
				null, this, trackNr, update);
		this.handler.getLoader().load(this.playlist.getTrack(trackNr).getSource(), loader);
	}

	@Override
	protected void nextInternal() {
		super.nextInternal();
		loadPlannedTracks();
	}

	@Override
	public void onShuffle() {
		super.onShuffle();
		loadPlannedTracks();
	}

	@Override
	protected int getSong(int songNr) {
		int i = super.getSong(songNr);
		if (i < 0) {
			return i;
		}
		if (this.playlist.getTrack(i).isLoaded()) {
			return i;
		} else {
			return -1;
		}
	}

	@Override
	public void runPlay(int i) {
		int planned = super.getSong(i);
		if (planned < 0) {
			this.player.getHandler().sendErrorMessage("SongNr out of Bounds.");
			return;
		}
		if (!this.playlist.getTrack(i).isLoaded()) {
			this.player.getHandler().sendInfoMessage("\"" + this.playlist.getTrack(i).getSource() + "\"\nSong was not preloaded. Loading now. Please try again in a few Seconds.");
			loadTrack(i, false);
			return;
		}
		this.currentTrack = i;
		loadPlannedTracks();
		playCurrent();
	}
}
