package halleg.discordmusikbot.guild.player.queue.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;

public class LoadablePlaylistQueueElement extends PlaylistQueueElement<LoadablePlaylist> {
    public LoadablePlaylistQueueElement(Player player, LoadablePlaylist playlist) {
        super(player, playlist);
    }

    @Override
    protected void queueCurrent() {
        System.out.println("queue current: " + this.currentTrack);
        if (!this.playlist.getTrack(this.currentTrack).isReady()) {
            nextInternal();
            return;
        }
        super.queueCurrent();
    }

    public boolean isBeeingLoaded(int i) {
        return this.playlist.getTrack(i).isBeeingLoaded();
    }

    public String getSource(int i) {
        return this.playlist.getTrack(i).getSource();
    }

    public void startLoading(int i) {
        this.playlist.getTrack(i).startLoading();
    }

    public void loadTrack(int i, AudioTrack track) {
        this.playlist.getTrack(i).loadTrack(track);
        checkUpdate(i);
    }

    public void notFound(int i) {
        this.playlist.getTrack(i).notFound();
        checkUpdate(i);
    }

    public boolean isRawSource(int i) {
        return this.playlist.getTrack(i).isRawSource();
    }
}
