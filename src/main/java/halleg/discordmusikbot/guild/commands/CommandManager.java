package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandManager {
	public static final String SEEK_TIPPS = "Incorrect Syntax, seek examples:\n'+10' - skips forward 10sec\n'+1:2:30' - skips forward 1h 2min 30sec\n'-20' - skips backward 20sec\n'5:30' - skips to 5min 30sec";
	private GuildHandler handler;
	private List<Command> commands;

	public CommandManager(GuildHandler handler) {
		this.handler = handler;
		this.commands = new ArrayList<>();

		this.commands.add(new Command(handler, "queue", false, true, false,
				true, false, "adds a song to the queue. Alternatively you can write the source directly in the specefied channel.",
				"*source*") {
			@Override
			protected void run(List<String> args, Message message) {
				if (args.size() <= 1) {
					this.handler.sendErrorMessage("Command ussage: " + getTip());
					return;
				}

				String search = "";
				for (int i = 1; i < args.size(); i++) {
					search += " " + args.get(i);
				}

				search = search.trim();

				this.handler.getBuilder().setLoading(message);
				this.handler.getPlayer().join(message.getMember().getVoiceState().getChannel());
				this.handler.getLoader().search(search, message.getMember(), message);
			}
		});

		this.commands.add(new Command(handler, "play", true, true, true,
				false, true, "plays the playlist song specefied by the number",
				"*songNr*") {
			@Override
			protected void run(List<String> args, Message message) {
				QueueElement ele = this.handler.getPlayer().getCurrentElement();

				int i;
				try {
					i = Integer.parseInt(args.get(1));
				} catch (NumberFormatException e) {
					this.handler.sendErrorMessage("Argument has to be a Number.");
					return;
				}
				if (ele == null) {
					this.handler.sendErrorMessage("Im not playing anything currently.");
					return;
				}
				ele.runPlay(i);
			}
		});

		this.commands.add(new Command(handler, "join", true, true, false,
				false, true, "the bot will join your voicechannel.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().join(message.getMember().getVoiceState().getChannel());
			}
		});

		this.commands.add(new Command(handler, "setchannel", false, false, false,
				false, true, "sets the channel for this bot.", "*channelid*") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.setChannel(this.handler.getGuild().getTextChannelById(args.get(1)));
			}
		});

		this.commands.add(new Command(handler, "setprefix", false, false, false,
				false, true, "sets the chracters commands have to start with.", "*prefix*") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.setPrefix(args.get(1));
			}
		});

		this.commands.add(new Command(handler, "pause", true, true, true,
				false, true, "pauses the player.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().setPaused(true);
			}
		});

		this.commands.add(new Command(handler, "resume", true, true, true,
				false, true, "resumes the player.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().setPaused(false);
			}
		});

		this.commands.add(new Command(handler, "skip", true, true, true,
				false, true, "skips the current track.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().nextTrack();
			}
		});

		this.commands.add(new Command(handler, "jump", true, true, true,
				false, true, "skips the current track.", "*Nr*") {
			@Override
			protected void run(List<String> args, Message message) {
				int nr;
				try {
					nr = Integer.parseInt(args.get(1));
				} catch (NumberFormatException e) {
					this.handler.sendErrorMessage("Nr is not a Number.");
					return;
				}
				if (nr < 1) {
					this.handler.sendErrorMessage("Nr cant be less than One.");
					return;
				}
				if (nr > this.handler.getPlayer().queueSize()) {
					this.handler.sendErrorMessage("Nr cant be more than queue length.");
					return;
				}
				this.handler.getPlayer().jump(nr);
			}
		});

		this.commands.add(new Command(handler, "seek", true, true, true,
				false, true, "seeks to the desired possition or skips forward the given amount of time.", "*[sign][[hours:]minutes:]seconds*") {
			@Override
			protected void run(List<String> args, Message message) {
				CommandManager.this.parseSeek(args, message);
			}
		});

		this.commands.add(new Command(handler, "time", true, true, true,
				false, true, "displays the time of the current Song.") {
			@Override
			protected void run(List<String> args, Message message) {
				if (!this.handler.getPlayer().isPlaying()) {
					this.handler.sendErrorMessage("No track currently playing.");
					return;
				}

				long milliseconds = this.handler.getPlayer().getPosition();

				int seconds = (int) (milliseconds / 1000) % 60;
				int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
				int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

				this.handler.sendInfoMessage("Current Track Position: " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));

			}
		});

		this.commands.add(new Command(handler, "disconnect", true, true, true,
				false, true, "the bot will disconnect from any voicechannel.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().setPaused(true);
				this.handler.getPlayer().leave();
			}
		});

		this.commands.add(new Command(handler, "leave", true, true, true,
				false, true, "the bot will leave any voicechannel and completly clear its Queue.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().leave();
			}
		});

		this.commands.add(new Command(handler, "clear", true, true, false,
				false, true, "clears the queue and the currently playling track.") {
			@Override
			protected void run(List<String> args, Message message) {
				this.handler.getPlayer().clearQueue();
			}
		});

		this.commands.add(new Command(handler, "help", false, false, false,
				false, true, "displays a help message.") {
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
		this.handler.deleteLater(message);
		this.handler.getBuilder().setUnknownCommand(message);
		return false;
	}

	public List<Command> getCommands() {
		return this.commands;
	}

	private void parseSeek(List<String> args, Message message) {

		String arg = args.get(1);
		arg = arg.trim();
		String sign = null;
		if (arg.startsWith("+")) {
			sign = "+";
			arg = arg.substring(1);
		}
		if (arg.startsWith("-")) {
			sign = "-";
			arg = arg.substring(1);
		}

		String[] times = arg.split(":");
		Collections.reverse(Arrays.asList(times));

		if (times.length == 0) {
			this.handler.sendErrorMessage(SEEK_TIPPS);
			return;
		} else if (times.length > 3) {
			this.handler.sendErrorMessage(SEEK_TIPPS);
			return;
		}
		int seconds = 0;
		int minutes = 0;
		int hours = 0;
		try {
			seconds = Integer.parseInt(times[0]);
			if (times.length > 1) {
				minutes = Integer.parseInt(times[1]);
				if (times.length > 2) {
					hours = Integer.parseInt(times[2]);
				}
			}
		} catch (NumberFormatException e) {
			this.handler.sendErrorMessage(SEEK_TIPPS);
			return;
		}

		if (seconds < 0 || minutes < 0 || hours < 0) {
			this.handler.sendErrorMessage(SEEK_TIPPS);
			return;
		}

		long time = seconds * 1000 + minutes * 60000 + hours * 3600000;

		if (sign == null) {
			this.handler.getPlayer().seekTo(time);
		} else if (sign.equals("+")) {
			this.handler.getPlayer().seekAdd(time);
		} else if (sign.equals("-")) {
			this.handler.getPlayer().seekAdd(-time);
		}
	}
}
