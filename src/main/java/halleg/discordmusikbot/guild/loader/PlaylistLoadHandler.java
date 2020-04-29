package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.PlaylistQueueElement;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
import net.dv8tion.jda.api.entities.Member;

public class PlaylistLoadHandler extends LoadHandler {
    protected LoadablePlaylist list;
    protected int retryAmount;
    protected int currentTrack;

    public PlaylistLoadHandler(GuildHandler handler, Member member, LoadablePlaylist list) {
        super(handler, list.getTrack(0).getSource(), member);
        this.list = list;
        this.currentTrack = 0;
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
    }

    @Override
    public void load() {
        this.handler.getLoader().search(this.handler.getLoader().youtubeSearch(this.source), this, this.member);
    }

    @Override
    public void noMatches() {
        this.handler.log("no matches found, retrying " + this.retryAmount + " \"" + this.source + "\"");
        if (this.retryAmount > 0) {
            this.retryAmount--;
            load();
        } else {
            this.handler.log("no matches found, giving up " + this.retryAmount + " \"" + this.source + "\"");
            onTrackNoMatches();
        }
    }

    @Override
    protected void onTrackLoaded(AudioTrack track) {
        this.list.getTrack(this.currentTrack).loadTrack(track);
        PlaylistQueueElement ele = new PlaylistQueueElement(this.handler.getPlayer(), this.list);
        this.handler.getPlayer().queueComplete(ele);
        PlaylistTrackLoadHandler norm = new PlaylistTrackLoadHandler(this.handler, this.member, ele, false);
        //norm.loadNext();
        PlaylistTrackLoadHandler rand = new PlaylistTrackLoadHandler(this.handler, this.member, ele, true);
        rand.loadNext();
    }

    protected void onTrackNoMatches() {
        this.list.getTrack(this.currentTrack).notFound();
        this.currentTrack++;
        if (this.currentTrack == this.list.getTotal()) {
            super.noMatches();
            return;
        }
        this.source = this.list.getTrack(this.currentTrack).getSource();
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
        load();
    }
}
