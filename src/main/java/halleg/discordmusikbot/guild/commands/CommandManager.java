package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.MessageFactory;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandManager {

    private GuildHandler handler;

    public CommandManager(GuildHandler handler) {
        this.handler = handler;
        CommandListUpdateAction list = this.handler.getGuild().updateCommands();
        for (MyCommand c : MyCommand.values()) {
            CommandDataImpl data = new CommandDataImpl(c.getCommand(), c.getDescription());
            data.addOptions(c.getOptions());
            list.addCommands(data);
        }
        list.queue();
    }

    public boolean handleCommand(SlashCommandInteractionEvent event) {

        for (MyCommand c : MyCommand.values()) {
            if (c.check(event, handler)) {
                return true;
            }
        }
        return false;
    }
}
