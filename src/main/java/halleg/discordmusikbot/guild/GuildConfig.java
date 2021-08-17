package halleg.discordmusikbot.guild;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GuildConfig implements Serializable {
    private long channelid;
    private String prefix;
    private Map<Long, Long> linkedBots;

    public GuildConfig() {
        this.channelid = 0;
        this.prefix = ".";
        this.linkedBots = new HashMap<>();
    }

    public GuildConfig(GuildHandler handler) {
        this.channelid = handler.getChannel().getIdLong();
        this.prefix = handler.getPrefix();
        this.linkedBots = handler.getLinkedBots();
    }

    public GuildConfig(long channelid, String prefix, Map<Long, Long> linkedBots) {
        this.channelid = channelid;
        this.prefix = prefix;
        this.linkedBots = linkedBots;
    }

    @JsonSetter
    public void setChannelId(long channelid) {
        this.channelid = channelid;
    }

    @JsonSetter
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonSetter
    public void setLinkedBots(Map<Long, Long> linkedBots) {
        this.linkedBots = linkedBots;
    }

    @JsonGetter
    public long getChannelId() {
        return this.channelid;
    }

    @JsonGetter
    public String getPrefix() {
        return this.prefix;
    }

    @JsonGetter
    public Map<Long, Long> getLinkedBots() {
        return this.linkedBots;
    }
}
