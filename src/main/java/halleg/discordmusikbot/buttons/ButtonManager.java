package halleg.discordmusikbot.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.ArrayList;
import java.util.List;

public class ButtonManager {
    private List<Button> buttons;
    private GuildHandler handler;

    public ButtonManager(GuildHandler handler) {
        this.handler = handler;
        this.buttons = new ArrayList<Button>();

        this.buttons.add(new Button(handler, GuildHandler.REPEAT_EMOJI, "queue song again") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                this.handler.getPlayer().loadAndQueue(this.handler.getBuilder().getURI(message), member);
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.REMOVE_EMOJI, "remove from queue") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.reactDelete(member);
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.RESUME_PAUSE_EMOJI, "resume/pause player") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.reactResumePause(member);
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.SKIP_EMOJI, "skip current song") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.reactSkip(member);
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.REMOVE_ALL_EMOJI, "remove all unplayed songs from this playlist from queue") {
            @Override
            protected void run(Message message, MessageReaction react, Member member) {
                QueueElement ele = this.handler.getPlayer().findElement(message.getIdLong());
                if (ele != null) {
                    ele.reactDeletePlaylist(member);
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
