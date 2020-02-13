package halleg.discordmusikbot.player;

import net.dv8tion.jda.api.entities.Message;

public class QueueElement {
	private Track track;
	private Message message;

	public QueueElement(Track track, Message message) {
		this.track = track;
		this.message = message;
	}

	public Track getTrack() {
		return this.track;
	}

	public Message getMessage() {
		return this.message;
	}
}
