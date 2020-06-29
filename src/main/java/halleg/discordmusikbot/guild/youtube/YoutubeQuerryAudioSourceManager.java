package halleg.discordmusikbot.guild.youtube;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class YoutubeQuerryAudioSourceManager implements AudioSourceManager {

    private static final String SPOTIFY_DOMAIN = "open.spotify.com";
    private YoutubeAudioSourceManager ytManager;


    public YoutubeQuerryAudioSourceManager(YoutubeAudioSourceManager ytManager) {
        this.ytManager = ytManager;
    }

    @Override
    public String getSourceName() {
        return "Youtube Playlist";
    }

    private AudioTrack buildTrackFromInfo(AudioTrackInfo info) {
        return new YoutubeAudioTrack(info, this.ytManager);
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {

        YoutubeSearchProvider searcher = new YoutubeSearchProvider();

        BasicAudioPlaylist results = (BasicAudioPlaylist) searcher.loadSearchResult(reference.identifier, YoutubeQuerryAudioSourceManager.this::buildTrackFromInfo);
        AudioItem item = results.getTracks().get(0);
        return item;
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