//package halleg.discordmusikbot.guild.loader;
//
//import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
//import halleg.discordmusikbot.guild.GuildHandler;
//import halleg.discordmusikbot.guild.player.queue.playlist.LoadablePlaylistQueueElement;
//import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
//import net.dv8tion.jda.api.entities.Member;
//
//public class LoadablePlaylistLoadHandler extends RetryLoadHandler {
//    protected LoadablePlaylist list;
//    protected int currentTrack;
//    protected boolean rawSource;
//
//    public LoadablePlaylistLoadHandler(GuildHandler handler, Member member, LoadablePlaylist list, boolean rawSource) {
//        super(handler, list.getTrack(0).getSource(), member, GuildHandler.RETRY_AMOUNT);
//        this.list = list;
//        this.currentTrack = 0;
//        this.retryAmount = GuildHandler.RETRY_AMOUNT;
//        this.rawSource = rawSource;
//    }
//
//    @Override
//    public void load() {
//        if (this.rawSource) {
//            this.handler.getLoader().load(this.source, this);
//        } else {
//            this.handler.getLoader().load(this.handler.getLoader().youtubeSearch(this.source), this);
//        }
//    }
//
//    @Override
//    protected void onTrackLoaded(AudioTrack track) {
//        super.onTrackLoaded(track);
//        this.list.getTrack(this.currentTrack).loadTrack(track);
//        LoadablePlaylistQueueElement ele = new LoadablePlaylistQueueElement(this.handler.getPlayer(), this.list);
//        this.handler.getPlayer().queueComplete(ele);
//
//        LoadablePlaylistTracksLoadHandler rand = new LoadablePlaylistTracksLoadHandler(this.handler, this.member, ele, true);
//        rand.loadNext();
//    }
//
//    @Override
//    protected void onTrackLoadFailed() {
//        this.list.getTrack(this.currentTrack).notFound();
//        this.currentTrack++;
//        if (this.currentTrack >= this.list.getTotal()) {
//            super.onTrackLoadFailed();
//            return;
//        }
//        this.source = this.list.getTrack(this.currentTrack).getSource();
//        this.retryAmount = GuildHandler.RETRY_AMOUNT;
//        load();
//    }
//}
