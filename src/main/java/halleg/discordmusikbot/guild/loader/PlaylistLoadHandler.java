package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.PlaylistQueueElement;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
import net.dv8tion.jda.api.entities.Member;

public class PlaylistLoadHandler extends RetryLoadHandler {
    protected LoadablePlaylist list;
    protected int currentTrack;

    public PlaylistLoadHandler(GuildHandler handler, Member member, LoadablePlaylist list) {
        super(handler, list.getTrack(0).getSource(), member, GuildHandler.RETRY_AMOUNT);
        this.list = list;
        this.currentTrack = 0;
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
    }

    @Override
    public void load() {
        this.handler.getLoader().search(this.handler.getLoader().youtubeSearch(this.source), this, this.member);
    }

    @Override
    protected void onTrackLoaded(AudioTrack track) {
        this.list.getTrack(this.currentTrack).loadTrack(track);
        PlaylistQueueElement ele = new PlaylistQueueElement(this.handler.getPlayer(), this.list);
        this.handler.getPlayer().queueComplete(ele);

        PlaylistTrackLoadHandler rand = new PlaylistTrackLoadHandler(this.handler, this.member, ele, true);
        rand.loadNext();
    }

    @Override
    protected void onTrackLoadFailed() {
        this.list.getTrack(this.currentTrack).notFound();
        this.currentTrack++;
        if (this.currentTrack >= this.list.getTotal()) {
            super.onTrackLoadFailed();
            return;
        }
        this.source = this.list.getTrack(this.currentTrack).getSource();
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
        load();
    }
}
