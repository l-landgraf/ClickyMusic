package halleg.discordmusikbot.guild.buttons;

import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum MyButton {
    REPEAT_BUTTON("repeat", "🔁", "Queue this Song again", (event, player) -> {
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

    SKIP_BUTTON("skip", "⏩", "Skip this song", (ele) -> ele.onSkip()),

    DELETE_BUTTON("delete", "❌", "Remove this song from the Queue", (ele) -> ele.onDelete()),

    PAUSE_BUTTON("pause", "⏸", "Pause the Player", (ele) -> ele.onResumePause()),

    RESUME_BUTTON("resume", "▶", "Resume the Player", (ele) -> ele.onResumePause()),

    BACK_BUTTON("back", "⏪", "Play Song from the beginning", (ele) -> ele.onBack()),

    REMOVE_ALL_BUTTON("remove_all", "Skip the entire Playlist", "❎", (ele) -> ele.onDeletePlaylist()),

    SHUFFLE_BUTTON("shuffle", "🔀", "Play all remaining Songs in this Playlist in a random order",
            (ele) -> ele.onShuffle());

    private Button button;
    private String id;
    private String emoji;
    private ButtonExecutor exe;

    MyButton(String id, String emoji, String description, SimpleExecutor executor) {
        this.id = id;
        this.emoji = emoji;
        this.exe = executor;
    }

    MyButton(String id, String emoji, String description, ButtonExecutor executor) {
        this.id = id;
        this.emoji = emoji;
        this.exe = executor;
    }

    public Button getButton() {
        return Button.secondary(this.id, this.emoji);
    }

    public String getId() {
        return this.id;
    }

    public String getEmoji() {
        return this.emoji;
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
            QueueElement ele = player.findElement(event.getIdLong());
            if (ele == null) {
                return;
            }
            execute(ele);
        }

        void execute(QueueElement ele);
    }
}
