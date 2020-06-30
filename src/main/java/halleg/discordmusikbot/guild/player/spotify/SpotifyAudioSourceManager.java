package halleg.discordmusikbot.guild.player.spotify;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.model_objects.specification.*;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.TrackLoader;
import halleg.discordmusikbot.guild.loader.PreLoadHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SpotifyAudioSourceManager implements AudioSourceManager, TrackLoader.PlaylistPreloadManager {

    public static final String PLAYLIST_PREFIX = "https://open.spotify.com/playlist/";
    public static final String ALBUM_PREFIX = "https://open.spotify.com/album/";
    private YoutubeAudioSourceManager ytManager;
    private YoutubeSearchProvider searchProvider;


    public SpotifyAudioSourceManager(YoutubeAudioSourceManager ytManager) {
        this.ytManager = ytManager;
        this.searchProvider = new YoutubeSearchProvider();
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
        SpotifyAudioPlaylist list = new SpotifyAudioPlaylist(playlist.getName(), playlist.getOwner().getDisplayName(), playlist.getImages()[0].getUrl());
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
            BasicAudioPlaylist results = (BasicAudioPlaylist) this.searchProvider.loadSearchResult(search, SpotifyAudioSourceManager.this::buildTrackFromInfo);
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
    public PreLoadHandler load(GuildHandler handler, String source, Member member, Message message) {
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

    private PreLoadHandler preLoadPlaylist(String playlistId, GuildHandler handler, String source, Member member, Message message) {
        Playlist playlist = SpotifyApi.loadPlaylist(playlistId);
        if (playlist == null) {
            return null;
        }
        PlaylistTrack t = playlist.getTracks().getItems()[0];
        String artists = getArtists(t.getTrack().getArtists());

        String search = t.getTrack().getName() + artists;
        return new PreLoadHandler(handler, source, search, member, message, playlist.getTracks().getTotal(), playlist.getName(), playlist.getOwner().getDisplayName(), playlist.getImages()[0].getUrl());
    }

    private PreLoadHandler preLoadAlbum(String albumId, GuildHandler handler, String source, Member member, Message message) {
        Album album = SpotifyApi.loadAlbum(albumId);
        if (album == null) {
            return null;
        }

        TrackSimplified t = album.getTracks().getItems()[0];

        String artists = getArtists(t.getArtists());

        String search = t.getName() + artists;

        return new PreLoadHandler(handler, source, search, member, message, album.getTracks().getTotal(), album.getName(), artists, album.getImages()[0].getUrl());
    }
}