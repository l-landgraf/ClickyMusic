package halleg.discordmusikbot.guild;

import halleg.discordmusikbot.guild.buttons.Button;
import halleg.discordmusikbot.guild.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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

    public Message buildNewErrorMessage(String error) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("💀 Error");
        eb.setDescription(error);

        return new net.dv8tion.jda.api.MessageBuilder(eb.build()).build();
    }

    public String getURI(Message message) {
        try {
            String uri = message.getEmbeds().get(0).getUrl();
            if (uri == null) {
                uri = message.getEmbeds().get(0).getFooter().getText();
            }
            return uri;
        } catch (Exception e) {
            return "";
        }

    }

    public Message buildHelpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("How to Use:");
        eb.setDescription("All commands have to start with `" + this.handler.getPrefix()
                + "` and most must be in this channel.\n" + "To start a new track simply write in the " + ((TextChannel) this.handler.getChannel()).getAsMention() + " channel.\n"
                + "You can also click the reactions to perform actions.");

        eb.addField("", "**Commands:**", false);
        for (Command command : this.handler.getCommands().getCommands()) {
            eb.addField(command.getTip(), command.getDescription(), false);
        }

        eb.addField("", "**Buttons:**", false);

        for (Button buttton : this.handler.getButtons().getButtons()) {
            eb.addField(buttton.getEmoji(), buttton.getDescription(), false);
        }

        eb.addField("", "Github: [https://github.com/mrhalleg/ClickyMusic](https://github.com/mrhalleg/ClickyMusic)", false);

        return new net.dv8tion.jda.api.MessageBuilder(eb.build()).build();
    }

    public Message buildInfoMessage(String message) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🔔 Info");
        eb.setDescription(message);

        return new net.dv8tion.jda.api.MessageBuilder(eb.build()).build();
    }

    public Message buildListMessage(String title, String[] elements) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        if (elements == null || elements.length == 0) {
            eb.setDescription("none");
            return new net.dv8tion.jda.api.MessageBuilder(eb.build()).build();
        }

        String[] batch = new String[6];
        for (int e = 0; e < batch.length; e++) {
            batch[e] = "";
        }
        for (int i = 0; i < elements.length; i++) {
            batch[i % 6] = elements[i];
            if (i % 6 == 5) {
                eb.addField(batch[0], batch[3], true);
                eb.addField(batch[1], batch[4], true);
                eb.addField(batch[2], batch[5], true);
                for (int e = 0; e < batch.length; e++) {
                    batch[e] = "";
                }
            }
        }

        eb.addField(batch[0], batch[3], true);
        eb.addField(batch[1], batch[4], true);
        eb.addField(batch[2], batch[5], true);

        return new net.dv8tion.jda.api.MessageBuilder(eb.build()).build();
    }

    public void setLoading(Message message) {
        this.handler.addReaction(message, GuildHandler.LOADING_EMOJI);
    }

    public void setLoadingFailed(Message message) {
        this.handler.addReaction(message, GuildHandler.LOADING_EMOJI);
        this.handler.addReaction(message, GuildHandler.LOADING_FAILED_EMOJI);
    }

    public Message buildRepeatMessage(String link) {
        net.dv8tion.jda.api.MessageBuilder mb = new net.dv8tion.jda.api.MessageBuilder();

        mb.append(GuildHandler.REPEAT_EMOJI);
        mb.appendCodeLine(link);

        return mb.build();
    }

    public void setUnknownCommand(Message message) {
        this.handler.addReaction(message, GuildHandler.UNKNOWN_COMMAND);
    }

    public void setConfirmed(Message message) {
        this.handler.addReaction(message, GuildHandler.CONFIRMED);
    }
}
