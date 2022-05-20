package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;

public abstract class Command {
    protected GuildHandler handler;
    protected String command;
    protected int atibNum;
    protected String description;
    protected String[] tips;
    protected boolean textChannelOnly;
    protected boolean voiceChannelOnly;
    protected boolean connectedOnly;
    protected boolean unlimitedArguments;
    private final boolean deleteLater;

    public Command(GuildHandler handler, String command, boolean textChannelOnly, boolean voiceChannelOnly, boolean connectedOnly, boolean unlimitedArguments, boolean deleteLater, String description, String... tips) {
        this.handler = handler;
        this.command = command;
        this.atibNum = tips.length;
        this.textChannelOnly = textChannelOnly;
        this.voiceChannelOnly = voiceChannelOnly;
        this.connectedOnly = connectedOnly;
        this.unlimitedArguments = unlimitedArguments;
        this.deleteLater = deleteLater;
        this.description = description;
        this.tips = tips;
    }

    protected abstract void run(List<String> args, QueuePlayer player, Message message);

    public boolean check(Message message) {
        if (this.textChannelOnly && message.getChannel().getIdLong() != this.handler.getChannel().getIdLong()) {
            return false;
        }


        List<String> args = Arrays.asList(message.getContentRaw().split(" "));
        if (!args.get(0).equalsIgnoreCase(this.handler.getPrefix() + this.command)) {
            return false;
        }


        QueuePlayer player = this.handler.getPlayer(message.getMember().getVoiceState().getChannel());
        if (this.voiceChannelOnly && player == null) {
            this.handler.sendErrorMessage("Im bussy in a diffrent Voicechanel.");
            return false;
        }

        if (!this.unlimitedArguments) {
            if ((args.size() - 1) != this.atibNum) {
                this.handler.sendErrorMessage("Command ussage: " + getTip());
                return false;
            }
        }

        if (this.deleteLater) {
            this.handler.deleteLater(message);
        }

        this.handler.log("executing command: " + this.command);
        run(args, player, message);
        return true;

    }

    public GuildHandler getHandler() {
        return this.handler;
    }

    public String getCommand() {
        return this.command;
    }

    public String getTip() {
        String ret = this.handler.getPrefix() + this.command;
        for (String string : this.tips) {
            ret += " " + string;
        }
        return ret;
    }

    public int getAtibNum() {
        return this.atibNum;
    }

    public String getDescription() {
        return this.description;
    }
}
