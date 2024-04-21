package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public abstract class LoadHandler implements AudioLoadResultHandler {
    protected GuildHandler handler;
    protected QueuePlayer player;
    protected Member member;
    protected String source;
    protected Message message;

    public LoadHandler(GuildHandler handler, QueuePlayer player, String source, Member member, Message message) {
        this.member = member;
        this.player = player;
        this.source = source;
        this.handler = handler;
        this.message = message;
    }

    public void load() {
        this.handler.getLoader().load(this.source, this);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (this.message != null) {
            this.handler.queue(this.message.delete());
        }
        this.handler.log("track loadet \"" + track.getInfo().title + "\"");
    }

    @Override
    public void noMatches() {
        this.handler.log("no matches found \"" + this.source + "\"");
        if (this.message != null) {
            this.handler.queueLater(this.message.delete());
            this.handler.getBuilder().setLoadingFailed(this.message);
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        this.handler.sendErrorMessage(exception.getMessage());
        if (this.message != null) {
            this.handler.queue(this.message.delete());
            this.handler.getBuilder().setLoadingFailed(this.message);
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        this.handler.queue(this.message.delete());
        this.handler.log("playlist loadet \"" + playlist.getName() + "\"");
    }
}
