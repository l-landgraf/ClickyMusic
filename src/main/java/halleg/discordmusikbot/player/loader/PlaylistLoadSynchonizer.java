package halleg.discordmusikbot.player.loader;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.queue.PlaylistHeaderQueueElement;
import halleg.discordmusikbot.player.queue.PlaylistQueueElement;
import halleg.discordmusikbot.player.tracks.MyPlaylist;
import halleg.discordmusikbot.player.tracks.MyPlaylistTrack;
import net.dv8tion.jda.api.entities.Member;

public class PlaylistLoadSynchonizer {
    private GuildHandler handler;
    private Member member;
    private MyPlaylist playlist;
    private String querry;
    private PlaylistHeaderQueueElement element;

    public PlaylistLoadSynchonizer(GuildHandler handler, Member member, MyPlaylist playlist, String querry) {
        this.member = member;
        this.handler = handler;
        this.playlist = playlist;
        this.querry = querry;
    }

    public synchronized void add(MyPlaylistTrack track) {

        if (this.element == null) {
            this.element = new PlaylistHeaderQueueElement(this.handler.getPlayer(), this.playlist);
            this.handler.getPlayer().queueComplete(this.element);
        }
        PlaylistQueueElement newEle = new PlaylistQueueElement(this.handler.getPlayer(), track, this.element);
        this.element.addTrack(newEle, track.getNr());
    }

    public GuildHandler getHandler() {
        return this.handler;
    }
}
