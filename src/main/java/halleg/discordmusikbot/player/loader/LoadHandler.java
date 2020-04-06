package halleg.discordmusikbot.player.loader;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.queue.SingleQueueElement;
import halleg.discordmusikbot.player.tracks.Track;
import net.dv8tion.jda.api.entities.Member;

public class LoadHandler implements AudioLoadResultHandler {
    private GuildHandler handler;
    private Member member;
    private String query;
    private boolean retry;

    public LoadHandler(GuildHandler handler, Member member, String query, boolean retry) {
        this.member = member;
        this.handler = handler;
        this.query = query;
        this.retry = retry;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.handler.log("track loadet: " + track.getInfo().title);
        this.handler.getPlayer().queueComplete(new SingleQueueElement(this.handler.getPlayer(), new Track(track, this.member)));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        this.handler.log("loadet playlist:" + playlist.getName());
        for (AudioTrack track : playlist.getTracks()) {
            this.handler.getPlayer().queueComplete(new SingleQueueElement(this.handler.getPlayer(), new Track(track, this.member)));
        }
    }

    @Override
    public void noMatches() {
        this.handler.log("no matches found");
        if (this.retry) {
            this.handler.getPlayer().loadAndQueueSearch(this.query, this.member);
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        this.handler.sendErrorMessage(exception.getMessage());
    }

}
