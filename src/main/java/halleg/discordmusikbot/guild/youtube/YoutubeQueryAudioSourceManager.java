package halleg.discordmusikbot.guild.youtube;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class YoutubeQueryAudioSourceManager implements AudioSourceManager {

	private static final String SPOTIFY_DOMAIN = "open.spotify.com";
	private YoutubeAudioSourceManager ytManager;
	YoutubeSearchProvider searcher;


	public YoutubeQueryAudioSourceManager(YoutubeAudioSourceManager ytManager) {
		this.ytManager = ytManager;
		this.searcher = new RetryYoutubeSearchProvider();
	}

	@Override
	public String getSourceName() {
		return "Youtube Playlist";
	}

	private AudioTrack buildTrackFromInfo(AudioTrackInfo info) {
		return new YoutubeAudioTrack(info, this.ytManager);
	}

	@Override
	public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {

		AudioItem results = this.searcher.loadSearchResult(reference.identifier, YoutubeQueryAudioSourceManager.this::buildTrackFromInfo);
		if (results instanceof BasicAudioPlaylist) {
			return ((BasicAudioPlaylist) results).getTracks().get(0);
		} else {
			return results;
		}
	}

	@Override
	public boolean isTrackEncodable(AudioTrack track) {
		return false;
	}

	@Override
	public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
		throw new UnsupportedOperationException("encodeTrack is unsupported.");
	}

	@Override
	public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
		throw new UnsupportedOperationException("decodeTrack is unsupported.");
	}

	@Override
	public void shutdown() {

	}
}