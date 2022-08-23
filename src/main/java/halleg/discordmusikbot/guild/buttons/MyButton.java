package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum MyButton {
    REPEAT_BUTTON("repeat", "<:replay:979022469342068736>", "Queue this Song again", (event, player) -> {
        String search = player.getHandler().getBuilder().getURI(event.getMessage());
        try {
            player.join(event.getMember().getVoiceState().getChannel());
        } catch (InsufficientPermissionException e) {
            player.getHandler().handleMissingPermission(e);
            return;
        }
        player.getHandler().sendRepeatMessage(search, message1 -> {
            player.getHandler().getLoader().search(search, player, event.getMember(), message1);
        });
    }),

    NEXT_BUTTON("next", "<:next:979022469308497940>", "Skip this song", (ele) -> ele.onNext()),

    SKIP_BUTTON("skip", "<:forward:979022469174280242>", "Skip the entire Playlist",
            (ele) -> ele.onSkip()),

    DELETE_BUTTON("delete", "<:cancel:979022468649988137>", "Remove this song from the Queue",
            (ele) -> ele.onRemoved()),

    PAUSE_BUTTON("pause", "<:pause:979022469081997334>", "Pause the Player", (ele) -> ele.onResumePause()),

    RESUME_BUTTON("resume", "<:playbutton:979022469195268106>", "Resume the Player", (ele) -> ele.onResumePause()),

    PREV_BUTTON("previous", "<:previous:979022469123952711>", "Play previous Song",
            (ele) -> ele.onPrevious()),

    BACK_BUTTON("back", "<:backwards:979420915664322620>", "Play Song from the beginning",
            (ele) -> ele.onBack()),


    SHUFFLE_BUTTON("shuffle", "<:shuffle:979024135449628722>", "Play all remaining Songs in this Playlist in a random" +
            " order",
            (ele) -> ele.onShuffle());

    private String id;
    private Emoji emoji;
    private ButtonExecutor exe;
    private String description;

    MyButton(String id, String emoji, String description, SimpleExecutor executor) {
        this.id = id;
        this.emoji = Emoji.fromMarkdown(emoji);
        this.exe = executor;
        this.description = description;
    }

    MyButton(String id, String emoji, String description, ButtonExecutor executor) {
        this.id = id;
        this.emoji = Emoji.fromMarkdown(emoji);
        this.exe = executor;
        this.description = description;
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
        return description;
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
