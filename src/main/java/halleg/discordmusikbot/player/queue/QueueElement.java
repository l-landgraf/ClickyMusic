package halleg.discordmusikbot.player.queue;

import halleg.discordmusikbot.player.Player;
import net.dv8tion.jda.api.EmbedBuilder;
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

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message m) {
        this.message = m;
    }


    public abstract void onQueued();

    public abstract void onPlaying();

    public abstract void onPlayed();

    public abstract void onResumePause();

    public abstract void onEnded();

    public abstract void onSkip();

    public abstract void onDelete();

    public abstract void onShuffle();

    public abstract void onDeletePlaylist();

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
