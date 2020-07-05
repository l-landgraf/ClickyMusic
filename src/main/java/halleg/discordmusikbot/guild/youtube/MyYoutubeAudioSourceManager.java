package halleg.discordmusikbot.guild.youtube;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

/**
 * Extends the  {@link YoutubeAudioSourceManager} by removing all parameters except the videoid from the url in load Item before passing it along
 */
public class MyYoutubeAudioSourceManager extends YoutubeAudioSourceManager {

    public static String IDENT_PREFIX = "https://www.youtube.com/watch";
    
    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        //remove all parameters except "v" (video id)
        String newIdent = null;

        if (!reference.identifier.startsWith(IDENT_PREFIX)) {
            return null;
        }
        String[] slpit = reference.identifier.split("\\?");
        if (!IDENT_PREFIX.equals(slpit[0])) {
            return null;
        }
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
}
