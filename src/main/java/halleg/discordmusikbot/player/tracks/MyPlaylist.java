package halleg.discordmusikbot.player.tracks;

import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Playlist;
import net.dv8tion.jda.api.entities.Member;

public class MyPlaylist {
    private Member member;
    private String querry;
    private String name;
    private String thumbnail;
    private String author;
    private int total;

    public MyPlaylist(Member member, String querry, Playlist list) {
        this.member = member;
        this.querry = querry;
        this.name = list.getName();
        this.thumbnail = list.getImages()[0].getUrl();
        this.author = list.getOwner().getDisplayName();
        this.total = list.getTracks().getTotal();
    }

    public MyPlaylist(Member member, String querry, Album album) {
        this.member = member;
        this.querry = querry;
        this.name = album.getName();
        this.thumbnail = album.getImages()[0].getUrl();
        this.total = album.getTracks().getTotal();
        this.author = "";
        for (ArtistSimplified a : album.getArtists()) {
            this.author += a.getName() + "\n";
        }
    }

    public Member getMember() {
        return this.member;
    }

    public String getName() {
        return this.name;
    }

    public String getQuerry() {
        return this.querry;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public String getAuthor() {
        return this.author;
    }

    public int getTotal() {
        return this.total;
    }
}
