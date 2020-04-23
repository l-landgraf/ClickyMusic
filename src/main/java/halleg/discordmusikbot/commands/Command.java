package halleg.discordmusikbot.commands;

import halleg.discordmusikbot.guild.GuildHandler;
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

    public Command(GuildHandler handler, String command, boolean textChannelOnly, boolean voiceChannelOnly, boolean connectedOnly, String description, String... tips) {
        this.handler = handler;
        this.command = command;
        this.atibNum = tips.length;
        this.textChannelOnly = textChannelOnly;
        this.voiceChannelOnly = voiceChannelOnly;
        this.connectedOnly = connectedOnly;
        this.description = description;
        this.tips = tips;
    }

    protected abstract void run(List<String> args, Message message);

    public boolean check(Message message) {
        if (this.textChannelOnly && message.getChannel().getIdLong() != this.handler.getChannel().getIdLong()) {
            return false;
        }


        List<String> args = Arrays.asList(message.getContentRaw().split(" "));
        if (args.get(0).equalsIgnoreCase(this.handler.getPrefix() + this.command)) {
            if (this.voiceChannelOnly) {
                if (message.getMember().getVoiceState().getChannel() == null) {
                    this.handler.sendErrorMessage("Cant find your Voicechannel.");
                    return false;
                }

                if (this.handler.getPlayer().getConnectedChannel() != null &&
                        message.getMember().getVoiceState().getChannel() != this.handler.getPlayer().getConnectedChannel()) {
                    this.handler.sendErrorMessage("Im bussy in a diffrent Voicechanel.");
                    return false;
                }
            }

            if (this.connectedOnly && this.handler.getPlayer().getConnectedChannel() == null) {
                this.handler.sendErrorMessage("Im not connected to any Voicechanel.");
                return false;
            }

            if ((args.size() - 1) != this.atibNum) {
                this.handler.sendErrorMessage("Command ussage: " + getTip());
                return false;
            }
            this.handler.log("executing command: " + this.command);
            run(args, message);
            return true;
        }
        return false;
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
