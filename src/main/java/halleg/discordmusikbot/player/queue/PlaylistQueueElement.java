package halleg.discordmusikbot.player.queue;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.Player;
import halleg.discordmusikbot.player.tracks.MyPlaylist;
import halleg.discordmusikbot.player.tracks.MyPlaylistTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class PlaylistQueueElement extends QueueElement {
    private MyPlaylist playlist;
    private MyPlaylistTrack[] tracks;
    private int currentTrack;
    private boolean isActive;
    private boolean shuffle;
    private List<Integer> notPlayed;
    private Random rand;

    public PlaylistQueueElement(Player player, MyPlaylist playlist) {
        super(player);
        this.tracks = new MyPlaylistTrack[playlist.getTotal()];
        this.playlist = playlist;
        this.currentTrack = 0;
        this.isActive = true;
        this.notPlayed = new ArrayList<>();
        this.rand = new Random();
    }

    @Override
    public MessageEmbed buildMessage() {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(this.playlist.getName(), this.playlist.getQuerry());
        eb.setDescription("Queued by " + this.playlist.getMember().getAsMention());
        eb.setThumbnail(this.playlist.getThumbnail());

        eb.addField("Playlist By", this.playlist.getAuthor(), true);
        eb.addField("Songs", this.playlist.getTotal() + "", true);
        eb.addField("", "", true);

        addNowPlayingRow(eb);

        if (!this.shuffle) {
            addCommingUpRows(eb);
        }

        return eb.build();
    }

    private void updateMessage() {
        if (this.message == null) {
            return;
        }
        this.message.editMessage(buildMessage()).queue(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                PlaylistQueueElement.this.message = message;
            }
        });
    }

    private void addNowPlayingRow(EmbedBuilder eb) {
        if (this.currentTrack < this.playlist.getTotal()) {
            if (this.shuffle) {
                eb.addField("Now Playing", getTitle(this.currentTrack) + shuffleString(eb), true);
            } else {
                eb.addField("Now Playing", getTitle(this.currentTrack), true);
            }
            eb.addField("By", getAuthor(this.currentTrack), true);
            eb.addField("Length", getTime(this.currentTrack), true);
        }
    }

    private void addCommingUpRows(EmbedBuilder eb) {
        String s = "";
        if (this.currentTrack + 1 < this.playlist.getTotal()) {
            s += "1. " + getTitle(this.currentTrack + 1) + "\n";
        }
        if (this.currentTrack + 2 < this.playlist.getTotal()) {
            s += "2. " + getTitle(this.currentTrack + 2) + "\n";
        }
        if (this.currentTrack + 3 < this.playlist.getTotal()) {
            s += "3. " + getTitle(this.currentTrack + 3) + "\n";
        }

        if (this.currentTrack + 4 < this.playlist.getTotal()) {
            s += "-- **" + (this.playlist.getTotal() - this.currentTrack - 4) + " More ** --";
        }

        if (!s.equals("")) {
            eb.addField("Comming Up", s, false);
        }
    }

    private String shuffleString(EmbedBuilder eb) {
        if (this.notPlayed.size() > 0) {
            return "\n-- **Shuffeling " + this.notPlayed.size() + " More ** --";
        } else {
            return "";
        }
    }

    private String getTitle(int i) {
        if (this.tracks[i] == null) {
            return "**Loading...**";
        }
        if (this.tracks[i].getAudioTrack() == null) {
            return "**Not Found**";
        }
        return "[" + this.tracks[i].getAudioTrack().getInfo().title + "]" +
                "(" + this.tracks[i].getAudioTrack().getInfo().uri + ")";
    }

    private String getAuthor(int i) {
        if (this.tracks[i] == null || this.tracks[i].getAudioTrack() == null) {
            return "";
        }
        return this.tracks[i].getAudioTrack().getInfo().author;
    }

    private String getTime(int i) {
        if (this.tracks[i] == null || this.tracks[i].getAudioTrack() == null) {
            return "";
        }
        return toTime((this.tracks[i].getAudioTrack().getInfo().length));
    }

    public synchronized void addTrack(MyPlaylistTrack track) {
        if (!this.isActive) {
            return;
        }
        this.tracks[track.getNr()] = track;

        if (track != null && track.getNr() < this.currentTrack + 4) {
            updateMessage();
        }
    }


    private void nextInternal() {

        if (this.shuffle) {
            if (this.notPlayed.size() <= 0) {
                this.player.nextTrack();
                return;
            }
            int r = this.rand.nextInt(this.notPlayed.size());
            this.currentTrack = this.notPlayed.get(r);
            this.notPlayed.remove(r);
            checkCurrentTrackNull();
        } else {
            this.currentTrack++;

            if (this.currentTrack >= this.tracks.length) {
                this.player.nextTrack();
                return;
            }
            checkCurrentTrackNull();
        }
    }

    private void checkCurrentTrackNull() {
        if (this.tracks[this.currentTrack] == null || this.tracks[this.currentTrack].getAudioTrack() == null) {
            nextInternal();
        } else {
            this.player.playTrack(this.tracks[this.currentTrack].getAudioTrack());
            updateMessage();
        }
    }

    @Override
    public void onShuffle() {
        this.shuffle = !this.shuffle;
        this.notPlayed = new ArrayList<>();
        if (this.shuffle) {
            for (int i = this.currentTrack + 1; i < this.playlist.getTotal(); i++) {
                this.notPlayed.add(i);
            }
        }
        updateMessage();
    }

    @Override
    public void onQueued() {
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_EMOJI).queue();
    }

    @Override
    public void onPlaying() {
        this.currentTrack--;
        nextInternal();
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.addReaction(GuildHandler.SKIP_EMOJI).queue();
        this.message.addReaction(GuildHandler.SHUFFLE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_ALL_EMOJI).queue();
    }

    @Override
    public void onPlayed() {
        this.currentTrack = this.playlist.getTotal();
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
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.player.removeElement(this);
    }

    @Override
    public void onDeletePlaylist() {
        this.player.nextTrack();
    }
}
