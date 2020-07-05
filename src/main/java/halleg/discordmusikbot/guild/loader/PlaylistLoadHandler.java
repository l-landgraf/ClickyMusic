package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.SingleQueueElement;
import halleg.discordmusikbot.guild.player.queue.playlist.DefaultPlaylistQueueElement;
import halleg.discordmusikbot.guild.player.tracks.DefaultPlaylist;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class PlaylistLoadHandler extends LoadHandler {
    protected String uri;

    public PlaylistLoadHandler(GuildHandler handler, String source, Member member, Message message) {
        super(handler, source, member, message);
        this.uri = source;
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        this.handler.getPlayer().addQueue(new SingleQueueElement(this.handler.getPlayer(), new Track(track, this.member)));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.playlistLoaded(playlist);
        DefaultPlaylist list = new DefaultPlaylist(playlist, this.member, this.source);
        DefaultPlaylistQueueElement element = new DefaultPlaylistQueueElement(this.handler.getPlayer(), list);
        this.handler.getPlayer().addQueue(element);
    }
}
