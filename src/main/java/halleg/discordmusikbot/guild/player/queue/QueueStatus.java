package halleg.discordmusikbot.guild.player.queue;

import halleg.discordmusikbot.guild.buttons.MyButton;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum QueueStatus {
    QUEUED(true, (isPaused, isPlaylist, isShuffled) -> {
        return QueueStatus.getQueued(isPaused, isPlaylist, isShuffled);
    }),

    PLAYING(true, (isPaused, isPlaylist, isShuffled) -> {
        return QueueStatus.getPlaying(isPaused, isPlaylist, isShuffled);
    }),

    PLAYED(false, (isPaused, isPlaylist, isShuffled) -> {
        return QueueStatus.getPlayed(isPaused, isPlaylist, isShuffled);
    }),

    REMOVED(false, (isPaused, isPlaylist, isShuffled) -> {
        return QueueStatus.getPlayed(isPaused, isPlaylist, isShuffled);
    }),

    SKIPPED(false, (isPaused, isPlaylist, isShuffled) -> {
        return QueueStatus.getPlayed(isPaused, isPlaylist, isShuffled);
    });

    private boolean keepLoading;
    private ButtonBuilder builder;

    QueueStatus(boolean keepLoading, ButtonBuilder builder) {
        this.keepLoading = keepLoading;
        this.builder = builder;
    }

    public boolean getKeepLoading() {
        return this.keepLoading;
    }

    public ActionRow[] getButtons(boolean isPaused, boolean isPlaylist, boolean isShuffled) {
        return this.builder.getButtons(isPaused, isPlaylist, isShuffled);
    }


    private static ActionRow[] getQueued(boolean isPaused, boolean isPlaylist, boolean isShuffled) {
        Button[] buttons = new Button[2];
        if (isPlaylist) {
            buttons = new Button[3];

            buttons[2] = shuffleButton(isShuffled);
        }
        buttons[0] = MyButton.REPEAT_BUTTON.getSecondary();
        buttons[1] = MyButton.DELETE_BUTTON.getSecondary();


        return new ActionRow[]{ActionRow.of(buttons)};
    }

    private static ActionRow[] getPlaying(boolean isPaused, boolean isPlaylist, boolean isShuffled) {
        Button[][] buttons = new Button[2][5];

        buttons[0][0] = MyButton.BACK_BUTTON.getPrimary();
        buttons[0][1] = disableButton(MyButton.PREV_BUTTON.getPrimary(), isPlaylist);
        buttons[0][2] = playButton(isPaused);
        buttons[0][3] = disableButton(MyButton.NEXT_BUTTON.getPrimary(), isPlaylist);
        buttons[0][4] = MyButton.SKIP_BUTTON.getPrimary();

        buttons[1][0] = MyButton.REPEAT_BUTTON.getSecondary();
        buttons[1][1] = Button.secondary("empty1", "-").asDisabled();
        buttons[1][2] = Button.secondary("empty2", "-").asDisabled();
        buttons[1][3] = Button.secondary("empty3", "-").asDisabled();
        buttons[1][4] = disableButton(shuffleButton(isShuffled), isPlaylist);


        ActionRow[] rows = new ActionRow[2];
        for (int i = 0; i < buttons.length; i++) {
            rows[i] = ActionRow.of(buttons[i]);
        }

        return rows;
    }


    private static ActionRow[] getPlayed(boolean isPaused, boolean isPlaylist, boolean isShuffled) {
        Button[] buttons = new Button[1];
        buttons[0] = MyButton.REPEAT_BUTTON.getSecondary();
        return new ActionRow[]{ActionRow.of(buttons)};
    }

    private static Button disableButton(Button button, boolean enabled) {
        if (enabled) {
            return button;
        } else {
            return button.asDisabled();
        }
    }

    private static Button playButton(boolean isPaused) {
        return toggleButton(isPaused, MyButton.RESUME_BUTTON.getSecondary(), MyButton.PAUSE_BUTTON.getPrimary());
    }

    private static Button shuffleButton(boolean isShuffled) {
        return toggleButton(isShuffled, MyButton.SHUFFLE_BUTTON.getPrimary(), MyButton.SHUFFLE_BUTTON.getSecondary());
    }

    private static Button toggleButton(boolean enabled, Button primary, Button secondary) {
        if (enabled) {
            return primary;
        } else {
            return secondary;
        }
    }

    private interface ButtonBuilder {
        ActionRow[] getButtons(boolean isPaused, boolean isPlaylist, boolean isShuffled);
    }
}
