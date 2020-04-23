package halleg.discordmusikbot.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import halleg.discordmusikbot.guild.GuildHandler;
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
    public void onQueued() {
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_EMOJI).queue();
    }

    @Override
    public void onPlaying() {
        this.player.playTrack(this.track.getTrack());
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.addReaction(GuildHandler.SKIP_EMOJI).queue();
    }

    @Override
    public void onPlayed() {
        this.message.clearReactions(GuildHandler.SKIP_EMOJI).queue();
        this.message.clearReactions(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.clearReactions(GuildHandler.REMOVE_ALL_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
    }

    @Override
    public void onResumePause() {
        this.player.togglePaused();
    }

    @Override
    public void onEnded() {
        this.player.nextTrack();
    }


    @Override
    public void onSkip() {
        this.player.nextTrack();
    }

    @Override
    public void onDelete() {
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.player.removeElement(this);
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
