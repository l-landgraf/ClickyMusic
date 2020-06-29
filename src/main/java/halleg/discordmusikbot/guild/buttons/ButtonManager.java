package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.loader.SingleLoadHandler;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ButtonManager {
    private List<Button> buttons;
    private GuildHandler handler;

    public ButtonManager(GuildHandler handler) {
        this.handler = handler;
        this.buttons = new ArrayList<Button>();

        this.buttons.add(new Button(handler, GuildHandler.REPEAT_EMOJI, false,
                "Queue this Song again.") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                String search = this.handler.getBuilder().getURI(message);
                this.handler.getPlayer().join(member.getVoiceState().getChannel());
                this.handler.sendRepeatMessage(search, new Consumer<Message>() {
                    @Override
                    public void accept(Message message) {
                        ButtonManager.this.handler.getBuilder().setLoading(message);
                        SingleLoadHandler rt = new SingleLoadHandler(ButtonManager.this.handler, search, member, message);
                        rt.load();
                    }
                });

            }
        });

        this.buttons.add(new Button(handler, GuildHandler.REMOVE_EMOJI, true,
                "Remove this Song from the Queue.") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.onDelete();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.RESUME_PAUSE_EMOJI, true,
                "Resume/Pause the Player.") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.onResumePause();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.SKIP_EMOJI, true,
                "Skip the current Song.") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.onSkip();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.SHUFFLE_EMOJI, true,
                "Play all remaining Songs in this Playlist in a random order.") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.onShuffle();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.REMOVE_ALL_EMOJI, true,
                "Skip the entire Playlist.") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.onDeletePlaylist();
                }
            }
        });
    }

    public boolean handleReaction(Message message, MessageReaction react, Member member) {
        for (MessageReaction r : message.getReactions()) {
            if (r.getReactionEmote().getEmoji().equalsIgnoreCase(react.getReactionEmote().getEmoji())) {
                if (!r.isSelf()) {
                    return false;
                } else {
                    break;
                }
            }
        }

        for (Button but : this.buttons) {
            if (but.check(message, react, member, react.getReactionEmote().getEmoji())) {
                return true;
            }
        }
        return false;
    }

    public List<Button> getButtons() {
        return this.buttons;
    }
}
