package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.SingleQueueElement;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.entities.Member;

public class SingleLoadHandler extends RetryLoadHandler {

    public SingleLoadHandler(GuildHandler handler, String source, Member member) {
        super(handler, source, member, GuildHandler.RETRY_AMOUNT);
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
    }

    @Override
    protected void retryLoad() {
        this.handler.getLoader().search(this.handler.getLoader().youtubeSearch(this.source), this, this.member);
    }

    @Override
    protected void onTrackLoaded(AudioTrack track) {
        super.onTrackLoaded(track);
        this.handler.getPlayer().queueComplete(new SingleQueueElement(this.handler.getPlayer(), new Track(track, this.member)));
    }

    @Override
    protected void onTrackLoadFailed() {
        super.onTrackLoadFailed();
    }
}
