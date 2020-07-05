package halleg.discordmusikbot.guild.player.queue.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.loader.PlaylistTrackLoadHandler;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
import halleg.discordmusikbot.guild.player.tracks.LoadableTrack;
import net.dv8tion.jda.api.entities.Member;

public class LoadablePlaylistQueueElement extends PlaylistQueueElement<LoadablePlaylist> {
    private static final int PRELOAD_MAX = 5;
    protected GuildHandler handler;

    public LoadablePlaylistQueueElement(GuildHandler handler, AudioTrack firstTrack, String title, String author, String thumbnail, String uri, Member member, String[] sources) {
        super(handler.getPlayer(), new LoadablePlaylist(title, author, thumbnail, uri, member, sources));
        this.handler = handler;
        this.playlist.getTrack(0).setTrack(firstTrack);
        loadPlannedTracks();
    }

    public void loadTrack(int trackNr, AudioTrack track, boolean update) {
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
        int[] planned = {};
        if (shuffle) {
            planned = getPlanedShuffle();
        } else {
            planned = getPlanedNormal();
        }

        for (int i = 0; i < planned.length && i < PRELOAD_MAX; i++) {

            this.handler.log("checling Song Nr. " + planned[i]);
            if (!this.playlist.getTrack(planned[i]).isLoaded()) {
                LoadableTrack track = this.playlist.getTrack(planned[i]);
                PlaylistTrackLoadHandler loader = new PlaylistTrackLoadHandler(this.handler, track.getSource(), track.getMember(), null, this, planned[i], (this.shuffle == shuffle && i < 3));
                this.handler.getLoader().load(this.playlist.getTrack(planned[i]).getSource(), loader);
            }
        }
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
    protected int getNextPlanedSong() {
        int i = super.getNextPlanedSong();
        if (i != -1 && this.playlist.getTrack(i).isLoaded()) {
            return i;
        } else {
            return -1;
        }
    }
}
