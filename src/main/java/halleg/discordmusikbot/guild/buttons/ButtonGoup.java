package halleg.discordmusikbot.guild.buttons;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum ButtonGoup {
    QUEUED(
            MyButton.REPEAT_BUTTON.getButton(),
            MyButton.DELETE_BUTTON.getButton()),
    PLAYING_SINGLE(
            MyButton.REPEAT_BUTTON.getButton(),
            MyButton.BACK_BUTTON.getButton(),
            MyButton.PAUSE_BUTTON.getButton(),
            MyButton.SKIP_BUTTON.getButton()),
    PAUSED_SINGLE(
            MyButton.REPEAT_BUTTON.getButton(),
            MyButton.BACK_BUTTON.getButton(),
            MyButton.RESUME_BUTTON.getButton(),
            MyButton.SKIP_BUTTON.getButton()),
    PLAYING_PLAYLIST(
            MyButton.REPEAT_BUTTON.getButton(),
            MyButton.BACK_BUTTON.getButton(),
            MyButton.PAUSE_BUTTON.getButton(),
            MyButton.SKIP_BUTTON.getButton(),
            MyButton.SHUFFLE_BUTTON.getButton(),
            MyButton.REMOVE_ALL_BUTTON.getButton()),
    PAUSED_PLAYLIST(
            MyButton.REPEAT_BUTTON.getButton(),
            MyButton.BACK_BUTTON.getButton(),
            MyButton.RESUME_BUTTON.getButton(),
            MyButton.SKIP_BUTTON.getButton()),
    PLAYED(
            MyButton.REPEAT_BUTTON.getButton());


    private Button[] buttons;

    ButtonGoup(Button... buttons) {
        this.buttons = buttons;
    }

    public Button[] getButtons() {
        return this.buttons;
    }
}
