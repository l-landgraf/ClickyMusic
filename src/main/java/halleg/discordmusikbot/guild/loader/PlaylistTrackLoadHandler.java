package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.playlist.LoadablePlaylistQueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class PlaylistTrackLoadHandler extends LoadHandler {
    private LoadablePlaylistQueueElement element;
    private int trackNr;
    private boolean update;
    private boolean playThis;

    public PlaylistTrackLoadHandler(GuildHandler handler, QueuePlayer player, String source, Member member, Message message,
                                    LoadablePlaylistQueueElement element, int trackNr, boolean update, boolean playThis) {
        super(handler, player, source, member, message);
        this.trackNr = trackNr;
        this.element = element;
        this.update = update;
        this.playThis = playThis;
    }


    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        this.element.loadTrack(this.trackNr, track, this.update, this.playThis);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        throw new IllegalStateException("playlist loading not supproted");
    }
}
