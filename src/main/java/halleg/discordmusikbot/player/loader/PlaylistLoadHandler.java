package halleg.discordmusikbot.player.loader;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.player.tracks.MyPlaylistTrack;

public class PlaylistLoadHandler implements AudioLoadResultHandler {
    private PlaylistLoadSynchonizer sync;
    private String querry;
    private int nr;

    public PlaylistLoadHandler(PlaylistLoadSynchonizer sync, String querry, int nr) {
        this.sync = sync;
        this.querry = querry;
        this.nr = nr;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.sync.getHandler().log("track loadet: " + track.getInfo().title);
        add(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        throw new IllegalStateException("playlist loadet!!?!?!?!");
    }

    @Override
    public void noMatches() {
        add(null);
        this.sync.getHandler().log("no matches found for \"" + this.querry + "\"");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        add(null);
        this.sync.getHandler().log("load failed for \"" + this.querry + "\"" + "\n" + exception.getMessage());
    }

    private synchronized void add(AudioTrack track) {
        this.sync.add(new MyPlaylistTrack(track, this.nr));
    }
}
