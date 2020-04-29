package halleg.discordmusikbot.guild;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Member;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;

public class TrackLoader {
    private GuildHandler handler;
    private SpotifyLinkHandler spotifyLinkHandler;
    private AudioPlayerManager manager;

    public TrackLoader(GuildHandler handler) {
        this.handler = handler;
        this.spotifyLinkHandler = new SpotifyLinkHandler(handler);
        this.manager = handler.getManager();
    }

    public void load(String source, AudioLoadResultHandler loader) {
        this.manager.loadItem(source, loader);
    }

    public synchronized String youtubeSearch(String query) {
        try {
            String escape = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8");
            Document doc = Jsoup.connect(escape).get();
            for (Element e : doc.getElementsByTag("a")) {
                String href = e.attr("href");
                if (href.startsWith("/watch?v=")) {
                    this.handler.log("found Youtube link https://www.youtube.com" + href + " for \"" + query + "\"");
                    return "https://www.youtube.com" + href;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.handler.log("found nothing on Youtube for \"" + query + "\"");
        return null;
    }

    public void search(String source, AudioLoadResultHandler loader, Member member) {
        if (source == null) {
            loader.noMatches();
            return;
        }
        if (source.startsWith("https://www.youtube.com") || source.startsWith("www.youtube.com")
                || source.startsWith("youtube.com")) {
            source = source.split("&")[0];
        }

        if (this.spotifyLinkHandler.handleLink(source, member)) {
            return;
        }

        load(source, loader);
    }
}
