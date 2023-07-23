package halleg.discordmusikbot.guild.player.queue.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import halleg.discordmusikbot.guild.player.queue.QueueStatus;
import halleg.discordmusikbot.guild.player.tracks.Track;
import halleg.discordmusikbot.guild.player.tracks.TrackPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class PlaylistQueueElement<L extends TrackPlaylist> extends QueueElement {
    protected L playlist;
    protected List<Integer> plannedShuffle;
    protected List<Integer> plannedNormal;

    //the index of the current track in the planned-lists
    protected int currentPlanedIndex;
    //the index of the current track in the playlist
    protected int currentTrackIndex;


    public PlaylistQueueElement(QueuePlayer player, L playlist) {
        super(player, true);
        this.playlist = playlist;
        this.currentPlanedIndex = 0;

        this.plannedShuffle = IntStream.range(0, this.playlist.getTotal()).boxed().collect(Collectors.toList());
        Collections.shuffle(this.plannedShuffle);

        this.plannedNormal = IntStream.range(0, this.playlist.getTotal()).boxed().collect(Collectors.toList());
    }

    @Override
    public MessageEmbed buildMessageEmbed(QueueStatus status) {
        super.buildMessageEmbed(status);
        EmbedBuilder eb = new EmbedBuilder();

        addPlaylistRow(eb);
        if (status.getKeepLoading()) {
            addNowPlayingRow(eb);
            addCommingUpRows(eb);
        }

        if (status == QueueStatus.PLAYING) {
            addProgressBar(eb, this.getCurrentTrack());
        }
        return eb.build();
    }


    protected void addPlaylistRow(EmbedBuilder eb) {
        eb.setTitle(this.playlist.getTitle(), this.playlist.getURI());
        eb.setDescription("Queued by " + this.playlist.getMember().getAsMention());
        if (this.playlist.getThumbnail() != null) {
            eb.setThumbnail(this.playlist.getThumbnail());
        }

        if (this.playlist.getAuthor() != null) {
            eb.addField("Playlist By", this.playlist.getAuthor(), true);
        }

        eb.addField("Songs", this.playlist.getTotal() + "", true);

        //add padding after songs
        if (this.playlist.getAuthor() == null) {
            eb.addField("", "", true);
        }

        eb.addField("", "", true);
    }

    protected void addNowPlayingRow(EmbedBuilder eb) {
        if (this.currentPlanedIndex < this.playlist.getTotal()) {
            Track curr = this.playlist.getTrack(this.currentTrackIndex);
            eb.setThumbnail(curr.getThumbnail());
            String title;

            if (this.status == QueueStatus.QUEUED) {
                title = "First Song";
            } else {
                title = "Now Playing";
            }

            String moreShuffle = "";
            eb.addField(title, curr.getTitleEmbedLink() + moreShuffle, true);
            eb.addField("By", curr.getAuthorEmbedLink(), true);
            eb.addField("Length", curr.getLength(), true);
        }
    }

    protected void addCommingUpRows(EmbedBuilder eb) {
        String s = "";
        List<Integer> plan = getPlanedTracks();
        int counter = 0;
        for (int i = this.currentPlanedIndex + 1; i < plan.size(); i++) {
            if (counter >= GuildHandler.PLAYLIST_PREVIEW_MAX) {
                break;
            }
            counter++;
            String title = this.playlist.getTrack(plan.get(i)).getTitleEmbedLink();
            s += plan.get(i) + ". " + title + "\n";
        }

        int songsLeft = (this.playlist.getTotal() - (this.currentPlanedIndex + GuildHandler.PLAYLIST_PREVIEW_MAX + 1));
        if (songsLeft > 0) {
            if (this.isShuffle) {
                s += "**-- Shuffling " + songsLeft + " More --**";
            } else {
                s += "**-- " + songsLeft + " More --**";
            }

        }

        if (counter > 0) {
            eb.addField("Comming up", s, false);
        }
    }

    protected int getPlanedTrack(int songNr) {
        try {
            return getPlanedTracks().get(songNr);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    protected List<Integer> getPlanedTracks() {
        if (this.isShuffle) {
            return getPlanedShuffle();
        } else {
            return getPlanedNormal();
        }
    }

    public List<Integer> getPlanedNormal() {
        return this.plannedNormal;
    }

    public List<Integer> getPlanedShuffle() {
        return this.plannedShuffle;
    }

    protected void nextInternal() {
        if (!setCurrent(this.currentPlanedIndex + 1)) {
            super.onSkip();
            return;
        }
        this.status = QueueStatus.PLAYING;


        playCurrent();
    }

    protected void prevInternal() {
        if (!setCurrent(this.currentPlanedIndex - 1)) {
            setCurrent(0);
        }
        this.status = QueueStatus.PLAYING;
        playCurrent();
    }

    protected void playCurrent() {
        this.player.playTrack(this.playlist.getTrack(this.currentTrackIndex).getTrack());
        updateMessage();
    }

    @Override
    public AudioTrack getCurrentTrack() {
        return this.playlist.getTrack(this.currentTrackIndex).getTrack();
    }

    @Override
    public void onEnded() {
        nextInternal();
    }


    @Override
    public void onNext() {
        nextInternal();
    }

    @Override
    public void onPrevious() {
        prevInternal();
    }

    @Override
    public void runPlay(int i) {
        int planned = getPlanedTrack(i);
        if (planned < 0) {
            this.player.getHandler().sendErrorMessage("SongNr out of Bounds.");
            return;
        }
        this.currentPlanedIndex = i;
        playCurrent();
    }

    protected boolean setCurrent(int index) {
        try {
            this.currentTrackIndex = getPlanedTracks().get(index);
            this.currentPlanedIndex = index;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

}
