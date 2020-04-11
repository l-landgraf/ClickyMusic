package halleg.discordmusikbot.player.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.function.Consumer;

public abstract class QueueElement {
    protected Player player;
    protected Message message;

    public QueueElement(Player player) {
        this.player = player;
    }

    public abstract MessageEmbed buildMessage();

    public abstract boolean isPlayable();

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message m) {
        this.message = m;
    }

    public abstract AudioTrack getTrack();

    public void setQueued() {
        setWaitingEmojis();
    }

    public void setPlaying() {
        setPlayingEmojis();
    }

    public void setPlayed() {
        setDoneEmojis();
    }

    public void setSkiped(Member member) {
        setDoneEmojis();
        //setFooter("skiped by " + member.getEffectiveName());
    }

    public void setRemoved(Member member) {
        setDoneEmojis();
        //setFooter("removed by " + member.getEffectiveName());
    }

    public void setPaused(Member member) {
        //setFooter("paused by " + member.getEffectiveName());
    }

    public void setUnpaused(Member member) {
        //setFooter("");
    }


    protected void setWaitingEmojis() {
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.REMOVE_EMOJI).queue();
    }

    protected void setPlayingEmojis() {
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
        this.message.addReaction(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.addReaction(GuildHandler.SKIP_EMOJI).queue();
    }

    protected void setDoneEmojis() {
        this.message.clearReactions(GuildHandler.SKIP_EMOJI).queue();
        this.message.clearReactions(GuildHandler.RESUME_PAUSE_EMOJI).queue();
        this.message.clearReactions(GuildHandler.REMOVE_EMOJI).queue();
        this.message.clearReactions(GuildHandler.REMOVE_ALL_EMOJI).queue();
        this.message.addReaction(GuildHandler.REPEAT_EMOJI).queue();
    }

    public void addError(String message) {
        if (this.message.getEmbeds().get(0).getFooter() == null) {
            setFooter(message);
        } else {
            setFooter(this.message.getEmbeds().get(0).getFooter() + "\n" + message);
        }
        setColor(Color.RED);
    }


    protected String toTime(long length) {
        String sec = Long.toString((length / 1000l) % 60l);
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        String min = Long.toString((length / 60000));
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        return min + ":" + sec;
    }

    public void reactResumePause(Member member) {
        this.player.togglePaused(member);
    }

    public void reactSkip(Member member) {
        this.player.nextTrack(member);
    }

    public void reactDelete(Member member) {
        this.player.removeElement(this, member);
    }

    public void reactDeletePlaylist(Member member) {
    }

    protected void setFooter(String footer) {
        EmbedBuilder eb = new EmbedBuilder(this.message.getEmbeds().get(0));
        eb.setFooter(footer);
        this.message.editMessage(eb.build()).queue(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                QueueElement.this.message = message;
            }
        });
    }

    protected void setColor(Color color) {
        EmbedBuilder eb = new EmbedBuilder(this.message.getEmbeds().get(0));
        eb.setColor(color);
        this.message.editMessage(eb.build()).queue(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                QueueElement.this.message = message;
            }
        });
    }
}
