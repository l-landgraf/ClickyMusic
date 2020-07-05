package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.playlist.LoadablePlaylistQueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class InititalPlaylistLoadHandler extends LoadHandler {
    private String initialSource;
    private LoadablePlaylistQueueElement element;
    private String[] sources;
    private String title;
    private String author;
    private String thumbnail;

    public InititalPlaylistLoadHandler(GuildHandler handler, String source, String initialSource, Member member, Message message, String[] sources, String title, String author, String thumbnail) {
        super(handler, source, member, message);
        this.initialSource = initialSource;
        this.sources = sources;
        this.title = title;
        this.author = author;
        this.thumbnail = thumbnail;
    }

    public String getInitialSource() {
        return this.initialSource;
    }

    @Override
    public void load() {
        this.handler.getLoader().load(this.initialSource, this);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        this.element = new LoadablePlaylistQueueElement(this.handler, track, this.title, this.author, this.thumbnail, this.source, this.member, this.sources);
        this.handler.getPlayer().addQueue(this.element);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        throw new IllegalStateException("playlist loading not supproted");
    }
}
