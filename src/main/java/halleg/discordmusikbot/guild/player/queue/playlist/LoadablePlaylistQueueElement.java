package halleg.discordmusikbot.guild.player.queue.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.loader.PlaylistTrackLoadHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
import halleg.discordmusikbot.guild.player.tracks.LoadableTrack;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class LoadablePlaylistQueueElement extends PlaylistQueueElement<LoadablePlaylist> {
    protected GuildHandler handler;
    protected QueuePlayer player;

    public LoadablePlaylistQueueElement(GuildHandler handler, QueuePlayer player, AudioTrack firstTrack, String title
            , String author, String thumbnail, String uri, Member member, String[] sources, String[] images) {
        super(player, new LoadablePlaylist(title, author, thumbnail, uri, member, sources, images));
        this.handler = handler;
        this.playlist.getTrack(0).setTrack(firstTrack);
        loadPlannedTracks();
    }

    public void loadTrack(int trackNr, AudioTrack track, boolean update, boolean playThis) {
        this.playlist.getTrack(trackNr).setTrack(track);
        if (update) {
            updateMessage();
        }
        if (playThis) {
            runPlay(trackNr);
        }
    }

    protected void loadPlannedTracks() {
        loadPlannedTracks(true);
        loadPlannedTracks(false);
    }

    protected void loadPlannedTracks(boolean shuffle) {
        List<Integer> planned = new ArrayList<>();
        if (shuffle) {
            planned = getPlanedShuffle();
        } else {
            planned = getPlanedNormal();
        }

        for (int i = this.currentPlanedIndex + 1; i < planned.size() && i < GuildHandler.PRELOAD_MAX + this.currentPlanedIndex + 1; i++) {
            loadTrack(planned.get(i),
                    (this.isShuffle == shuffle && i < GuildHandler.PRELOAD_MAX + this.currentPlanedIndex + 1), false);
        }

        for (int i = this.currentPlanedIndex - 1; i >= 0 && i > this.currentPlanedIndex - GuildHandler.PRELOAD_MAX - 1; i--) {
            loadTrack(planned.get(i), false, false);
        }
    }

    protected void loadTrack(int trackNr, boolean update, boolean playThis) {
        if (!this.playlist.getTrack(trackNr).isLoaded()) {
            this.handler.log("preloading Song Nr. " + trackNr);
            LoadableTrack track = this.playlist.getTrack(trackNr);
            PlaylistTrackLoadHandler loader = new PlaylistTrackLoadHandler(this.handler, this.player,
                    track.getSource(), track.getMember(),
                    null, this, trackNr, update, playThis);
            this.handler.getLoader().load(this.playlist.getTrack(trackNr).getSource(), loader);
        } else {
            this.handler.log("Song Nr. " + trackNr + " is already Loaded.");
        }
    }

    @Override
    protected void nextInternal() {
        super.nextInternal();
        loadPlannedTracks();
    }

    @Override
    protected void prevInternal() {
        super.prevInternal();
        loadPlannedTracks();
    }

    @Override
    protected int getPlanedTrack(int songNr) {
        int i = super.getPlanedTrack(songNr);
        if (i < 0) {
            return i;
        }
        if (this.playlist.getTrack(i).isLoaded()) {
            return i;
        } else {
            return -1;
        }
    }

    @Override
    public void runPlay(int i) {
        int planned = super.getPlanedTrack(i);
        if (planned < 0) {
            this.player.getHandler().sendErrorMessage("SongNr out of Bounds.");
            return;
        }
        if (!this.playlist.getTrack(i).isLoaded()) {
            loadTrack(i, false, true);
            return;
        }
        setCurrent(i);
        loadPlannedTracks();
        playCurrent();
    }
}
