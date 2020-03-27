package halleg.discordmusikbot.commands;

import java.util.ArrayList;
import java.util.List;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Message;

public class CommandManager {
	private GuildHandler handler;
	private List<Command> commands;

	public CommandManager(GuildHandler handler) {
		this.handler = handler;
		this.commands = new ArrayList<Command>();

		this.commands.add(new Command(handler, "play", false,true,
				"adds a song to the queue. Alternatively you can write the source directly in the specefied channel.",
				"[source]") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().loadAndQueue(args.get(1), message.getMember());
				this.handler.delete(message);
			}
		});

		this.commands.add(new Command(handler, "setchannel", false,false, "sets the channel for this bot.", "[channelid]") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.setChannel(this.handler.getGuild().getTextChannelById(args.get(1)));
			}
		});

		this.commands.add(
				new Command(handler, "setprefix", true, false,"sets the chracters commands have to start with.", "[prefix]") {
					@Override
					protected void run(List<String> args, Message message) {
						this.handler.setPrefix(args.get(1));
					}
				});

		this.commands.add(new Command(handler, "pause", true, true,"pauses the player.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().setPaused(true);
			}
		});

		this.commands.add(new Command(handler, "resume", true, true,"resumes the player.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().setPaused(false);
			}
		});

		this.commands.add(new Command(handler, "skip", true, true,"skips the current track.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().nextTrack();
			}
		});

		this.commands.add(new Command(handler, "leave", true, false,"the bot will leave any voicechannel.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().leave();
			}
		});

		this.commands.add(new Command(handler, "help", false,false, "displays a help message.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.sendHelpMessage(message.getChannel());
			}
		});
	}

	public boolean handleCommand(Message message) {

		for (Command com : this.commands) {
			if (com.check(message)) {
				return true;
			}
		}

		return false;
	}

	public List<Command> getCommands() {
		return this.commands;
	}
}
