package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.playlist.LoadablePlaylistQueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class PlaylistTrackLoadHandler extends LoadHandler {
    private LoadablePlaylistQueueElement element;
    private int trackNr;
    private boolean update;

    public PlaylistTrackLoadHandler(GuildHandler handler, String source, Member member, Message message, LoadablePlaylistQueueElement element, int trackNr, boolean update) {
        super(handler, source, member, message);
        this.trackNr = trackNr;
        this.element = element;
        this.update = update;
    }


    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        this.element.loadTrack(this.trackNr, track, this.update);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        throw new IllegalStateException("playlist loading not supproted");
    }
}
