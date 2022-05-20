package halleg.discordmusikbot.guild;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GuildConfig implements Serializable {
    private long channelid;
    private String prefix;

    public GuildConfig() {
        this.channelid = 0;
        this.prefix = ".";
    }

    public GuildConfig(GuildHandler handler) {
        this.channelid = handler.getChannel().getIdLong();
        this.prefix = handler.getPrefix();
    }

    public GuildConfig(long channelid, String prefix, Map<Long, Long> linkedBots) {
        this.channelid = channelid;
        this.prefix = prefix;
    }

    @JsonSetter
    public void setChannelId(long channelid) {
        this.channelid = channelid;
    }

    @JsonSetter
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonGetter
    public long getChannelId() {
        return this.channelid;
    }

    @JsonGetter
    public String getPrefix() {
        return this.prefix;
    }
}
