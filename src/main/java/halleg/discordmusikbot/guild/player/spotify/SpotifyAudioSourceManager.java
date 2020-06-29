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
            String playlistID = reference.identifier.replace(PLAYLIST_PREFIX, "");
            return loadPlaylist(playlistID);
        }

        if (reference.identifier.startsWith(ALBUM_PREFIX)) {
            String playlistID = reference.identifier.replace(ALBUM_PREFIX, "");
            return loadAlbum(playlistID);
        }

        return null;
    }

    private SpotifyAudioPlaylist loadPlaylist(String playlistId) {
        Playlist playlist = SpotifyApi.loadPlaylist(playlistId);
        SpotifyAudioPlaylist list = new SpotifyAudioPlaylist(playlist.getName(), playlist.getOwner().getDisplayName(), playlist.getImages()[0].getUrl());

        for (PlaylistTrack t : playlist.getTracks().getItems()) {
            String search = t.getTrack().getName();
            addToList(search, list);
        }
        return list;
    }

    private SpotifyAudioPlaylist loadAlbum(String albumId) {
        Album album = SpotifyApi.loadAlbum(albumId);
        String artists = "";
        for (ArtistSimplified a : album.getArtists()) {
            artists += a.getName() + " ";
        }
        SpotifyAudioPlaylist list = new SpotifyAudioPlaylist(album.getName(), artists, album.getImages()[0].getUrl());

        for (TrackSimplified t : album.getTracks().getItems()) {
            String search = t.getName();
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
        if (source.startsWith(PLAYLIST_PREFIX) || source.startsWith(ALBUM_PREFIX)) {
            String initSource = "";
            return new PreLoadHandler(handler, source, initSource, member, message);
        }
        return null;
    }
}