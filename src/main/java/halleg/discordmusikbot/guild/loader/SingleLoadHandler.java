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

public class SingleLoadHandler extends RetryLoadHandler {

    public SingleLoadHandler(GuildHandler handler, String source, Member member, Message message) {
        super(handler, source, member, GuildHandler.RETRY_AMOUNT, message);
    }

    @Override
    protected void retryLoad() {
        onTrackLoadFailed();
        //this.handler.getLoader().load(this.handler.getLoader().youtubeSearch(this.source), this);
    }

    @Override
    protected void onTrackLoaded(AudioTrack track) {
        super.onTrackLoaded(track);
        this.handler.getPlayer().addQueue(new SingleQueueElement(this.handler.getPlayer(), new Track(track, this.member)));
    }

    @Override
    protected void onTrackLoadFailed() {
        super.onTrackLoadFailed();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.playlistLoaded(playlist);
        DefaultPlaylist list = DefaultPlaylist.tryBuildFormSpotifyPlaylist(playlist, this.member, this.source);
        DefaultPlaylistQueueElement element = new DefaultPlaylistQueueElement(this.handler.getPlayer(), list);
        this.handler.getPlayer().addQueue(element);
    }
}
