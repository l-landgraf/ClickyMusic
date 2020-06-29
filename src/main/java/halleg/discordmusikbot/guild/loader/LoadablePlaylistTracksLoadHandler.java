//package halleg.discordmusikbot.guild.loader;
//
//import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
//import halleg.discordmusikbot.guild.GuildHandler;
//import halleg.discordmusikbot.guild.player.queue.playlist.LoadablePlaylistQueueElement;
//import net.dv8tion.jda.api.entities.Member;
//
//class LoadablePlaylistTracksLoadHandler extends RetryLoadHandler {
//    protected LoadablePlaylistQueueElement element;
//    protected int trackNr;
//    protected boolean random;
//    protected boolean rawSource;
//
//    public LoadablePlaylistTracksLoadHandler(GuildHandler handler, Member member, LoadablePlaylistQueueElement ele, boolean random) {
//        super(handler, null, member, GuildHandler.RETRY_AMOUNT);
//        this.element = ele;
//        this.trackNr = -1;
//        this.random = random;
//    }
//
//    public synchronized void loadNext() {
//        int[] arr = null;
//        if (!this.element.getStatus().getKeepLoading()) {
//            return;
//        }
//
//        if (this.random) {
//            arr = this.element.getPlanedShuffle();
//        } else {
//            arr = this.element.getPlanedNormal();
//        }
//
//        boolean done = true;
//        for (int i : arr) {
//            if (!this.element.isBeeingLoaded(i)) {
//                this.trackNr = i;
//                done = false;
//                break;
//            }
//        }
//
//        if (done) {
//            return;
//        }
//
//        this.element.startLoading(this.trackNr);
//        this.source = this.element.getSource(this.trackNr);
//        this.rawSource = this.element.isRawSource(this.trackNr);
//        this.retryAmount = GuildHandler.RETRY_AMOUNT;
//        load();
//    }
//
//    @Override
//    protected void onTrackLoaded(AudioTrack track) {
//        super.onTrackLoaded(track);
//        this.element.loadTrack(this.trackNr, track);
//        this.random = !this.random;
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        loadNext();
//    }
//
//    @Override
//    protected void onTrackLoadFailed() {
//        this.element.notFound(this.trackNr);
//        this.random = !this.random;
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        loadNext();
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
//}
//
