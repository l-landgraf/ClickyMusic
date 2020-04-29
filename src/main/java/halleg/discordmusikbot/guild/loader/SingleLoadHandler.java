package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.SingleQueueElement;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.entities.Member;

public class SingleLoadHandler extends LoadHandler {
    protected int retryAmount;

    public SingleLoadHandler(GuildHandler handler, String source, Member member) {
        super(handler, source, member);
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
    }

    @Override
    public void noMatches() {
        if (this.retryAmount > 0) {
            this.handler.log("no matches found, retrying " + this.retryAmount + " \"" + this.source + "\"");
            this.retryAmount--;
            this.handler.getLoader().search(this.handler.getLoader().youtubeSearch(this.source), this, this.member);
        } else {
            this.handler.log("no matches found, giving up " + this.retryAmount + " \"" + this.source + "\"");
            super.noMatches();
        }
    }

    @Override
    protected void onTrackLoaded(AudioTrack track) {
        this.handler.getPlayer().queueComplete(new SingleQueueElement(this.handler.getPlayer(), new Track(track, this.member)));
    }
}
