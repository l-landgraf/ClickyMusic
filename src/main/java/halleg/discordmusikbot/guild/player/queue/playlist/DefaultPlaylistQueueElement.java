package halleg.discordmusikbot.guild.player.queue.playlist;

import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.tracks.DefaultPlaylist;

public class DefaultPlaylistQueueElement extends PlaylistQueueElement<DefaultPlaylist> {
    public DefaultPlaylistQueueElement(Player player, DefaultPlaylist playlist) {
        super(player, playlist);
    }
}
