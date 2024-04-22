package halleg.discordmusikbot.guild.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import halleg.discordmusikbot.guild.player.queue.QueueStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.LinkedList;
import java.util.List;

public class QueuePlayer {
    private static final long DISCONNECT_TIME = 10000L;
    private static final long PROGRESS_UPDATE_TIME = 10000L;

    private List<QueueElement> queue;
    private QueueElement currentTrack;

    private SendHandler sender;
    private EventListener listener;

    private AudioPlayer player;
    private GuildHandler handler;
    private AudioManager audioManager;

    private Timer progressUpdateTimer;
    private Timer leaveTimer;

    public QueuePlayer(GuildHandler handler, AudioPlayer player) {

        this.handler = handler;
        this.audioManager = handler.getGuild().getAudioManager();
        this.player = player;
        this.listener = new EventListener(handler, this);
        this.player.addListener(this.listener);
        this.sender = new SendHandler(this.player);
        this.queue = new LinkedList<>();
        this.leaveTimer = new Timer(DISCONNECT_TIME, false, () -> leave());
        this.progressUpdateTimer = new Timer(PROGRESS_UPDATE_TIME, true, () -> progressUpdate());
        this.progressUpdateTimer.start();
        this.audioManager.setSendingHandler(this.sender);
    }

    public void clearQueue() {
        this.player.stopTrack();
        for (QueueElement queueElement : this.queue) {
            queueElement.onPlayed();
        }
        this.queue.clear();

        nextTrack();
    }

    public void disconnect() {
        this.audioManager.closeAudioConnection();
    }

    public void leave() {
        disconnect();
        clearQueue();
    }

    public void addQueue(QueueElement element) {
        MessageCreateBuilder mcb = new MessageCreateBuilder();
        if (this.currentTrack == null) {
            mcb.setEmbeds(element.buildMessageEmbed(QueueStatus.PLAYING));
            Message message =
                    this.handler.complete(this.handler.getConfig().getOutputChannel().sendMessage(mcb.build()));
            element.setMessage(message);
            this.currentTrack = element;
            this.currentTrack.onPlaying();
        } else {
            mcb.setEmbeds(element.buildMessageEmbed(QueueStatus.QUEUED));
            Message message =
                    this.handler.complete(this.handler.getConfig().getOutputChannel().sendMessage(mcb.build()));
            element.setMessage(message);
            this.queue.add(element);
            element.onQueued();
        }
    }

    public void trackEnded() {
        this.currentTrack.onEnded();
    }

    public void nextTrack() {
        if (this.currentTrack != null) {
            this.currentTrack.onPlayed();
            this.currentTrack = null;
        }
        QueueElement next = null;
        if (!this.queue.isEmpty()) {
            next = this.queue.get(0);
        }
        if (next == null) {
            this.player.stopTrack();
        } else {
            this.queue.remove(next);
            this.currentTrack = next;
            this.currentTrack.onPlaying();
        }
    }

    public boolean jump(int nr) {
        if (nr < 1) {
            return false;
        } else if (nr > this.queue.size()) {
            return false;
        }
        if (this.currentTrack != null) {
            this.currentTrack.onPlayed();
            this.currentTrack = null;
        }
        for (int i = 1; i < nr; i++) {
            this.queue.get(0).onPlayed();
            this.queue.remove(0);
        }
        nextTrack();
        return true;
    }

    public void playTrack(AudioTrack track) {
        this.player.playTrack(track.makeClone());
    }

    public boolean join(AudioChannel c) {
        if (c == null) {
            return false;
        }
        
        this.handler.log("Joining voice cannel " + c.getName());
        try {
            this.audioManager.openAudioConnection(c);
            return true;
        } catch (InsufficientPermissionException e) {
            this.handler.handleMissingPermission(e);
            return false;
        }
    }

    public void removeElement(QueueElement element) {
        this.queue.remove(element);
    }

    public QueueElement findElement(long id) {
        if (this.currentTrack != null && this.currentTrack.getMessage().getIdLong() == id) {
            return this.currentTrack;
        }

        for (QueueElement queueElement : this.queue) {
            if (queueElement.getMessage().getIdLong() == id) {
                return queueElement;
            }
        }
        return null;
    }

    public QueueElement getCurrentElement() {
        return this.currentTrack;
    }

    public void seekAdd(long l) throws Exception {
        seekTo(this.player.getPlayingTrack().getPosition() + l);
    }

    public long getPosition() {
        if (this.player.getPlayingTrack() == null) {
            return 0;
        }
        return this.player.getPlayingTrack().getPosition();
    }

    public void seekTo(long l) throws Exception {
        if (this.player.getPlayingTrack() == null) {
            throw new Exception("No Track is currently playing.");
        }

        if (!this.player.getPlayingTrack().isSeekable()) {
            throw new Exception("Seeking not supported for this type of Track.");
        }

        if (this.player.getPlayingTrack().getDuration() < l) {
            throw new Exception("Cant seek, track end reached.");
        }
        this.player.getPlayingTrack().setPosition(l);
        progressUpdate();
    }

    public void progressUpdate() {
        if (this.currentTrack == null) {
            return;
        }
        this.handler.log("updating progress");
        this.currentTrack.updateMessage();
    }

    public void voiceUpdate() {
        if (this.audioManager.isConnected()) {
            int nonBotUsers = 0;
            for (Member m : this.audioManager.getConnectedChannel().getMembers()) {
                if (!m.getUser().isBot()) {
                    nonBotUsers++;
                }
            }

            if (nonBotUsers >= 1) {
                this.handler.log("Timer stopped.");
                this.leaveTimer.stop();
                return;
            }
            this.handler.log("Disconnecting in " + DISCONNECT_TIME / 1000 + "s...");
            this.leaveTimer.start();

        }
    }

    public void setPaused(boolean b) {
        this.player.setPaused(b);
    }

    public boolean isPaused() {
        return this.player.isPaused();
    }

    public AudioChannel getConnectedChannel() {
        return this.audioManager.getConnectedChannel();
    }

    public void togglePaused() {
        setPaused(!isPaused());
    }

    public boolean isPlaying() {
        return this.player.getPlayingTrack() != null;
    }

    public GuildHandler getHandler() {
        return this.handler;
    }

    public int queueSize() {
        return this.queue.size();
    }
}
