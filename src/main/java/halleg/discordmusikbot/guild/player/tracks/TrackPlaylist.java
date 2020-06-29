package halleg.discordmusikbot.guild.player.tracks;

import net.dv8tion.jda.api.entities.Member;

public abstract class TrackPlaylist<T extends Track> {
    protected T[] tracks;
    protected String title;
    protected String author;
    protected String thumbnail;
    protected String uri;
    protected Member member;

    protected TrackPlaylist(String title, String author, String thumbnail, String uri, Member member) {
        this.title = title;
        this.author = author;
        this.thumbnail = thumbnail;
        this.uri = uri;
        this.member = member;
    }

    public T getTrack(int i) {
        return this.tracks[i];
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public Member getMember() {
        return this.member;
    }

    public int getTotal() {
        return this.tracks.length;
    }

    public String getURI() {
        return this.uri;
    }


}
