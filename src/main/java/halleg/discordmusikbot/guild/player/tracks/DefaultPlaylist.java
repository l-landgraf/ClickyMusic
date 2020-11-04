package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import halleg.discordmusikbot.guild.spotify.SpotifyAudioPlaylist;
import halleg.discordmusikbot.guild.youtube.MyYoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Member;

public class DefaultPlaylist extends TrackPlaylist<Track> {
	public static DefaultPlaylist tryBuildFormSpotifyPlaylist(AudioPlaylist list, Member member, String uri) {
		if (list instanceof SpotifyAudioPlaylist) {
			SpotifyAudioPlaylist spot = (SpotifyAudioPlaylist) list;
			return new DefaultPlaylist(list, spot.getAuthor(), spot.getThumbnail(), member, uri);
		} else {
			return new DefaultPlaylist(list, member, uri);
		}
	}

	public DefaultPlaylist(AudioPlaylist list, String author, String thumbnail, Member member, String uri) {
		super(list.getName(), author, thumbnail, uri, member);
		this.tracks = new Track[list.getTracks().size()];
		for (int i = 0; i < this.tracks.length; i++) {
			this.tracks[i] = new Track(list.getTracks().get(i), member, null,
					MyYoutubeAudioSourceManager.loadThumbnail(list.getTracks().get(0)));
		}
	}

	public DefaultPlaylist(AudioPlaylist list, Member member, String uri) {
		super(list.getName(), null, null, uri, member);
		this.tracks = new Track[list.getTracks().size()];
		for (int i = 0; i < this.tracks.length; i++) {
			this.tracks[i] = new Track(list.getTracks().get(i), member, null,
					MyYoutubeAudioSourceManager.loadThumbnail(list.getTracks().get(0)));
		}
	}
}
