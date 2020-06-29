package halleg.discordmusikbot.guild.player.tracks;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;

public class LoadablePlaylist extends TrackPlaylist<LoadableTrack> {

    public LoadablePlaylist(GuildHandler handler, Playlist list, PlaylistTrack[] tracks, Member member, String uri) {
        super(list.getName(), list.getOwner().getDisplayName(), list.getImages()[0].getUrl(), uri, member);
        this.tracks = new LoadableTrack[tracks.length];
        for (int i = 0; i < this.tracks.length; i++) {
            String art = "";
            for (ArtistSimplified a : tracks[i].getTrack().getArtists()) {
                art += " " + a.getName();
            }
            this.tracks[i] = new LoadableTrack(member, tracks[i].getTrack().getName() + art, false);
        }
    }
}
