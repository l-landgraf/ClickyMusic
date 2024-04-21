package halleg.discordmusikbot.guild;

import halleg.discordmusikbot.guild.buttons.MyButton;
import halleg.discordmusikbot.guild.commands.MyCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class MessageFactory {


    private GuildHandler handler;

    public MessageFactory(GuildHandler handler) {
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

    public MessageCreateData buildCommandHelpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("How to Use:");
        eb.setDescription("To start a new track simply write in the " + (
                this.handler.getChannel()).getAsMention() + " channel.\n"
                + "there are also commands available:");

        eb.addField("", "**Commands:**", false);
        for (MyCommand command : MyCommand.values()) {
            String args = "";
            for (OptionData data : command.getOptions()) {
                if (data.isRequired()) {
                    args += " <" + data.getName() + ">";
                } else {
                    args += " [<" + data.getName() + ">]";
                }
            }
            eb.addField(command.getCommand() + args, command.getDescription(), false);
        }


        eb.addField("", "Github: [https://github.com/mrhalleg/ClickyMusic](https://github.com/mrhalleg/ClickyMusic)",
                false);

        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.addEmbeds(eb.build());
        return mcb.build();
    }

    public MessageCreateData buildButtonHelpMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("How to Use:");
        eb.setDescription("You can click the Buttons to perform actions.");
        eb.addField("", "**Buttons:**", false);

        for (MyButton buttton : MyButton.values()) {
            eb.addField(buttton.getEmoji().getFormatted(), buttton.getDescription(), false);
        }
        eb.addField("", "Github: [https://github.com/mrhalleg/ClickyMusic](https://github.com/mrhalleg/ClickyMusic)",
                false);

        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.addEmbeds(eb.build());
        return mcb.build();
    }

    public MessageCreateData buildInfoMessage(String message) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🔔 Info");
        eb.setDescription(message);

        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.addEmbeds(eb.build());
        return mcb.build();
    }

    public MessageCreateData buildErrorMessage(String error) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("💀 Error");
        eb.setDescription(error);

        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.addEmbeds(eb.build());
        return mcb.build();
    }

    public MessageCreateData buildListMessage(String[] directories, String[] files) {
        EmbedBuilder eb = new EmbedBuilder();
        buildListEmbed("Directories", directories, eb);
        buildListEmbed("Files", files, eb);

        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.addEmbeds(eb.build());
        return mcb.build();
    }

    public MessageEmbed buildListEmbed(String title, String[] elements, EmbedBuilder eb) {
        eb.addField(title, "", false);
        if (elements == null || elements.length == 0) {
            eb.addField("none", "", false);
            return eb.build();
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

        return eb.build();
    }

    public MessageCreateData errorReply(String s) {
        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.setContent(GuildHandler.LOADING_FAILED_EMOJI + " " + s);
        return mcb.build();
    }

    public MessageCreateData successReply(String s) {
        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.setContent(GuildHandler.CONFIRMED + " " + s);
        return mcb.build();
    }

    public static String inlineCodeBlock(String message) {
        return "`" + message + "`";
    }

    public void setLoading(Message message) {
        this.handler.addReaction(message, GuildHandler.LOADING_EMOJI);
    }

    public void setLoadingFailed(Message message) {
        this.handler.addReaction(message, GuildHandler.LOADING_FAILED_EMOJI);
    }

    public MessageCreateData buildRepeatMessage(String link) {
        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.setContent(MyButton.REPEAT_BUTTON.getEmoji().getFormatted() + MessageFactory.inlineCodeBlock(link));
        return mcb.build();
    }

    public void setUnknownCommand(Message message) {
        this.handler.addReaction(message, GuildHandler.UNKNOWN_COMMAND);
    }

    public void setConfirmed(Message message) {
        this.handler.addReaction(message, GuildHandler.CONFIRMED);
    }

    public MessageCreateData buildReplyMessage(String message) {
        MessageCreateBuilder mcb = new MessageCreateBuilder();
        mcb.setContent(message);
        return mcb.build();
    }
}
