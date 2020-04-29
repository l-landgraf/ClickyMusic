package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;

public abstract class RetryLoadHandler extends LoadHandler {
    protected int retryAmount;

    public RetryLoadHandler(GuildHandler handler, String source, Member member, int retryAmount) {
        super(handler, source, member);
        this.retryAmount = retryAmount;
    }

    @Override
    public void noMatches() {
        if (this.retryAmount > 0) {
            this.handler.log("no matches found, retrying " + this.retryAmount + " \"" + this.source + "\"");
            this.retryAmount--;
            retryLoad();
        } else {
            this.handler.log("no matches found, giving up \"" + this.source + "\"");
            onTrackLoadFailed();
        }
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        onTrackLoaded(track);
    }

    protected void retryLoad() {
        load();
    }

    protected void onTrackLoaded(AudioTrack track) {
        super.trackLoaded(track);
    }

    protected void onTrackLoadFailed() {
        super.noMatches();
    }
}
