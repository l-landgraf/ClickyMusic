package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.playlist.PreloadablePlaylistQueueElement;
import halleg.discordmusikbot.guild.player.tracks.DefaultPlaylist;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class PreLoadHandler extends LoadHandler {
    private String initialSource;
    private PreloadablePlaylistQueueElement element;
    private int total;
    private String title;
    private String author;
    private String thumbnail;

    public PreLoadHandler(GuildHandler handler, String source, String initialSource, Member member, Message message, int total, String title, String author, String thumbnail) {
        super(handler, source, member, message);
        this.initialSource = initialSource;
        this.total = total;
        this.title = title;
        this.author = author;
        this.thumbnail = thumbnail;
    }

    public String getInitialSource() {
        return this.initialSource;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        this.element = new PreloadablePlaylistQueueElement(this.handler.getPlayer(), new Track(track, this.member), this.title, this.author, this.thumbnail, this.source, this.member, this.total);
        this.handler.getPlayer().addQueue(this.element);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.playlistLoaded(playlist);
        DefaultPlaylist list = DefaultPlaylist.tryBuildFormSpotifyPlaylist(playlist, this.member, this.source);
        this.element.loadPlaylist(list);
    }
}
