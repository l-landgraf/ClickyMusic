package halleg.discordmusikbot.guild.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.PlaylistQueueElement;
import net.dv8tion.jda.api.entities.Member;

class PlaylistTrackLoadHandler extends LoadHandler {
    protected PlaylistQueueElement element;
    protected int trackNr;
    protected int retryAmount;
    protected boolean random;

    public PlaylistTrackLoadHandler(GuildHandler handler, Member member, PlaylistQueueElement ele, boolean random) {
        super(handler, null, member);
        this.element = ele;
        this.trackNr = -1;
        this.retryAmount = 5;
        this.random = random;
    }

    public synchronized void loadNext() {
        int[] arr = null;
        if (!this.element.getStatus().getKeepLoading()) {
            return;
        }

        if (this.random) {
            arr = this.element.getPlanedShuffle();
        } else {
            arr = this.element.getPlanedNormal();
        }

        boolean done = true;
        for (int i : arr) {
            if (!this.element.isBeeingLoaded(i)) {
                this.trackNr = i;
                done = false;
                break;
            }
        }

        if (done) {
            return;
        }

        this.element.startLoading(this.trackNr);
        this.source = this.element.getSource(this.trackNr);
        this.retryAmount = GuildHandler.RETRY_AMOUNT;
        load();
    }

    @Override
    protected void onTrackLoaded(AudioTrack track) {
        this.element.loadTrack(this.trackNr, track);
        this.random = !this.random;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadNext();
    }

    @Override
    public void load() {
        this.handler.getLoader().search(this.handler.getLoader().youtubeSearch(this.source), this, this.member);
    }

    @Override
    public void noMatches() {
        if (this.retryAmount > 0) {
            this.handler.log("no matches found, retrying " + this.retryAmount + " \"" + this.source + "\"");
            this.retryAmount--;
            load();
        } else {
            this.handler.log("no matches found, giving up " + this.retryAmount + " \"" + this.source + "\"");
            this.element.notFound(this.trackNr);
            this.random = !this.random;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loadNext();
        }
    }
}

