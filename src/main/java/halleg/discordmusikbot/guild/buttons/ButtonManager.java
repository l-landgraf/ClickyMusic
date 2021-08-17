package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.Player;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ButtonManager {
    private List<Button> buttons;
    private GuildHandler handler;

    public ButtonManager(GuildHandler handler) {
        this.handler = handler;
        this.buttons = new ArrayList<>();

        this.buttons.add(new Button(handler, GuildHandler.REPEAT_EMOJI, true,
                "Queue this Song again.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                String search = this.handler.getBuilder().getURI(message);
                player.join(member.getVoiceState().getChannel());
                this.handler.sendRepeatMessage(search, new Consumer<>() {
                    @Override
                    public void accept(Message message) {
                        //ButtonManager.this.handler.getBuilder().setLoading(message);
                        handler.getLoader().search(search, player, member, message);
                    }
                });

            }
        });

        this.buttons.add(new Button(handler, GuildHandler.REMOVE_EMOJI, true,
                "Remove this Song from the Queue.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                QueueElement ele = player.findElement(message.getIdLong());
                if (ele != null) {
                    ele.onDelete();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.RESUME_PAUSE_EMOJI, true,
                "Resume/Pause the Player.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                QueueElement ele = player.findElement(message.getIdLong());
                if (ele != null) {
                    ele.onResumePause();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.BACK_EMOJI, true,
                "Go back.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                QueueElement ele = player.findElement(message.getIdLong());
                if (ele != null) {
                    ele.onBack();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.SKIP_EMOJI, true,
                "Skip the current Song.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                QueueElement ele = player.findElement(message.getIdLong());
                if (ele != null) {
                    ele.onSkip();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.SHUFFLE_EMOJI, true,
                "Play all remaining Songs in this Playlist in a random order.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                QueueElement ele = player.findElement(message.getIdLong());
                if (ele != null) {
                    ele.onShuffle();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.REMOVE_ALL_EMOJI, true,
                "Skip the entire Playlist.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                QueueElement ele = player.findElement(message.getIdLong());
                if (ele != null) {
                    ele.onDeletePlaylist();
                }
            }
        });

        this.buttons.add(new Button(handler, GuildHandler.RIP, false,
                "Kills the Bot.") {
            @Override
            protected void run(Message message, Player player, MessageReaction react, Member member) {
                System.exit(1);
            }
        });
    }

    public void handleReaction(Message message, MessageReaction react, Member member) {
        if (this.handler.getPlayer(member.getVoiceState().getChannel()) == null) {

        }
        react.retrieveUsers()
             .queue(
                     new Consumer<List<User>>() {
                         @Override
                         public void accept(List<User> users) {
                             for (User r : users) {
                                 if (react.getJDA().getSelfUser() == r || ButtonManager.this.handler.isLinkedBot(r)) {
                                     message.removeReaction(react.getReactionEmote().getEmoji(), member.getUser()).queue();
                                     for (Button but : ButtonManager.this.buttons) {
                                         if (but.check(message, react, member, react.getReactionEmote().getEmoji())) {
                                             return;
                                         }
                                     }
                                 }
                             }
                         }
                     }, null);
    }

    public List<Button> getButtons() {
        return this.buttons;
    }
}
