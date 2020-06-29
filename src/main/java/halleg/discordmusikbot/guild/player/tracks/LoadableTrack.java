package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;

public class LoadableTrack extends Track {
    protected boolean notFound;
    protected boolean beeingLoaded;
    protected String source;
    protected boolean rawSource;

    public LoadableTrack(Member member, String source, boolean rawSource) {
        super(null, member);
        this.notFound = false;
        this.source = source;
        this.beeingLoaded = false;
        this.rawSource = rawSource;
    }

    public String getSource() {
        return this.source;
    }

    public void startLoading() {
        this.beeingLoaded = true;
    }

    public boolean isBeeingLoaded() {
        return this.beeingLoaded;
    }

    public void loadTrack(AudioTrack track) {
        this.beeingLoaded = true;
        this.track = track;
    }

    public void notFound() {
        this.notFound = true;
    }

    public boolean isLoading() {
        return this.track == null && !this.notFound;
    }

    public boolean isNotFound() {
        return this.track == null && this.notFound;
    }

    public boolean isReady() {
        return this.track != null;
    }

    @Override
    public String getTitle() {
        if (isLoading()) {
            return "**Loading...**";
        }
        if (isNotFound()) {
            return "**Not Found**";
        }
        return super.getTitle();
    }

    @Override
    public String getEmbedLink() {
        if (!isReady()) {
            return getTitle();
        }
        return super.getEmbedLink();
    }

    @Override
    public String getAuthor() {
        if (!isReady()) {
            return "";
        }
        return super.getAuthor();
    }

    @Override
    public String getLength() {
        if (!isReady()) {
            return "";
        }
        return super.getLength();
    }

    @Override
    public String getURI() {
        if (!isReady()) {
            return "";
        }
        return super.getURI();
    }

    public boolean isRawSource() {
        return this.rawSource;
    }
}
