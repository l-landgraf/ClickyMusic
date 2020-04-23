package halleg.discordmusikbot.player.loader;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.queue.PlaylistQueueElement;
import halleg.discordmusikbot.player.tracks.MyPlaylist;
import halleg.discordmusikbot.player.tracks.MyPlaylistTrack;
import net.dv8tion.jda.api.entities.Member;

public class PlaylistLoadSynchonizer {
    private GuildHandler handler;
    private Member member;
    private MyPlaylist playlist;
    private String querry;
    private PlaylistQueueElement element;

    public PlaylistLoadSynchonizer(GuildHandler handler, Member member, MyPlaylist playlist, String querry) {
        this.member = member;
        this.handler = handler;
        this.playlist = playlist;
        this.querry = querry;
    }

    public synchronized void add(MyPlaylistTrack track) {

        if (this.element == null) {
            this.element = new PlaylistQueueElement(this.handler.getPlayer(), this.playlist);
            this.element.addTrack(track);
            this.handler.getPlayer().queueComplete(this.element);
        } else {
            this.element.addTrack(track);
        }
    }

    public GuildHandler getHandler() {
        return this.handler;
    }
}
