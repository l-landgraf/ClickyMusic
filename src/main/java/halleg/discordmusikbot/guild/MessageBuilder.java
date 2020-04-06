package halleg.discordmusikbot.guild;

import halleg.discordmusikbot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MessageBuilder {
    private GuildHandler handler;

    public MessageBuilder(GuildHandler handler) {
        this.handler = handler;
    }


    private String toTime(long length) {
        String sec = Long.toString((length / 1000l) % 60l);
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        String min = Long.toString((length / 60000));
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        return min + ":" + sec;
    }

    public MessageEmbed buildNewErrorMessage(String error) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("💀 Error");
        eb.setDescription(error);

        return eb.build();
    }

    public String getURI(Message message) {
        try {
            return message.getEmbeds().get(0).getUrl();
        } catch (Exception e) {
            return "";
        }

    }

    public MessageEmbed buildHelpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Avilable Commands");
        eb.setDescription("All commands have to start with `" + this.handler.getPrefix()
                + "` and most must be in this channel.\n" + "To start a new track simply write in this channel.\n"
                + "You can also click the reactions to perform actions.");

        for (Command command : this.handler.getCommands().getCommands()) {
            eb.addField(command.getTip(), command.getDescription(), false);
        }

        return eb.build();
    }

    public MessageEmbed buildInfoMessage(String message) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🔔 Info");
        eb.setDescription(message);

        return eb.build();
    }
}
