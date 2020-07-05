package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;

public class LoadableTrack extends Track {
    public String source;

    public LoadableTrack(Member member, String source) {
        super(null, member);
        this.source = source;
    }

    public void setTrack(AudioTrack track) {
        this.track = track;
    }

    @Override
    public String getTitle() {
        if (isLoaded()) {
            return super.getTitle();
        } else {
            return "**Loading...**";
        }
    }

    @Override
    public String getEmbedLink() {
        if (isLoaded()) {
            return super.getTitle();
        } else {
            return getTitle();
        }
    }

    public boolean isLoaded() {
        return this.track != null;
    }

    public String getSource() {
        return this.source;
    }
}
