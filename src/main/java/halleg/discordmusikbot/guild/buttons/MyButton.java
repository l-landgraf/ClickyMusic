package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.commands.CommandType;
import halleg.discordmusikbot.guild.commands.MyCommand;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.function.Consumer;

public enum MyButton {
    REPEAT_BUTTON("repeat", "<:replay:979022469342068736>", "Queue this Song again", CommandType.FREE,
            new ButtonExecutor() {
                @Override
                public void execute(ButtonInteractionEvent event, QueuePlayer player) {
                    String search = player.getHandler().getBuilder().getURI(event.getMessage());

                    if (!player.join(event.getMember().getVoiceState().getChannel())) {
                        return;
                    }


                    player.getHandler().queue(player.getHandler().getChannel().sendMessage(player.getHandler().getBuilder().buildRepeatMessage(search)),
                            new Consumer<Message>() {
                                @Override
                                public void accept(Message message) {
                                    player.getHandler().getLoader().search(search, player, event.getMember(),
                                            message);
                                }
                            });
                }
            }),


    NEXT_BUTTON("next", "<:next:979022469308497940>", "Skip this song", CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onNext();
                }
            }),

    SKIP_BUTTON("skip", "<:forward:979022469174280242>", "Skip the entire Playlist",
            CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onSkip();
                }
            }),

    DELETE_BUTTON("delete", "<:cancel:979022468649988137>", "Remove this song from the Queue",
            CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onRemoved();
                }
            }),

    PAUSE_BUTTON("pause", "<:pause:979022469081997334>", "Pause the Player", CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onResumePause();
                }
            }),

    RESUME_BUTTON("resume", "<:playbutton:979022469195268106>", "Resume the Player",
            CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onResumePause();
                }
            }),

    PREV_BUTTON("previous", "<:previous:979022469123952711>", "Play previous Song",
            CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onPrevious();
                }
            }),

    BACK_BUTTON("back", "<:backwards:979420915664322620>", "Play Song from the beginning",
            CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onBack();
                }
            }),


    SHUFFLE_BUTTON("shuffle", "<:shuffle:979024135449628722>", "Play all remaining Songs in this " +
            "Playlist in a random" +
            " order", CommandType.SAME_CHANNEL,
            new SimpleExecutor() {
                @Override
                public void execute(QueueElement ele) {
                    ele.onShuffle();
                }
            });

    private String id;
    private Emoji emoji;
    private ButtonExecutor exe;
    private String description;
    private CommandType type;

    MyButton(String id, String emoji, String description, CommandType type, ButtonExecutor executor) {
        this.id = id;
        this.emoji = Emoji.fromFormatted(emoji);
        this.exe = executor;
        this.description = description;
        this.type = type;
    }

    public Button getSecondary() {
        return Button.secondary(this.id, this.emoji);
    }

    public Button getPrimary() {
        return Button.primary(this.id, this.emoji);
    }

    public Button getDanger() {
        return Button.danger(this.id, this.emoji);
    }

    public Button getSuccess() {
        return Button.success(this.id, this.emoji);
    }


    public Button getDisabledSecondary() {
        return Button.secondary(this.id, this.emoji).asDisabled();
    }

    public Button getDisabledPrimary() {
        return Button.primary(this.id, this.emoji).asDisabled();
    }

    public Button getDisabledDanger() {
        return Button.danger(this.id, this.emoji).asDisabled();
    }

    public Button getDisabledSuccess() {
        return Button.success(this.id, this.emoji).asDisabled();
    }

    public String getId() {
        return this.id;
    }

    public Emoji getEmoji() {
        return this.emoji;
    }

    public String getDescription() {
        return this.description;
    }

    public CommandType getType() {
        return this.type;
    }

    public void execute(ButtonInteractionEvent event, QueuePlayer player) {
        this.exe.execute(event, player);
    }

    private interface ButtonExecutor {
        void execute(ButtonInteractionEvent event, QueuePlayer player);
    }

    private interface SimpleExecutor extends ButtonExecutor {
        @Override
        default void execute(ButtonInteractionEvent event, QueuePlayer player) {
            QueueElement ele = player.findElement(event.getMessage().getIdLong());
            if (ele == null) {
                return;
            }
            execute(ele);
        }

        void execute(QueueElement ele);
    }
}
