package halleg.discordmusikbot.commands;

import java.util.Arrays;
import java.util.List;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Message;

public abstract class Command {
	protected GuildHandler handler;
	protected String command;
	protected int atibNum;
	protected String description;
	protected String[] tips;
	protected boolean channelOnly;

	public Command(GuildHandler handler, String command, boolean channelOnly, String description, String... tips) {
		this.handler = handler;
		this.command = command;
		this.atibNum = tips.length;
		this.channelOnly = channelOnly;
		this.description = description;
		this.tips = tips;
	}

	protected abstract void run(List<String> args, Message message);

	public boolean check(Message message) {
		if (this.channelOnly && message.getChannel().getIdLong() != this.handler.getChannel().getIdLong()) {
			return false;
		}

		List<String> args = Arrays.asList(message.getContentRaw().split(" "));

		if ((args.size() - 1) != this.atibNum) {
			return false;
		}

		if (args.get(0).equalsIgnoreCase(this.handler.getPrefix() + this.command)) {
			this.handler.log("executing command: " + this.command);
			run(args, message);
			return true;
		}
		return false;
	}

	public GuildHandler getHandler() {
		return this.handler;
	}

	public String getCommand() {
		return this.command;
	}

	public String getTip() {
		String ret = this.handler.getPrefix() + this.command;
		for (String string : this.tips) {
			ret += " " + string;
		}
		return ret;
	}

	public int getAtibNum() {
		return this.atibNum;
	}

	public String getDescription() {
		return this.description;
	}
}
