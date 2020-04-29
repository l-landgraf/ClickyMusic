package halleg.discordmusikbot.guild.player.tracks;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;

public class LoadablePlaylist {
    private LoadableTrack[] tracks;
    private String title;
    private String author;
    private String thumbnail;
    private Member member;
    private String uri;

    public LoadablePlaylist(GuildHandler handler, Playlist list, PlaylistTrack[] tracks, Member member, String uri) {
        this.title = list.getName();
        this.author = list.getOwner().getDisplayName();
        this.thumbnail = list.getImages()[0].getUrl();
        this.member = member;
        this.tracks = new LoadableTrack[tracks.length];
        this.uri = uri;
        for (int i = 0; i < this.tracks.length; i++) {
            String art = "";
            for (ArtistSimplified a : tracks[i].getTrack().getArtists()) {
                art += " " + a.getName();
            }
            this.tracks[i] = new LoadableTrack(member, tracks[i].getTrack().getName() + art);
        }
    }

    public LoadableTrack getTrack(int i) {
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
