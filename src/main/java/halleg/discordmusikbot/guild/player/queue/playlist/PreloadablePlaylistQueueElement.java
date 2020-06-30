package halleg.discordmusikbot.guild.player.queue.playlist;

import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.queue.QueueStatus;
import halleg.discordmusikbot.guild.player.tracks.Track;
import halleg.discordmusikbot.guild.player.tracks.TrackPlaylist;
import halleg.discordmusikbot.guild.player.tracks.UnloadedPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PreloadablePlaylistQueueElement extends PlaylistQueueElement {
    private Track firstTrack;
    private boolean isLoaded;

    public PreloadablePlaylistQueueElement(Player player, Track firstTrack, String title, String author, String thumbnail, String uri, Member member, int total) {
        super(player, new UnloadedPlaylist(title, author, thumbnail, uri, member, total));
        this.firstTrack = firstTrack;
        this.isLoaded = false;
    }

    public void loadPlaylist(TrackPlaylist list) {
        this.playlist = list;
        this.isLoaded = true;
        updateMessage();
    }


    @Override
    public Track getCurrentTrack() {
        if (this.isLoaded) {
            return super.getCurrentTrack();
        } else {
            return this.firstTrack;
        }
    }

    @Override
    public void onSkip() {
        if (this.isLoaded) {
            super.onSkip();
        } else {
            onDeletePlaylist();
        }
    }

    @Override
    public MessageEmbed buildMessageEmbed(QueueStatus status) {
        if (this.isLoaded) {
            return super.buildMessageEmbed(status);
        } else {
            this.status = status;
            EmbedBuilder eb = new EmbedBuilder();

            addPlaylistRow(eb);
            if (status.getKeepLoading()) {
                addNowPlayingRow(eb);
                eb.addField("", "**Loading " + (this.playlist.getTotal() - 1) + " More...**", false);
            }

            return eb.build();
        }
    }
}
