package halleg.discordmusikbot.player;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class SendHandler implements AudioSendHandler {
	private final AudioPlayer player;
	private AudioFrame lastFrame;

	public SendHandler(AudioPlayer player) {
		this.player = player;
	}

	public boolean canProvide() {
		this.lastFrame = this.player.provide();
		return this.lastFrame != null;
	}

	public ByteBuffer provide20MsAudio() {
		return ByteBuffer.wrap(this.lastFrame.getData());
	}

	public boolean isOpus() {
		return true;
	}
}
