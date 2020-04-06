package halleg.discordmusikbot.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import halleg.discordmusikbot.player.Player;
import halleg.discordmusikbot.player.tracks.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SingleQueueElement extends QueueElement {
    private Track track;

    public SingleQueueElement(Player player, Track track) {
        super(player);
        this.track = track;
    }

    @Override
    public MessageEmbed buildMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        AudioTrackInfo info = this.track.getTrack().getInfo();

        eb.setTitle(info.title, info.uri);
        eb.setDescription("Queued by " + this.track.getMember().getAsMention());

        if (this.track.hasThumbnail()) {
            eb.setThumbnail(this.track.getThumbnail());
        }

        eb.addField("By", info.author, true);
        eb.addField("Length", toTime(info.length), true);
        return eb.build();
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public AudioTrack getTrack() {
        return this.track.getTrack();
    }
}
