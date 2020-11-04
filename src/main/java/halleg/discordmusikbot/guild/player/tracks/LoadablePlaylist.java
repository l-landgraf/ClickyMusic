package halleg.discordmusikbot.guild.player.tracks;

import net.dv8tion.jda.api.entities.Member;

public class LoadablePlaylist extends TrackPlaylist<LoadableTrack> {

    public LoadablePlaylist(String title, String author, String thumbnail, String uri, Member member, String[] sources,String[] images) {
        super(title, author, thumbnail, uri, member);
        this.tracks = new LoadableTrack[sources.length];
        for (int i = 0; i < sources.length; i++) {
            this.tracks[i] = new LoadableTrack(member, sources[i],images[i]);
        }
    }
}
