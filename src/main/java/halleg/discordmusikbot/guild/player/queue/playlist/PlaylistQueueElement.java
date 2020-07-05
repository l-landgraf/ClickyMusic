package halleg.discordmusikbot.guild.player.queue.playlist;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import halleg.discordmusikbot.guild.player.queue.QueueStatus;
import halleg.discordmusikbot.guild.player.tracks.Track;
import halleg.discordmusikbot.guild.player.tracks.TrackPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class PlaylistQueueElement<L extends TrackPlaylist> extends QueueElement {
    protected L playlist;
    protected List<Integer> randList;
    protected int[] plannedNormal;
    protected int[] plannedShuffle;

    protected int currentTrack;
    protected boolean shuffle;

    public PlaylistQueueElement(Player player, L playlist) {
        super(player);
        this.playlist = playlist;
        this.currentTrack = 0;
        this.randList = IntStream.range(1, this.playlist.getTotal()).boxed().collect(Collectors.toList());
        Collections.shuffle(this.randList);
        updatePlanedNormal();
        updatePlanedShuffle();
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

        return eb.build();
    }

    protected void updateMessage() {
        if (this.message == null) {
            return;
        }
        this.message.editMessage(buildMessageEmbed(this.status)).queue(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                PlaylistQueueElement.this.message = message;
            }
        });
    }

    protected void checkUpdate(int t) {
        int[] planned = getPlanedSongs();
        for (int i = 0; i < GuildHandler.PLAYLIST_PREVIEW_MAX; i++) {
            if (planned[i] == t) {
                updateMessage();
                return;
            }
        }
    }

    protected void addPlaylistRow(EmbedBuilder eb) {
        eb.setTitle(this.playlist.getTitle(), this.playlist.getURI());
        eb.setDescription("Queued by " + this.playlist.getMember().getAsMention());
        eb.setThumbnail(this.playlist.getThumbnail());

        eb.addField("Playlist By", this.playlist.getAuthor(), true);
        eb.addField("Songs", this.playlist.getTotal() + "", true);
        eb.addField("", "", true);
    }

    protected void addNowPlayingRow(EmbedBuilder eb) {
        if (this.currentTrack < this.playlist.getTotal()) {
            String title = "";

            if (this.status == QueueStatus.QUEUED) {
                title = "First Song";
            } else {
                title = "Now Playing";
            }

            String moreShuffle = "";
            eb.addField(title, getCurrentTrack().getEmbedLink() + moreShuffle, true);
            eb.addField("By", getCurrentTrack().getAuthor(), true);
            eb.addField("Length", getCurrentTrack().getLength(), true);
        }
    }

    protected void addCommingUpRows(EmbedBuilder eb) {
        String s = "";
        int[] plan = getPlanedSongs();
        int counter = 0;
        for (int i : plan) {
            if (counter >= GuildHandler.PLAYLIST_PREVIEW_MAX) {
                break;
            }
            counter++;
            String title = this.playlist.getTrack(i).getEmbedLink();
            s += i + ". " + title + "\n";
        }

        int songsLeft = (plan.length - GuildHandler.PLAYLIST_PREVIEW_MAX);
        if (songsLeft > 0) {
            if (this.shuffle) {
                s += "**-- Shuffling " + songsLeft + " More --**";
            } else {
                s += "**-- " + songsLeft + " More --**";
            }

        }

        if (plan.length > 0) {
            eb.addField("Coming Up", s, false);
        }
    }

    protected int getNextPlanedSong() {
        try {
            return getPlanedSongs()[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    protected int[] getPlanedSongs() {
        int[] arr = null;
        if (this.shuffle) {
            arr = getPlanedShuffle();
        } else {
            arr = getPlanedNormal();
        }


        return arr;
    }

    public int[] getPlanedNormal() {
        return this.plannedNormal;
    }

    public int[] getPlanedShuffle() {
        return this.plannedShuffle;
    }

    public void updatePlanedNormal() {
        int length = this.playlist.getTotal() - this.currentTrack - 1;
        if (length < 0) {
            this.plannedNormal = new int[0];
            return;
        }
        this.plannedNormal = new int[length];
        for (int i = 0; i < this.plannedNormal.length; i++) {
            this.plannedNormal[i] = i + this.currentTrack + 1;
        }
    }

    public void updatePlanedShuffle() {
        this.plannedShuffle = this.randList.stream().mapToInt(i -> i).toArray();
    }

    protected void nextInternal() {
        int planned = getNextPlanedSong();
        if (planned < 0) {
            super.onSkip();
            return;
        }
        this.status = QueueStatus.PLAYING;
        this.currentTrack = planned;
        this.randList.remove(Integer.valueOf(this.currentTrack));

        if (this.shuffle) {
            updatePlanedShuffle();
        } else {
            updatePlanedNormal();
        }

        queueCurrent();
    }

    protected void queueCurrent() {
        this.player.playTrack(getCurrentTrack().getTrack());
        updateMessage();
    }

    @Override
    public void onShuffle() {
        this.shuffle = !this.shuffle;
        if (this.shuffle) {
            updatePlanedShuffle();
        } else {
            updatePlanedNormal();
        }
        updateMessage();
    }

    @Override
    public void onQueued() {
        super.onQueued();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_EMOJI).queue();
    }

    @Override
    public synchronized void onPlaying() {
        queueCurrent();
        super.onPlaying();
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.addReaction(GuildHandler.SKIP_EMOJI).queue();
        this.message.addReaction(GuildHandler.SHUFFLE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_ALL_EMOJI).queue();
    }

    @Override
    public void onPlayed() {
        super.onPlayed();
        updateMessage();
        this.message.clearReactions(GuildHandler.REMOVE_ALL_EMOJI).queue();
        this.message.clearReactions(GuildHandler.SHUFFLE_EMOJI).queue();
        this.message.clearReactions(GuildHandler.SKIP_EMOJI).queue();
        this.message.clearReactions(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
    }

    @Override
    public void onResumePause() {
        this.player.togglePaused();
    }

    @Override
    public void onEnded() {
        nextInternal();
    }

    @Override
    public void onSkip() {
        nextInternal();
    }

    @Override
    public void onDelete() {
        super.onDelete();
        updateMessage();
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.player.removeElement(this);
    }

    @Override
    public void onDeletePlaylist() {
        super.onDelete();
        this.player.nextTrack();
    }

    public Track getCurrentTrack() {
        return this.playlist.getTrack(this.currentTrack);
    }

    public int getTotal() {
        return this.playlist.getTotal();
    }


}
