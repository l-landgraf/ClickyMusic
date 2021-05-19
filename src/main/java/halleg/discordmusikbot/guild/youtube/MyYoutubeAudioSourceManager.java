package halleg.discordmusikbot.guild.youtube;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends the {@link YoutubeAudioSourceManager} by removing all parameters
 * except the videoid from the url in load Item before passing it along
 */
public class MyYoutubeAudioSourceManager extends YoutubeAudioSourceManager {

	public static String IDENT_PREFIX = "https://www.youtube.com/watch";

	@Override
	public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {

		String[] slpit = reference.identifier.split("\\?");
		if (IDENT_PREFIX.equals(slpit[0])) {
			// remove all parameters except "v" (video id)
			String newIdent = null;

			String[] params = slpit[1].split("&");
			for (String s : params) {
				if (s.startsWith("v=")) {
					newIdent = s.split("=")[1];
				}
			}

			if (newIdent == null) {
				return null;
			}

			newIdent = IDENT_PREFIX + "?v=" + newIdent;

			AudioReference newRef = new AudioReference(newIdent, reference.title, reference.containerDescriptor);
			return super.loadItem(manager, newRef);
		}

		return super.loadItem(manager, reference);
	}

	public static String loadThumbnail(AudioTrack track) {
		if (track == null) {
			return "";
		}

		String string = track.getInfo().uri;
		String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(string);
		if (matcher.find()) {
			return "https://img.youtube.com/vi/" + matcher.group() + "/default.jpg";
		} else {
			return "";
		}
	}
}
