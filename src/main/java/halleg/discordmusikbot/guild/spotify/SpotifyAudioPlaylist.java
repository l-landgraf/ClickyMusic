package halleg.discordmusikbot.guild.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.LinkedList;
import java.util.List;

public class SpotifyAudioPlaylist implements AudioPlaylist {

    private String name;
    private List<AudioTrack> tracks;
    private String author;
    private String thumbnail;

    public SpotifyAudioPlaylist(String name, String author, String thumbnail) {
        this.name = name;
        this.author = author;
        this.thumbnail = thumbnail;
        this.tracks = new LinkedList<>();
    }

    public void add(AudioTrack t) {
        this.tracks.add(t);
    }

    public String getAuthor() {
        return this.author;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<AudioTrack> getTracks() {
        return this.tracks;
    }

    @Override
    public AudioTrack getSelectedTrack() {
        return null;
    }

    @Override
    public boolean isSearchResult() {
        return false;
    }
}
