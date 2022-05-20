package halleg.discordmusikbot.guild;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import halleg.discordmusikbot.guild.loader.InititalPlaylistLoadHandler;
import halleg.discordmusikbot.guild.loader.SingleLoadHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class TrackLoader {
    private GuildHandler handler;
    private AudioPlayerManager manager;
    private PlaylistPreloadManager preloader;

    public TrackLoader(GuildHandler handler, PlaylistPreloadManager preloader) {
        this.handler = handler;
        this.manager = handler.getManager();
        this.preloader = preloader;
    }

    public void load(String source, AudioLoadResultHandler loader) {
        this.manager.loadItem(source, loader);
    }

    public void search(String source, QueuePlayer player, Member member, Message message) {
        if (source == null) {
            return;
        }

        InititalPlaylistLoadHandler loader = this.preloader.load(this.handler, source, member, message);
        if (loader != null) {
            load(loader.getInitialSource(), loader);
        } else {
            load(source, new SingleLoadHandler(this.handler, player, source, member, message));
        }

    }

    public interface PlaylistPreloadManager {
        public InititalPlaylistLoadHandler load(GuildHandler handler, String source, Member member, Message message);
    }
}
