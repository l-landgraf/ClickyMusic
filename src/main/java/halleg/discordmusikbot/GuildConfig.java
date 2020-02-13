package halleg.discordmusikbot;

import java.io.Serializable;

import halleg.discordmusikbot.guild.GuildHandler;

public class GuildConfig implements Serializable {
	private long guildid;
	private long channelid;
	private String prefix;

	public GuildConfig(GuildHandler handler) {
		this.guildid = handler.getGuild().getIdLong();
		this.channelid = handler.getChannel().getIdLong();
		this.prefix = handler.getPrefix();
	}

	public long getGuildid() {
		return this.guildid;
	}

	public long getChannelid() {
		return this.channelid;
	}

	public String getPrefix() {
		return this.prefix;
	}
}
