package halleg.discordmusikbot.player.tracks;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class MyPlaylistTrack {
    private AudioTrack audioTrack;
    private int nr;

    public MyPlaylistTrack(AudioTrack audioTrack, int nr) {
        this.audioTrack = audioTrack;
        this.nr = nr;
    }

    public AudioTrack getAudioTrack() {
        return this.audioTrack;
    }

    public int getNr() {
        return this.nr;
    }
}
