package halleg.discordmusikbot.guild.youtube;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public class RetryYoutubeSearchProvider extends YoutubeSearchProvider {
    @Override
    public AudioItem loadSearchResult(String query, Function<AudioTrackInfo, AudioTrack> trackFactory) {
        int i = 8;

        while (true) {
            System.out.println("searching youtube;https://www.youtube.com/watch?v=DJZ6OaA_Ogk attempts left: " + i +
                    " for \"" + query + "\"");
            try {
                return super.loadSearchResult(query, trackFactory);
            } catch (Exception e) {
                System.out.println("search failed: " + e.getMessage());
                i--;
                if (i < 0) {
                    throw e;
                }
            }
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
