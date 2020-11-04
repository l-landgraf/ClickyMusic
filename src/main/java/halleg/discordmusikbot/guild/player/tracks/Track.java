package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;

public class Track {
	protected AudioTrack track;
	protected Member member;
	protected String thumbnail;
	protected String authorLink;

	public Track(AudioTrack track, Member member, String authorLink, String image) {
		this.track = track;
		this.member = member;
		this.thumbnail = image;
		this.authorLink = authorLink;
	}

	public String getTitle() {
		return this.track.getInfo().title;
	}

	public String getTitleEmbedLink() {
		return "[" + getTitle() + "](" + getURI() + ")";
	}

	public String getAuthor() {
		return this.track.getInfo().author;
	}

	public String getAuthorEmbedLink() {
		return "[" + getAuthor() + "](" + getAuthorLink() + ")";
	}

	public String getLength() {
		return toTime(this.track.getInfo().length);
	}

	public String getURI() {
		return this.track.getInfo().uri;
	}

	public String getThumbnail() {
		return this.thumbnail;
	}

	public AudioTrack getTrack() {
		return this.track;
	}

	public Member getMember() {
		return this.member;
	}

	public String getAuthorLink() {
		return this.authorLink;
	}

	protected String toTime(long length) {
		String sec = Long.toString((length / 1000l) % 60l);
		if (sec.length() < 2) {
			sec = "0" + sec;
		}

		String min = Long.toString((length / 60000));
		if (sec.length() < 2) {
			sec = "0" + sec;
		}

		return min + ":" + sec;
	}
}
