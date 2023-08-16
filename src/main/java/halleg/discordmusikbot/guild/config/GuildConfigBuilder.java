package halleg.discordmusikbot.guild.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import net.dv8tion.jda.api.entities.Guild;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GuildConfigBuilder {
    private Long outputChannelId;
    private String prefix;

    public GuildConfigBuilder() {
        this.outputChannelId = null;
        this.prefix = null;
    }

    public GuildConfig build(Guild g) throws GuildConfigException {
        return new GuildConfig(this.outputChannelId, this.prefix, g);
    }

    @JsonSetter
    private void setChannelId(Long outputChannelId) {
        this.outputChannelId = outputChannelId;
    }

    @JsonSetter
    private void setOutputChannelId(Long outputChannelId) {
        this.outputChannelId = outputChannelId;
    }

    @JsonSetter
    private void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
