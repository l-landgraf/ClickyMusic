package halleg.discordmusikbot.guild.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import halleg.discordmusikbot.guild.GuildHandler;

public class EventListener extends AudioEventAdapter {
    private GuildHandler handler;
    private QueuePlayer player;

    public EventListener(GuildHandler handler, QueuePlayer player) {
        this.handler = handler;
        this.player = player;
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        this.handler.getPlayer().progressUpdate();
        this.handler.log("paused");
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        this.handler.getPlayer().progressUpdate();
        this.handler.log("resumed");
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.handler.log("playing track: " + track.getInfo().title);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.handler.log("track ended: " + endReason);
        if (endReason.mayStartNext) {
            this.player.trackEnded();
        }
        // endReason == FINISHED: A track finished or died by an exception (mayStartNext
        // = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not
        // finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you
        // can put a
        // clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        this.handler.log("track exception: " + " " + exception.getMessage());
//        this.handler.getPlayer().findElement(track).addError(exception.getMessage());
//      this.handler.log(exception.getCause().getMessage() + "\n" + exception.getCause().getStackTrace());
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        this.handler.sendErrorMessage("Track stuck");
    }

}
