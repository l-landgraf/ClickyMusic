package halleg.discordmusikbot.player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;

public class Track {
	private AudioTrack track;
	private Member member;
	private String thumbnail;

	public Track(AudioTrack track, Member member) {
		this.track = track;
		this.member = member;
		this.thumbnail = loadThumbnail();
	}

	private String loadThumbnail() {
		String string = this.track.getInfo().uri;
		String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(string);
		if (matcher.find()) {
			return "https://img.youtube.com/vi/" + matcher.group() + "/default.jpg";
		} else {
			return null;
		}
	}

	public boolean hasThumbnail() {
		return this.thumbnail != null;
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
}
