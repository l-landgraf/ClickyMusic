package halleg.discordmusikbot.guild.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@JsonIgnoreType
public class GuildConfig {
    @JsonIgnore
    private Guild guild;
    @JsonIgnore
    private MessageChannel outputChannel;
    @JsonIgnore
    private String prefix;

    public GuildConfig(Long channelid, String prefix, Guild guild) throws GuildConfigException {
        this.guild = guild;

        if (channelid != null) {
            this.outputChannel = guild.getTextChannelById(channelid);
        } else if (guild.getDefaultChannel() != null) {
            this.outputChannel = guild.getDefaultChannel().asTextChannel();
        } else if (guild.getTextChannels().size() > 0) {
            this.outputChannel = guild.getTextChannels().get(0);
        } else {
            throw new GuildConfigException("No text channels visible");
        }

        if (prefix == null) {
            this.prefix = ".";
        } else {
            this.prefix = prefix;
        }
    }

    public Guild getGuild() {
        return this.guild;
    }

    public void setOutputChannel(MessageChannel outputChannel) {
        this.outputChannel = outputChannel;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public MessageChannel getOutputChannel() {
        return this.outputChannel;
    }

    @JsonGetter
    public long getGuildId() {
        return this.guild.getIdLong();
    }

    @JsonGetter
    public long getOutputChannelId() {
        return this.outputChannel.getIdLong();
    }

    @JsonGetter
    public String getPrefix() {
        return this.prefix;
    }
}
