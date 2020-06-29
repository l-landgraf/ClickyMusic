package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.loader.SingleLoadHandler;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private GuildHandler handler;
    private List<Command> commands;

    public CommandManager(GuildHandler handler) {
        this.handler = handler;
        this.commands = new ArrayList<Command>();

        this.commands.add(new Command(handler, "queue", false, true, false,
                true, "adds a song to the queue. Alternatively you can write the source directly in the specefied channel.",
                "[source]") {
            @Override
            protected void run(List<String> args, Message message) {
                String search = "";
                for (int i = 1; i < args.size(); i++) {
                    search += " " + args.get(i);
                }
                this.handler.getPlayer().join(message.getMember().getVoiceState().getChannel());
                SingleLoadHandler rt = new SingleLoadHandler(this.handler, search, message.getMember(), message);
                rt.load();
                this.handler.delete(message);
            }
        });

        this.commands.add(new Command(handler, "join", true, true, false,
                false, "the bot will join your voicechannel.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().join(message.getMember().getVoiceState().getChannel());
            }
        });

        this.commands.add(new Command(handler, "setchannel", false, false, false,
                false, "sets the channel for this bot.", "[channelid]") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.setChannel(this.handler.getGuild().getTextChannelById(args.get(1)));
            }
        });

        this.commands.add(
                new Command(handler, "setprefix", false, false, false,
                        false, "sets the chracters commands have to start with.", "[prefix]") {
                    @Override
                    protected void run(List<String> args, Message message) {
                        this.handler.setPrefix(args.get(1));
                    }
                });

        this.commands.add(new Command(handler, "pause", true, true, true,
                false, "pauses the player.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().setPaused(true);
            }
        });

        this.commands.add(new Command(handler, "resume", true, true, true,
                false, "resumes the player.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().setPaused(false);
            }
        });

        this.commands.add(new Command(handler, "skip", true, true, true,
                false, "skips the current track.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().nextTrack();
            }
        });

        this.commands.add(new Command(handler, "disconnect", true, true, true,
                false, "the bot will disconnect from any voicechannel.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().setPaused(true);
                this.handler.getPlayer().leave();
            }
        });

        this.commands.add(new Command(handler, "leave", true, true, true,
                false, "the bot will leave any voicechannel and completly clear its Queue.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().leave();
            }
        });

        this.commands.add(new Command(handler, "clear", true, true, false,
                false, "clears the queue and the currently playling track.") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.getPlayer().clearQueue();
            }
        });

        this.commands.add(new Command(handler, "exit", true, false, false,
                false, "terminates the bot (and hopefully restarts it).") {
            @Override
            protected void run(List<String> args, Message message) {
                if (message.getGuild().getIdLong() == 283386616314068992l || message.getGuild().getIdLong() == 506930046481465355l) {
                    System.exit(0);
                } else {
                    this.handler.sendErrorMessage("Not supportet on this Server.");
                }
            }
        });

        this.commands.add(new Command(handler, "help", false, false, false,
                false, "displays a help message.") {
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
