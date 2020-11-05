package halleg.discordmusikbot.guild.player.queue;

import halleg.discordmusikbot.guild.player.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.function.Consumer;

public abstract class QueueElement {
	protected Player player;
	protected Message message;
	protected QueueStatus status;

	public QueueElement(Player player) {
		this.player = player;
		this.status = null;
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


	public void onQueued() {
		this.status = QueueStatus.QUEUED;
	}

	public void onPlaying() {
		this.status = QueueStatus.PLAYING;
	}

	public void onPlayed() {
		this.status = QueueStatus.PLAYED;
	}

	public abstract void onResumePause();

	public void onEnded() {
		this.status = QueueStatus.PLAYED;
	}

	public void onSkip() {
		this.status = QueueStatus.SKIPPED;
		this.player.nextTrack();
	}

	public void onBack() {
		this.status = QueueStatus.PLAYING;
	}

	public void onDelete() {
		this.status = QueueStatus.REMOVED;
	}

	public void onDeletePlaylist() {
		this.status = QueueStatus.SKIPPED;
	}

	public abstract void onShuffle();

	public void addError(String message) {
		if (this.message.getEmbeds().get(0).getFooter() == null) {
			setFooter(message);
		} else {
			setFooter(this.message.getEmbeds().get(0).getFooter() + "\n" + message);
		}
		setColor(Color.RED);
	}


	protected void setFooter(String footer) {
		EmbedBuilder eb = new EmbedBuilder(this.message.getEmbeds().get(0));
		eb.setFooter(footer);
		this.message.editMessage(eb.build()).queue(new Consumer<>() {
			@Override
			public void accept(Message message) {
				QueueElement.this.message = message;
			}
		});
	}

	protected void setColor(Color color) {
		EmbedBuilder eb = new EmbedBuilder(this.message.getEmbeds().get(0));
		eb.setColor(color);
		this.message.editMessage(eb.build()).queue(new Consumer<>() {
			@Override
			public void accept(Message message) {
				QueueElement.this.message = message;
			}
		});
	}

	public QueueStatus getStatus() {
		return this.status;
	}

	public void runPlay(int i) {
		this.player.getHandler().sendErrorMessage("Command not supported for this Track.");
	}
}
