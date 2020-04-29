package halleg.discordmusikbot.guild.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.tracks.LoadablePlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlaylistQueueElement extends QueueElement {
    private LoadablePlaylist playlist;
    private List<Integer> randList;
    private int[] plannedNormal;
    private int[] plannedShuffle;

    private int currentTrack;
    private boolean shuffle;

    public PlaylistQueueElement(Player player, LoadablePlaylist playlist) {
        super(player);
        this.playlist = playlist;
        this.currentTrack = 0;
        this.randList = IntStream.range(1, this.playlist.getTotal()).boxed().collect(Collectors.toList());
        Collections.shuffle(this.randList);
        updatePlanedNormal();
        updatePlanedShuffle();
    }

    @Override
    public MessageEmbed buildMessage(QueueStatus status) {
        super.buildMessage(status);
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(this.playlist.getTitle(), this.playlist.getURI());
        eb.setDescription("Queued by " + this.playlist.getMember().getAsMention());
        eb.setThumbnail(this.playlist.getThumbnail());

        eb.addField("Playlist By", this.playlist.getAuthor(), true);
        eb.addField("Songs", this.playlist.getTotal() + "", true);
        eb.addField("", "", true);

        if (status.getKeepLoading()) {
            addNowPlayingRow(eb);
            addCommingUpRows(eb);
        }

        return eb.build();
    }

    private void updateMessage() {
        if (this.message == null) {
            return;
        }
        this.message.editMessage(buildMessage(this.status)).queue(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                PlaylistQueueElement.this.message = message;
            }
        });
    }

    private void checkUpdate(int t) {
        int[] planned = getPlanedSongs();
        for (int i = 0; i < GuildHandler.PLAYLIST_PREVIEW_MAX; i++) {
            if (planned[i] == t) {
                updateMessage();
                return;
            }
        }
    }

    private void addNowPlayingRow(EmbedBuilder eb) {
        if (this.currentTrack < this.playlist.getTotal()) {
            String title = "";

            if (this.status == QueueStatus.QUEUED) {
                title = "First Song";
            } else {
                title = "Now Playing";
            }
            String moreShuffle = "";
            eb.addField(title, this.playlist.getTrack(this.currentTrack).getEmbedLink() + moreShuffle, true);
            eb.addField("By", this.playlist.getTrack(this.currentTrack).getAuthor(), true);
            eb.addField("Length", this.playlist.getTrack(this.currentTrack).getLength(), true);
        }
    }

    private void addCommingUpRows(EmbedBuilder eb) {
        String s = "";
        int[] plan = getPlanedSongs();
        int counter = 0;
        for (int i : plan) {
            if (counter >= GuildHandler.PLAYLIST_PREVIEW_MAX) {
                break;
            }
            counter++;
            String title = this.playlist.getTrack(i).getEmbedLink();
            s += (counter + 1) + ". " + title + "\n";
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

    private int[] getPlanedSongs() {
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

    private void nextInternal() {
        int[] planned = getPlanedSongs();
        if (planned.length == 0) {
            this.player.nextTrack();
            return;
        }
        this.currentTrack = planned[0];
        this.randList.remove(Integer.valueOf(this.currentTrack));

        if (this.shuffle) {
            updatePlanedShuffle();
        } else {
            updatePlanedNormal();
        }

        nextAvailable();
    }

    private void nextAvailable() {
        if (this.currentTrack == this.playlist.getTotal()) {
            this.player.nextTrack();
            return;
        }


        if (!this.playlist.getTrack(this.currentTrack).isReady()) {
            nextInternal();
            return;
        }
        this.player.playTrack(this.playlist.getTrack(this.currentTrack).getTrack());
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
        if (this.status != QueueStatus.PLAYING) {
            nextAvailable();
        }
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
        super.onEnded();
        nextInternal();
    }

    @Override
    public void onSkip() {
        //DONT call super.onSkip()
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

    public int getCurrentTrack() {
        return this.currentTrack;
    }

    public boolean isBeeingLoaded(int i) {
        return this.playlist.getTrack(i).isBeeingLoaded();
    }

    public int getTotal() {
        return this.playlist.getTotal();
    }

    public String getSource(int i) {
        return this.playlist.getTrack(i).getSource();
    }

    public void startLoading(int i) {
        this.playlist.getTrack(i).startLoading();
    }

    public void loadTrack(int i, AudioTrack track) {
        this.playlist.getTrack(i).loadTrack(track);
        checkUpdate(i);
    }

    public void notFound(int i) {
        this.playlist.getTrack(i).notFound();
        checkUpdate(i);
    }
}
