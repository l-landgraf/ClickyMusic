package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class Command {
    private GuildHandler handler;
    private String command;
    private String description;
    private OptionData[] options;
    private boolean voiceChannelOnly;
    private boolean connectedOnly;

    public Command(GuildHandler handler, String command, String description, OptionData... options) {
        this.handler = handler;
        this.command = command;
        this.description = description;
        this.options = options;
    }

    protected abstract Message run(SlashCommandInteractionEvent event, QueuePlayer player);

    public boolean check(SlashCommandInteractionEvent event) {
        if (!event.getInteraction().getName().equals(this.command)) {
            return false;
        }

        this.handler.log("executing command: " + this.command);
        Message m = run(event, getHandler().getPlayer());
        this.handler.queueAndDeleteLater(event.reply(m));


        return true;

    }

    public GuildHandler getHandler() {
        return this.handler;
    }

    public OptionData[] getOptions() {
        return this.options;
    }

    public String getCommand() {
        return this.command;
    }

    public String getDescription() {
        return this.description;
    }
}
