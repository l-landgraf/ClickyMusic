//package halleg.discordmusikbot.guild.youtube;
//
//import halleg.discordmusikbot.guild.GuildHandler;
//import halleg.discordmusikbot.guild.loader.PlaylistLoadHandler;
//import net.dv8tion.jda.api.entities.Member;
//
//import java.util.List;
//
//public class YoutubeLinkHandler {
//
//    private GuildHandler handler;
//
//    public YoutubeLinkHandler(GuildHandler handler) {
//        this.handler = handler;
//    }
//
//    public boolean handleLink(String link, Member member) {
//
//        if (link.startsWith("https://www.youtube.com")) {
//            PlaylistLoadHandler loader = new PlaylistLoadHandler(this.handler, link, member);
//            loader.load();
//            return true;
//        }
//
//        return false;
//    }
//
//    private void queuePlaylist(PlaylistLink link, Member member) {
////        List<VideoLink> links = YoutubeCrawler.extractLinks(link.buildLink());
////        List<String> tracks = new LinkedList<>(Arrays.asList(new String[links.size()]));
////        for (int i = 0; i < links.size(); i++) {
////
////            if (links.get(i) instanceof PlaylistLink) {
////
////                PlaylistLink l = (PlaylistLink) links.get(i);
////
////                if (l.getList().equals(link.getList()) && l.getIndex() >= link.getIndex()) {
////                    tracks.set(l.getIndex(), l.buildSimpleLink());
////                }
////            }
////        }
////
////        while (tracks.remove(null)) {
////        }
////
////        LoadablePlaylist loadList = new DefaultPlaylist(this.handler, tracks, member, link.buildLink());
////        PlaylistLoadHandler load = new PlaylistLoadHandler(this.handler, member, loadList, true);
////        load.load();
//    }
//
//    private void queueSong() {
//
//    }
//
//    public synchronized String youtubeSearch(String query) {
//
//        List<VideoLink> list = YoutubeCrawler.extractLinks(YoutubeCrawler.buildSearchLink(query));
//        if (!list.isEmpty()) {
//            this.handler.log("found link " + list.get(0).buildLink() + " for \"" + query + "\"");
//            return list.get(0).buildLink();
//        } else {
//            this.handler.log("found nothing on Youtube for \"" + query + "\"");
//            return null;
//        }
//    }
//}
