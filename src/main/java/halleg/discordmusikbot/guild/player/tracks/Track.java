package halleg.discordmusikbot.guild.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Track {
    protected AudioTrack track;
    protected Member member;
    protected String thumbnail;

    public Track(AudioTrack track, Member member) {
        this.track = track;
        this.member = member;
        this.thumbnail = loadThumbnail();
    }

    protected String loadThumbnail() {
        if (this.track == null) {
            return "";
        }

        String string = this.track.getInfo().uri;
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(string);
        if (matcher.find()) {
            return "https://img.youtube.com/vi/" + matcher.group() + "/default.jpg";
        } else {
            return "";
        }
    }

    public String getTitle() {
        return this.track.getInfo().title;
    }

    public String getEmbedLink() {
        return "[" + this.track.getInfo().title + "](" + this.track.getInfo().uri + ")";
    }

    public String getAuthor() {
        return this.track.getInfo().author;
    }

    public String getLength() {
        return toTime(this.track.getInfo().length);
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

    protected String toTime(long length) {
        String sec = Long.toString((length / 1000l) % 60l);
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        String min = Long.toString((length / 60000));
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        return min + ":" + sec;
    }
}
