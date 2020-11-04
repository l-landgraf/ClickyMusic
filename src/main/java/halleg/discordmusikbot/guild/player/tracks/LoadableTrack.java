package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;

public class LoadableTrack extends Track {

	private static String LOADING = "**Loading...**";
	public String source;

	public LoadableTrack(Member member, String source, String image) {
		super(null, member, "", image);
		this.source = source;
		this.thumbnail = image;
	}

	public void setTrack(AudioTrack track) {
		this.track = track;
	}

	@Override
	public String getTitle() {
		if (isLoaded()) {
			return super.getTitle();
		} else {
			return LOADING;
		}
	}

	@Override
	public String getTitleEmbedLink() {
		if (isLoaded()) {
			return super.getTitleEmbedLink();
		} else {
			return LOADING;
		}
	}

	public boolean isLoaded() {
		return this.track != null;
	}

	public String getSource() {
		return this.source;
	}
}
