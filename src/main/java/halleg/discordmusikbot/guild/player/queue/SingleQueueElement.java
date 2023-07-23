package halleg.discordmusikbot.guild.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.File;
import java.util.Random;

public class SingleQueueElement extends QueueElement {
    private Track track;

    public SingleQueueElement(QueuePlayer player, Track track) {
        super(player, false);
        this.track = track;
    }

    @Override
    public MessageEmbed buildMessageEmbed(QueueStatus status) {
        super.buildMessageEmbed(status);
        EmbedBuilder eb = new EmbedBuilder();
        try {
            eb.setTitle(this.track.getTitle(), this.track.getURI());
        } catch (IllegalArgumentException e) {
            eb.setTitle(this.track.getTitle());
            eb.setFooter(this.player.getHandler().getFileManager().getRelativePath(new File(this.track.getTrack().getInfo().identifier)));
        }
        eb.setDescription("Queued by " + this.track.getMember().getAsMention());

        try {

            eb.setThumbnail(this.track.getThumbnail());
        } catch (IllegalArgumentException e) {
        }

        eb.addField("By", this.track.getAuthorEmbedLink(), true);
        eb.addField("Length", this.track.getLength(), true);
        if (status == QueueStatus.PLAYING) {
            addProgressBar(eb, this.track.getTrack());
        }
        return eb.build();
    }

    @Override
    public AudioTrack getCurrentTrack() {
        return this.track.getTrack();
    }

    @Override
    public void onShuffle() {
        throw new IllegalStateException();
    }

    @Override
    public void onNext() {
        throw new IllegalStateException();
    }

    @Override
    public void onPrevious() {
        throw new IllegalStateException();
    }
}
