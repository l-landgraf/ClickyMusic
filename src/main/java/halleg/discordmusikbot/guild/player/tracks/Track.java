package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;

public class Track {
    protected AudioTrack track;
    protected Member member;
    protected String thumbnail;
    protected String authorLink;

    public Track(AudioTrack track, Member member, String authorLink, String image) {
        this.track = track;
        this.member = member;
        this.thumbnail = image;
        this.authorLink = authorLink;
    }

    public String getTitle() {
        return this.track.getInfo().title;
    }

    public String getTitleEmbedLink() {
        if (getURI() == null) {
            return getTitle();
        }
        return "[" + getTitle() + "](" + getURI() + ")";
    }

    public String getAuthor() {
        return this.track.getInfo().author;
    }

    public String getAuthorEmbedLink() {
        if (getAuthorLink() == null) {
            return getAuthor();
        }
        return "[" + getAuthor() + "](" + getAuthorLink() + ")";
    }

    public String getLength() {
        return toTime(this.track.getInfo().length);
    }

    public long getSize() {
        return this.track.getInfo().length;
    }

    public String getURI() {
        return this.track.getInfo().uri;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public AudioTrack getTrack() {
        return this.track;
    }

    public Member getMember() {
        return this.member;
    }

    public String getAuthorLink() {
        return this.authorLink;
    }

    public static String toTime(long length) {

        int seconds = (int) (length / 1000) % 60;
        int minutes = (int) ((length / (1000 * 60)) % 60);
        int hours = (int) ((length / (1000 * 60 * 60)) % 24);

        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d",
                seconds);
    }
}
