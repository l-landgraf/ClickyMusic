package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.SingleQueueElement;
import halleg.discordmusikbot.guild.player.queue.playlist.DefaultPlaylistQueueElement;
import halleg.discordmusikbot.guild.player.tracks.DefaultPlaylist;
import halleg.discordmusikbot.guild.player.tracks.Track;
import halleg.discordmusikbot.guild.youtube.MyYoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class SingleLoadHandler extends LoadHandler {

    public SingleLoadHandler(GuildHandler handler, QueuePlayer player, String source, Member member, Message message) {
        super(handler, player, source, member, message);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        super.trackLoaded(track);
        this.player.addQueue(
                new SingleQueueElement(this.player,
                        new Track(track, this.member, null, MyYoutubeAudioSourceManager.loadThumbnail(track))));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.playlistLoaded(playlist);
        DefaultPlaylist list = DefaultPlaylist.tryBuildFormSpotifyPlaylist(playlist, this.member, this.source);
        DefaultPlaylistQueueElement element = new DefaultPlaylistQueueElement(this.player, list);
        this.player.addQueue(element);
    }
}
