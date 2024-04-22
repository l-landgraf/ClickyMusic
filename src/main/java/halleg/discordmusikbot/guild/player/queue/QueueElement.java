package halleg.discordmusikbot.guild.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.tracks.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.function.Consumer;

public abstract class QueueElement {
    public static final String PROGRESSBAR_FULL_CHARACTER = "█";
    public static final String PROGRESSBAR_SEPERATOR_CHARACTER = "█";
    public static final String PROGRESSBAR_EMPTY_CHARACTER = "░";


    protected final boolean isPlaylist;
    protected boolean isShuffle;

    protected QueuePlayer player;
    protected Message message;
    protected QueueStatus status;

    public QueueElement(QueuePlayer player, boolean isPlaylist) {

        this.player = player;
        this.status = null;
        this.isPlaylist = isPlaylist;
    }

    public MessageEmbed buildMessageEmbed(QueueStatus status) {
        this.status = status;
        return null;
    }


    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message m) {
        this.message = m;
    }

    public void updateMessage() {
        if (this.message == null) {
            return;
        }

        MessageEditBuilder meb = new MessageEditBuilder();
        meb.setEmbeds(buildMessageEmbed(this.status));
        meb.setComponents(this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
        this.player.getHandler().queue(this.message.editMessage(meb.build()), new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                QueueElement.this.message = message;
            }
        });
    }

    public abstract AudioTrack getCurrentTrack();

    public void onQueued() {
        this.status = QueueStatus.QUEUED;
        setButtons();
    }

    public void onPlaying() {
        this.status = QueueStatus.PLAYING;
        this.player.playTrack(this.getCurrentTrack());
        setButtons();
        updateMessage();
    }

    public void onPlayed() {
        this.status = QueueStatus.PLAYED;
        setButtons();
        updateMessage();
    }

    public void onResumePause() {
        this.player.togglePaused();
        setButtons();
    }

    public void onEnded() {
        this.status = QueueStatus.PLAYED;
        this.player.nextTrack();
        setButtons();
        updateMessage();
    }

    public void onSkip() {
        this.status = QueueStatus.SKIPPED;
        setButtons();
        this.player.nextTrack();
        updateMessage();
    }

    public void onBack() {
        this.status = QueueStatus.PLAYING;
        this.player.playTrack(this.getCurrentTrack());
        updateMessage();
    }

    public void onRemoved() {
        this.status = QueueStatus.REMOVED;
        this.player.removeElement(this);

        setButtons();
        updateMessage();
    }

    private void setButtons() {
        this.player.getHandler().queue(
                this.message.editMessage(MessageEditData.fromMessage(this.message)).setComponents(this.status.getButtons(this.player.isPaused(),
                        this.isPlaylist,
                        this.isShuffle)));
    }

    public void onShuffle() {
        this.isShuffle = !this.isShuffle;
        updateMessage();
    }

    protected void addProgressBar(EmbedBuilder eb, AudioTrack track) {
        int barWidth = 30;
        int p = (int) (((float) this.player.getPosition() / (float) track.getDuration()) * (float) barWidth);

        String bar = "";
        for (int i = 0; i < barWidth; i++) {
            if (i < p) {
                bar += PROGRESSBAR_FULL_CHARACTER;
            } else if (i == p) {
                bar += PROGRESSBAR_SEPERATOR_CHARACTER;
            } else {
                bar += PROGRESSBAR_EMPTY_CHARACTER;
            }
        }
        eb.addField(bar, Track.toTime(this.player.getPosition()), false);
    }


    public abstract void onNext();

    public abstract void onPrevious();

    public void runPlay(int i) throws Exception {
        throw new Exception("Command not supported for this Track.");
    }

    public boolean isPlaylist() {
        return this.isPlaylist;
    }

    public boolean isShuffle() {
        return this.isShuffle;
    }
}
