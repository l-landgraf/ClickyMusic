package halleg.discordmusikbot.player;

import com.wrapper.spotify.model_objects.specification.*;
import halleg.discordmusikbot.SpotifyDings;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.loader.PlaylistLoadSynchonizer;
import halleg.discordmusikbot.player.tracks.MyPlaylist;
import net.dv8tion.jda.api.entities.Member;

public class SpotifyLinkHandler {
    private GuildHandler handler;

    public SpotifyLinkHandler(GuildHandler handler) {
        this.handler = handler;
    }

    public boolean handleLink(String link, Member member) {
        if (link.startsWith("https://open.spotify.com/playlist/")) {
            String querry = link.replace("https://open.spotify.com/playlist/", "");
            querry = querry.split("\\?")[0];
            queuePlaylist(querry, member, link);
            return true;
        } else if (link.startsWith("https://open.spotify.com/album/")) {
            String querry = link.replace("https://open.spotify.com/album/", "");
            querry = querry.split("\\?")[0];
            queueAlbum(querry, member, link);
            return true;
        }
        return false;
    }

    private void queuePlaylist(String s, Member member, String querry) {
        Playlist list = SpotifyDings.loadPlaylist(s);
        PlaylistLoadSynchonizer sync = new PlaylistLoadSynchonizer(this.handler, member, new MyPlaylist(member, querry, list), querry);
        int counter = 0;
        for (PlaylistTrack track : list.getTracks().getItems()) {
            String artists = "";
            for (ArtistSimplified a : track.getTrack().getArtists()) {
                artists += " " + a.getName();
            }
            this.handler.getPlayer().loadAndQueueSpotify(track.getTrack().getName() + artists, member, sync, counter);
            counter++;
        }
    }

    private void queueAlbum(String s, Member member, String querry) {
        Album album = SpotifyDings.loadAlbum(s);

        PlaylistLoadSynchonizer sync = new PlaylistLoadSynchonizer(this.handler, member, new MyPlaylist(member, querry, album), querry);
        int counter = 0;
        for (TrackSimplified track : album.getTracks().getItems()) {
            String artists = "";
            for (ArtistSimplified a : track.getArtists()) {
                artists += " " + a.getName();
            }
            this.handler.getPlayer().loadAndQueueSpotify(track.getName() + artists, member, sync, counter);
            counter++;
        }
    }
}

