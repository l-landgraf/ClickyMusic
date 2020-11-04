package halleg.discordmusikbot.guild.spotify;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.model_objects.specification.User;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.TrackLoader;
import halleg.discordmusikbot.guild.loader.InititalPlaylistLoadHandler;
import halleg.discordmusikbot.guild.youtube.RetryYoutubeSearchProvider;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class SpotifyAudioSourceManager implements AudioSourceManager, TrackLoader.PlaylistPreloadManager {

	public static final String PLAYLIST_PREFIX = "https://open.spotify.com/playlist/";
	public static final String ALBUM_PREFIX = "https://open.spotify.com/album/";
	private YoutubeAudioSourceManager ytManager;
	private YoutubeSearchProvider searchProvider;

	public SpotifyAudioSourceManager(YoutubeAudioSourceManager ytManager) {
		this.ytManager = ytManager;
		this.searchProvider = new RetryYoutubeSearchProvider();
	}

	@Override
	public String getSourceName() {
		return "spotify";
	}

	private AudioTrack buildTrackFromInfo(AudioTrackInfo info) {
		return new YoutubeAudioTrack(info, this.ytManager);
	}

	@Override
	public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
		if (reference.identifier.startsWith(PLAYLIST_PREFIX)) {
			String playlistId = extractId(reference.identifier);
			return loadPlaylist(playlistId);
		}

		if (reference.identifier.startsWith(ALBUM_PREFIX)) {
			String albumId = extractId(reference.identifier);
			return loadAlbum(albumId);
		}

		return null;
	}

	private String extractId(String url) {
		url = url.split("\\?")[0];
		url = url.replace(PLAYLIST_PREFIX, "");
		url = url.replace(ALBUM_PREFIX, "");
		return url;
	}

	private SpotifyAudioPlaylist loadPlaylist(String playlistId) {
		Playlist playlist = SpotifyApi.loadPlaylist(playlistId);
		if (playlist == null) {
			return null;
		}
		SpotifyAudioPlaylist list = new SpotifyAudioPlaylist(playlist.getName(), playlist.getOwner().getDisplayName(),
				playlist.getImages()[0].getUrl());
		int i = 0;
		for (PlaylistTrack t : SpotifyApi.loadPlaylistTracks(playlistId, playlist.getTracks().getTotal())) {
			String artists = getArtists(t.getTrack().getArtists());
			String search = t.getTrack().getName() + artists;
			addToList(search, list);
			i++;
			System.out.println(i + "/" + playlist.getTracks().getTotal());
		}
		return list;
	}

	private SpotifyAudioPlaylist loadAlbum(String albumId) {
		Album album = SpotifyApi.loadAlbum(albumId);
		if (album == null) {
			return null;
		}
		String artists = "";
		for (ArtistSimplified a : album.getArtists()) {
			artists += " " + a.getName();
		}
		SpotifyAudioPlaylist list = new SpotifyAudioPlaylist(album.getName(), artists, album.getImages()[0].getUrl());

		for (TrackSimplified t : album.getTracks().getItems()) {
			String search = t.getName() + artists;
			addToList(search, list);
		}
		return list;
	}

	private void addToList(String search, SpotifyAudioPlaylist list) {
		addToList(search, list, 5);
	}

	private void addToList(String search, SpotifyAudioPlaylist list, int i) {
		try {
			BasicAudioPlaylist results = (BasicAudioPlaylist) this.searchProvider.loadSearchResult(search,
					SpotifyAudioSourceManager.this::buildTrackFromInfo);
			AudioItem item = results.getTracks().get(0);
			list.add((AudioTrack) item);
		} catch (Exception e) {
			if (i <= 0) {
				return;
			}
			try {
				Thread.sleep(100l);
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}
			addToList(search, list, i - 1);
		}
	}

	public String getArtists(ArtistSimplified[] arts) {
		String artists = "";
		for (ArtistSimplified a : arts) {
			artists += " " + a.getName();
		}
		return artists;
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

	@Override
	public InititalPlaylistLoadHandler load(GuildHandler handler, String source, Member member, Message message) {
		if (source.startsWith(PLAYLIST_PREFIX)) {
			String playlistId = extractId(source);
			return preLoadPlaylist(playlistId, handler, source, member, message);
		}

		if (source.startsWith(ALBUM_PREFIX)) {
			String playlistId = extractId(source);
			return preLoadAlbum(playlistId, handler, source, member, message);
		}
		return null;
	}

	private InititalPlaylistLoadHandler preLoadPlaylist(String playlistId, GuildHandler handler, String source,
			Member member, Message message) {
		Playlist playlist = SpotifyApi.loadPlaylist(playlistId);
		if (playlist == null) {
			return null;
		}
		PlaylistTrack t = playlist.getTracks().getItems()[0];
		String artists = getArtists(t.getTrack().getArtists());

		String search = t.getTrack().getName() + artists;

		String[] images = new String[playlist.getTracks().getTotal()];
		String[] sources = new String[playlist.getTracks().getTotal()];
		PlaylistTrack[] tracks = SpotifyApi.loadPlaylistTracks(playlistId, playlist.getTracks().getTotal());
		for (int i = 0; i < tracks.length; i++) {
			sources[i] = tracks[i].getTrack().getName() + getArtists(tracks[i].getTrack().getArtists());
			images[i] = tracks[i].getTrack().getAlbum().getImages()[0].getUrl();
		}

		return new InititalPlaylistLoadHandler(handler, source, search, member, message, sources, images,
				playlist.getName(), getUserLink(playlist.getOwner()), playlist.getImages()[0].getUrl());
	}

	private InititalPlaylistLoadHandler preLoadAlbum(String albumId, GuildHandler handler, String source, Member member,
			Message message) {
		Album album = SpotifyApi.loadAlbum(albumId);
		if (album == null) {
			return null;
		}

		TrackSimplified t = album.getTracks().getItems()[0];
		String artists = getArtists(t.getArtists());

		String search = t.getName() + artists;

		String[] images = new String[album.getTracks().getTotal()];
		String[] sources = new String[album.getTracks().getTotal()];
		TrackSimplified[] tracks = SpotifyApi.loadAlbumTracks(albumId, album.getTracks().getTotal());
		for (int i = 0; i < tracks.length; i++) {
			sources[i] = tracks[i].getName() + getArtists(tracks[i].getArtists());
			images[i] = album.getImages()[0].getUrl();
		}

		return new InititalPlaylistLoadHandler(handler, source, search, member, message, sources, images,
				album.getName(), artists, album.getImages()[0].getUrl());
	}

	private String getUserLink(User own) {
		String link = own.getHref().substring("https://api.spotify.com/v1/users/".length());
		link = "https://open.spotify.com/user/" + link;
		// return link;
		return "[" + own.getDisplayName() + "](" + link + ")";
	}
}