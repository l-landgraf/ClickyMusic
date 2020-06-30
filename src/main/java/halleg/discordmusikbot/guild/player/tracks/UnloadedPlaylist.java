package halleg.discordmusikbot.guild.player.tracks;

import net.dv8tion.jda.api.entities.Member;

public class UnloadedPlaylist extends TrackPlaylist<Track> {
    private int total;

    public UnloadedPlaylist(String title, String author, String thumbnail, String uri, Member member, int total) {
        super(title, author, thumbnail, uri, member);
        this.total = total;
    }

    @Override
    public int getTotal() {
        if (this.tracks == null) {
            return this.total;
        } else {
            return super.getTotal();
        }
    }

    @Override
    public Track getTrack(int i) {
        throw new IllegalStateException("called get Track from unloaded playlist");
    }
}
