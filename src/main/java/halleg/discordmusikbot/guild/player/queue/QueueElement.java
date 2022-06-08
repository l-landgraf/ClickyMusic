package halleg.discordmusikbot.guild.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class QueueElement {
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

    public Message buildMessage(QueueStatus status) {
        return new MessageBuilder(buildMessageEmbed(status)).build();
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

        this.player.getHandler().editMessage(this.message, new MessageBuilder(buildMessageEmbed(this.status)).build(),
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle),
                (message) -> {
                    QueueElement.this.message = message;
                });
    }

    public abstract AudioTrack getCurrentTrack();

    public void onQueued() {
        this.status = QueueStatus.QUEUED;
        this.player.getHandler().setButtons(this.message, this.status.getButtons(
                this.player.isPaused(), this.isPlaylist, this.isShuffle));
    }

    public void onPlaying() {
        this.status = QueueStatus.PLAYING;
        this.player.playTrack(this.getCurrentTrack());
        this.player.getHandler().setButtons(this.message,
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
        updateMessage();
    }

    public void onPlayed() {
        this.status = QueueStatus.PLAYED;
        this.player.getHandler().setButtons(this.message,
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
        updateMessage();
    }

    public void onResumePause() {
        this.player.togglePaused();
        this.player.getHandler().setButtons(this.message,
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
    }

    public void onEnded() {
        this.status = QueueStatus.PLAYED;
        this.player.nextTrack();
        this.player.getHandler().setButtons(this.message,
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
        updateMessage();
    }

    public void onSkip() {
        this.status = QueueStatus.SKIPPED;
        this.player.getHandler().setButtons(this.message,
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
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
        this.player.getHandler().setButtons(this.message,
                this.status.getButtons(this.player.isPaused(), this.isPlaylist, this.isShuffle));
        updateMessage();
    }

    public void onShuffle() {
        this.isShuffle = !this.isShuffle;
        updateMessage();
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
