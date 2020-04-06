package halleg.discordmusikbot.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import halleg.discordmusikbot.player.Player;
import halleg.discordmusikbot.player.tracks.MyPlaylistTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PlaylistQueueElement extends QueueElement {
    private MyPlaylistTrack track;
    private PlaylistHeaderQueueElement header;
    private boolean inQueue;

    public PlaylistQueueElement(Player player, MyPlaylistTrack track, PlaylistHeaderQueueElement header) {
        super(player);
        this.track = track;
        this.header = header;
        if (track == null) {
            this.inQueue = false;
        } else {
            this.inQueue = true;
        }
    }

    @Override
    public MessageEmbed buildMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        AudioTrackInfo info = this.track.getAudioTrack().getInfo();

        eb.setTitle(info.title, info.uri);

        eb.addField("By", info.author, true);
        eb.addField("Length", toTime(info.length), true);
        eb.addField("Position", this.track.getNr() + 1 + "", true);
        return eb.build();
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public AudioTrack getTrack() {
        return this.track.getAudioTrack();
    }


    private void setInQueue(boolean b) {
        this.inQueue = b;
        this.header.updateStatus();
    }

    public boolean getInQueue() {
        return this.inQueue;
    }

    @Override
    public void setPlaying() {
        super.setPlaying();
        setInQueue(false);
    }

    @Override
    public void setPlayed() {
        super.setPlayed();
        setInQueue(false);
    }

    @Override
    public void setSkiped(Member member) {
        super.setSkiped(member);
        setInQueue(false);
    }

    @Override
    public void setRemoved(Member member) {
        super.setRemoved(member);
        setInQueue(false);
    }
}
