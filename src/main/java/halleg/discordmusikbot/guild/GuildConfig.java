package halleg.discordmusikbot.guild;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

public class GuildConfig implements Serializable {
    private long guildid;
    private long channelid;
    private String prefix;

    public GuildConfig(){

    }

    public GuildConfig(GuildHandler handler) {
        this.guildid = handler.getGuild().getIdLong();
        this.channelid = handler.getChannel().getIdLong();
        this.prefix = handler.getPrefix();
    }

    public GuildConfig(long guildid, long channelid, String prefix) {
        this.guildid = guildid;
        this.channelid = channelid;
        this.prefix = prefix;
    }

    @JsonSetter
    public void setGuildid(long guildid) {
        this.guildid = guildid;
    }

    @JsonSetter
    public void setChannelid(long channelid) {
        this.channelid = channelid;
    }

    @JsonSetter
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonGetter
    public long getGuildid() {
        return this.guildid;
    }

    @JsonGetter
    public long getChannelid() {
        return this.channelid;
    }

    @JsonGetter
    public String getPrefix() {
        return this.prefix;
    }
}
