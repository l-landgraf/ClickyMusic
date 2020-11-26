package halleg.discordmusikbot.guild.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import halleg.discordmusikbot.guild.player.queue.QueueStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.LinkedList;
import java.util.List;

public class Player implements Timer.TimerListener {
	private static final long DISCONNECT_TIME = 10000l;

	private List<QueueElement> queue;
	private QueueElement currentTrack;

	private SendHandler sender;
	private EventListener listener;

	private AudioPlayer player;
	private GuildHandler handler;
	private AudioManager audioManager;

	private Timer timer;

	public Player(GuildHandler handler) {

		this.handler = handler;
		this.audioManager = handler.getGuild().getAudioManager();
		this.player = handler.getManager().createPlayer();
		this.listener = new EventListener(handler);
		this.player.addListener(this.listener);
		this.sender = new SendHandler(this.player);
		this.queue = new LinkedList<>();
		this.timer = new Timer(DISCONNECT_TIME, this);
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
		try {
			if (this.currentTrack == null) {
				Message m = element.buildMessage(QueueStatus.PLAYING);
				Message message = this.handler.complete(m);
				element.setMessage(message);
				this.currentTrack = element;
				this.currentTrack.onPlaying();
			} else {
				Message m = element.buildMessage(QueueStatus.QUEUED);
				Message message = this.handler.complete(m);
				element.setMessage(message);
				Player.this.queue.add(element);
				element.onQueued();
			}
		} catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
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

	public void join(VoiceChannel c) {
		if (c == null) {
			return;
		}

		if (getConnectedChannel() != null &&
				c != getConnectedChannel()) {
			return;
		}
		connect(c);
	}

	public void connect(VoiceChannel c) {
		setPaused(false);
		this.audioManager.openAudioConnection(c);
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

	public void seekAdd(long l) {
		seekTo(this.player.getPlayingTrack().getPosition() + l);
	}

	public long getPosition() {
		return this.player.getPlayingTrack().getPosition();
	}

	public void seekTo(long l) {
		if (this.player.getPlayingTrack() == null) {
			this.handler.sendErrorMessage("No Track is currently playing.");
			return;
		}

		if (!this.player.getPlayingTrack().isSeekable()) {
			this.handler.sendErrorMessage("Seeking not supportet for this type of Track.");
			return;
		}

		if (this.player.getPlayingTrack().getDuration() < l) {
			this.handler.sendErrorMessage("Cant seek, track end reached.");
			return;
		}
		this.player.getPlayingTrack().setPosition(l);
	}

	public void voiceUpdate() {
		if (this.audioManager.isConnected()) {
			if (this.audioManager.getConnectedChannel().getMembers().size() > 1) {
				this.handler.log("Timer stopped.");
				this.timer.stop();
			}
			if (this.audioManager.getConnectedChannel().getMembers().size() == 1) {
				this.handler.log("Disconnecting in " + DISCONNECT_TIME / 1000 + "s...");
				this.timer.start();
			}
		}
	}

	public void setPaused(boolean b) {
		this.player.setPaused(b);
	}

	public boolean isPaused() {
		return this.player.isPaused();
	}

	public VoiceChannel getConnectedChannel() {
		return this.audioManager.getConnectedChannel();
	}

	public void togglePaused() {
		setPaused(!isPaused());
	}

	@Override
	public void onTimerEnd() {
		leave();
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
