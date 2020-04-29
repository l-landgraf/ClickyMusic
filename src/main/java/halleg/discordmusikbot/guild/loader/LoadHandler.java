package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;

public abstract class LoadHandler implements AudioLoadResultHandler {
    protected GuildHandler handler;
    protected Member member;
    protected String source;

    public LoadHandler(GuildHandler handler, String source, Member member) {
        this.member = member;
        this.source = source;
        this.handler = handler;
    }

    public void load() {
        this.handler.getLoader().search(this.source, this, this.member);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.handler.log("track loadet \"" + track.getInfo().title + "\"");
    }

    @Override
    public void noMatches() {
        this.handler.log("no matches found \"" + this.source + "\"");
        this.handler.sendErrorMessage("No Matches Found!");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        this.handler.sendErrorMessage(exception.getMessage());
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        throw new IllegalArgumentException("Plailist loaded called!?");
    }
}
