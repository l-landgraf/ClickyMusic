package halleg.discordmusikbot.guild.spotify;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.specification.*;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.TrackLoader;
import halleg.discordmusikbot.guild.loader.InititalPlaylistLoadHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.youtube.RetryYoutubeSearchProvider;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SpotifyAudioSourceManager implements AudioSourceManager, TrackLoader.PlaylistPreloadManager {

    public static final String PLAYLIST_PREFIX = "https://open.spotify.com/playlist/";
    public static final String ALBUM_PREFIX = "https://open.spotify.com/album/";
    public static final String TRACK_PREFIX = "https://open.spotify.com/track/";
    private static final int MAX_CHECKS = 5;
    private static final int SONG_LENGTH_DIFFERENCE = 2;
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
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (reference.identifier.startsWith(PLAYLIST_PREFIX)) {
            String playlistId = extractId(reference.identifier);
            return loadPlaylist(playlistId);
        }

        if (reference.identifier.startsWith(ALBUM_PREFIX)) {
            String albumId = extractId(reference.identifier);
            return loadAlbum(albumId);
        }

        if (reference.identifier.startsWith(TRACK_PREFIX)) {
            String trackId = extractId(reference.identifier);
            return loadTrack(trackId);
        }

        return null;
    }

    private String extractId(String url) {
        url = url.split("\\?")[0];
        url = url.replace(PLAYLIST_PREFIX, "");
        url = url.replace(ALBUM_PREFIX, "");
        url = url.replace(TRACK_PREFIX, "");
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
            String artists = getArtists(t.getTrack());
            String search = t.getTrack().getName() + artists;
            addToList(search, t.getTrack().getDurationMs(), list);
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
            addToList(search, t.getDurationMs(), list);
        }
        return list;
    }

    private AudioItem loadTrack(String trackId) {
        Track track = SpotifyApi.loadTrack(trackId);
        if (track == null) {
            return null;
        }

        String search = track.getName() + getArtists(track);
        AudioItem item = getTrack(search, track.getDurationMs());
        return item;
    }

    private void addToList(String search, int dirationMs, SpotifyAudioPlaylist list) {
        addToList(search, dirationMs, list, 5);
    }

    private void addToList(String search, int durationMs, SpotifyAudioPlaylist list, int i) {
        try {
            AudioItem item = getTrack(search, durationMs);
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
            addToList(search, durationMs, list, i - 1);
        }
    }

    private AudioItem getTrack(String search, int durationMs) {
        BasicAudioPlaylist results = (BasicAudioPlaylist) this.searchProvider.loadSearchResult(search,
                SpotifyAudioSourceManager.this::buildTrackFromInfo);

        for (int i = 0; i < MAX_CHECKS; i++) {
            AudioTrack item = results.getTracks().get(i);
            if (item.getDuration() < durationMs * SONG_LENGTH_DIFFERENCE) {
                return item;
            }
        }

        return results.getTracks().get(0);
    }

    public String getArtists(ArtistSimplified[] arts) {
        String artists = "";
        for (ArtistSimplified a : arts) {
            artists += " " + a.getName();
        }
        return artists;
    }

    public String getArtists(IPlaylistItem item) {
        String artists = "";
        if (item instanceof Track) {
            return getArtists(((Track) item).getArtists());
        }

        return artists;
    }

    public String getImage(IPlaylistItem item) {
        String artists = "";
        if (item instanceof Track) {
            return ((Track) item).getAlbum().getImages()[0].getUrl();
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

            return preLoadPlaylist(playlistId, handler.getPlayer(), handler, source, member, message);
        }

        if (source.startsWith(ALBUM_PREFIX)) {
            String playlistId = extractId(source);
            return preLoadAlbum(playlistId, handler.getPlayer(), handler, source, member, message);
        }
        return null;
    }

    private InititalPlaylistLoadHandler preLoadPlaylist(String playlistId, QueuePlayer player, GuildHandler handler, String source,
                                                        Member member, Message message) {
        Playlist playlist = SpotifyApi.loadPlaylist(playlistId);
        if (playlist == null) {
            return null;
        }
        PlaylistTrack t = playlist.getTracks().getItems()[0];
        String artists = getArtists(t.getTrack());

        String search = t.getTrack().getName() + artists;

        String[] images = new String[playlist.getTracks().getTotal()];
        String[] sources = new String[playlist.getTracks().getTotal()];
        PlaylistTrack[] tracks = SpotifyApi.loadPlaylistTracks(playlistId, playlist.getTracks().getTotal());
        for (int i = 0; i < tracks.length; i++) {
            sources[i] = tracks[i].getTrack().getName() + getArtists(tracks[i].getTrack());
            try {
                images[i] = getImage(tracks[i].getTrack());
            } catch (ArrayIndexOutOfBoundsException e) {
                handler.log("cant find images for song nr " + i + " (" + tracks[i].getTrack().getName() + ")");
            }
        }

        return new InititalPlaylistLoadHandler(handler, player, source, search, member, message, sources, images,
                playlist.getName(), getUserLink(playlist.getOwner()), playlist.getImages()[0].getUrl());
    }

    private InititalPlaylistLoadHandler preLoadAlbum(String albumId, QueuePlayer player, GuildHandler handler, String source, Member member,
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
            try {
                images[i] = album.getImages()[0].getUrl();
            } catch (ArrayIndexOutOfBoundsException e) {
                handler.log("cant find images for song nr " + i + " (" + tracks[i].getName() + ")");
            }
        }

        return new InititalPlaylistLoadHandler(handler, player, source, search, member, message, sources, images,
                album.getName(), artists, album.getImages()[0].getUrl());
    }

    private String getUserLink(User own) {
        String link = own.getHref().substring("https://api.spotify.com/v1/users/".length());
        link = "https://open.spotify.com/user/" + link;
        return "[" + own.getDisplayName() + "](" + link + ")";
    }
}