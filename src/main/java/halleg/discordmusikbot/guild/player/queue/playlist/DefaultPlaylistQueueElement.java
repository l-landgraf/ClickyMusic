package halleg.discordmusikbot.guild.player.queue.playlist;

import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.tracks.DefaultPlaylist;

public class DefaultPlaylistQueueElement extends PlaylistQueueElement<DefaultPlaylist> {
    public DefaultPlaylistQueueElement(QueuePlayer player, DefaultPlaylist playlist) {
        super(player, playlist);
    }
}
