//package halleg.discordmusikbot.guild.player.spotify;
//
//import com.wrapper.spotify.model_objects.specification.Playlist;
//import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
//import halleg.discordmusikbot.guild.GuildHandler;
//import halleg.discordmusikbot.guild.player.spotify.SpotifyDings;
//import halleg.discordmusikbot.guild.loader.LoadablePlaylistLoadHandler;
//import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
//import net.dv8tion.jda.api.entities.Member;
//
//public class SpotifyLinkHandler {
//    private GuildHandler handler;
//
//    public SpotifyLinkHandler(GuildHandler handler) {
//        this.handler = handler;
//    }
//
//    public boolean handleLink(String link, Member member) {
//        if (link.startsWith("https://open.spotify.com/playlist/")) {
//            String querry = link.replace("https://open.spotify.com/playlist/", "");
//            querry = querry.split("\\?")[0];
//            queuePlaylist(querry, member);
//            return true;
//        } else if (link.startsWith("https://open.spotify.com/album/")) {
//            String querry = link.replace("https://open.spotify.com/album/", "");
//            querry = querry.split("\\?")[0];
//            queueAlbum(querry, member, link);
//            return true;
//        }
//        return false;
//    }
//
//    private void queuePlaylist(String s, Member member) {
//        Playlist list = SpotifyDings.loadPlaylist(s);
//        if (list == null) {
//            this.handler.sendErrorMessage("Playlist Not Found!");
//            return;
//        }
//        PlaylistTrack[] tracks = SpotifyDings.loadPlaylistTracks(s, list.getTracks().getTotal());
//
//        LoadablePlaylist loadList = new LoadablePlaylist(this.handler, list, tracks, member, "https://open.spotify.com/playlist/" + s);
//        LoadablePlaylistLoadHandler load = new LoadablePlaylistLoadHandler(this.handler, member, loadList, false);
//        load.load();
//    }
//
//    private void queueAlbum(String s, Member member, String querry) {
//
//    }
//}
//
