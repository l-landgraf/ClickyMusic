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

            if (isShuffled) {
                buttons[2] = MyButton.SHUFFLE_BUTTON.getPrimary();
            } else {
                buttons[2] = MyButton.SHUFFLE_BUTTON.getSecondary();
            }
        }
        buttons[0] = MyButton.REPEAT_BUTTON.getSecondary();
        buttons[1] = MyButton.DELETE_BUTTON.getSecondary();


        return new ActionRow[]{ActionRow.of(buttons)};
    }

    private static ActionRow[] getPlaying(boolean isPaused, boolean isPlaylist, boolean isShuffled) {
        Button[][] buttons = new Button[2][5];
        buttons[0][0] = MyButton.BACK_ALL_BUTTON.getSecondary();
        buttons[0][4] = MyButton.SKIP_ALL_BUTTON.getSecondary();

        buttons[1][0] = MyButton.REPEAT_BUTTON.getSecondary();
        buttons[1][1] = Button.secondary("empty1", " ").asDisabled();
        buttons[1][2] = Button.secondary("empty2", " ").asDisabled();
        buttons[1][3] = Button.secondary("empty3", " ").asDisabled();


        if (isPaused) {
            buttons[0][2] = MyButton.RESUME_BUTTON.getPrimary();
        } else {
            buttons[0][2] = MyButton.PAUSE_BUTTON.getSecondary();
        }

        if (isPlaylist) {
            buttons[0][1] = MyButton.BACK_BUTTON.getSecondary();
            buttons[0][3] = MyButton.SKIP_BUTTON.getSecondary();
            if (isShuffled) {
                buttons[1][4] = MyButton.SHUFFLE_BUTTON.getPrimary();
            } else {
                buttons[1][4] = MyButton.SHUFFLE_BUTTON.getSecondary();
            }
        } else {
            buttons[0][1] = MyButton.BACK_BUTTON.getDisabled();
            buttons[0][3] = MyButton.SKIP_BUTTON.getDisabled();
            buttons[1][4] = MyButton.SHUFFLE_BUTTON.getDisabled();
        }

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

    private interface ButtonBuilder {
        ActionRow[] getButtons(boolean isPaused, boolean isPlaylist, boolean isShuffled);
    }

}
