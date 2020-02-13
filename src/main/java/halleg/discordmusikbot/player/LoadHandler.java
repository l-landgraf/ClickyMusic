package halleg.discordmusikbot.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;

public class LoadHandler implements AudioLoadResultHandler {
	private GuildHandler handler;
	private Member member;
	private String query;
	private boolean retry;

	public LoadHandler(GuildHandler handler, Member member, String query, boolean retry) {
		this.member = member;
		this.handler = handler;
		this.query = query;
		this.retry = retry;
	}

	public void trackLoaded(AudioTrack track) {
		this.handler.log("track loadet: " + track.getInfo().title);
		this.handler.getPlayer().queue(new Track(track, this.member));
	}

	public void playlistLoaded(AudioPlaylist playlist) {
		this.handler.log("loadet playlist:" + playlist.getName());
		for (AudioTrack track : playlist.getTracks()) {
			this.handler.getPlayer().queue(new Track(track, this.member));
		}
	}

	public void noMatches() {
		this.handler.log("no matches found");
		if (this.retry) {
			this.handler.getPlayer().playSearch(this.query, this.member);
		}
	}

	public void loadFailed(FriendlyException exception) {
		this.handler.sendErrorMessage(exception.getMessage());
	}

}
