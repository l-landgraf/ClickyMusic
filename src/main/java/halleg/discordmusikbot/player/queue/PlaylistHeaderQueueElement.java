package halleg.discordmusikbot.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.Player;
import halleg.discordmusikbot.player.tracks.MyPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PlaylistHeaderQueueElement extends QueueElement {
    private MyPlaylist playlist;
    private PlaylistQueueElement[] tracks;
    private int queuedUpTo;
    private boolean isActive;

    public PlaylistHeaderQueueElement(Player player, MyPlaylist playlist) {
        super(player);
        this.tracks = new PlaylistQueueElement[playlist.getTotal()];
        this.playlist = playlist;
        this.queuedUpTo = -1;
        this.isActive = true;
    }

    @Override
    public MessageEmbed buildMessage() {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(this.playlist.getName(), this.playlist.getQuerry());
        eb.setDescription("Playlist queued by " + this.playlist.getMember().getAsMention());
        eb.setImage(this.playlist.getThumbnail());

        eb.addField("By", this.playlist.getAuthor(), true);
        eb.addField("Songs", this.playlist.getTotal() + "", true);
        return eb.build();
    }

    @Override
    public boolean isPlayable() {
        return false;
    }

    @Override
    public AudioTrack getTrack() {
        return null;
    }

    @Override
    public void reactDeletePlaylist(Member member) {
        this.player.removeElement(this, member);
        for (PlaylistQueueElement ele : this.tracks) {
            this.player.removeElement(ele, member);
        }
        setRemoved(member);
    }

    public synchronized void addTrack(PlaylistQueueElement track, int pos) {
        if (!this.isActive) {
            return;
        }

        this.tracks[pos] = track;

        for (int i = this.queuedUpTo + 1; i < this.tracks.length; i++) {
            if (this.tracks[i] == null) {
                break;
            } else {
                if (this.tracks[i].getTrack() != null) {
                    this.player.queueComplete(this.tracks[i]);
                }

                this.queuedUpTo = i;
            }
        }
    }

    protected void updateStatus() {
        for (PlaylistQueueElement t : this.tracks) {
            if (t != null && t.getInQueue()) {
                return;
            }
        }

        if (this.queuedUpTo < this.tracks.length - 1) {
            return;
        }
        setPlayed();
    }

    @Override
    protected void setWaitingEmojis() {
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_ALL_EMOJI).queue();
    }

    @Override
    protected void setPlayingEmojis() {
        setWaitingEmojis();
    }
}
