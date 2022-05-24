package halleg.discordmusikbot.guild.player.queue;

import halleg.discordmusikbot.guild.buttons.ButtonGoup;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.File;

public class SingleQueueElement extends QueueElement {
    private Track track;

    public SingleQueueElement(QueuePlayer player, Track track) {
        super(player);
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
        return eb.build();
    }

    @Override
    public void onQueued() {
        super.onQueued();
        this.player.getHandler().setButtons(this.message, ButtonGoup.QUEUED);
    }

    @Override
    public void onPlaying() {
        super.onPlaying();
        this.player.playTrack(this.track.getTrack());
        this.player.getHandler().setButtons(this.message, ButtonGoup.PLAYING_SINGLE);
    }

    @Override
    public void onPlayed() {
        super.onPlayed();
        this.player.getHandler().setButtons(this.message, ButtonGoup.PLAYED);
    }

    @Override
    public void onResumePause() {
        this.player.togglePaused();
    }

    @Override
    public void onEnded() {
        super.onEnded();
        this.player.nextTrack();
    }

    @Override
    public void onDelete() {
        super.onDelete();
        this.player.getHandler().setButtons(this.message, ButtonGoup.PLAYED);
        this.player.removeElement(this);
    }

    @Override
    public void onBack() {
        super.onBack();
        this.player.playTrack(this.track.getTrack());
    }

    @Override
    public void onShuffle() {
        throw new IllegalStateException();
    }

    @Override
    public void onDeletePlaylist() {
        throw new IllegalStateException();
    }
}
