package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.loader.SingleLoadHandler;
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
        this.commands = new ArrayList<Command>();

        this.commands.add(new Command(handler, "queue", false, true, false,
                true, "adds a song to the queue. Alternatively you can write the source directly in the specefied channel.",
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
                this.handler.getBuilder().setLoading(message);
                this.handler.getPlayer().join(message.getMember().getVoiceState().getChannel());
                SingleLoadHandler rt = new SingleLoadHandler(this.handler, search, message.getMember(), message);
                rt.load();
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
                false, "sets the channel for this bot.", "*channelid*") {
            @Override
            protected void run(List<String> args, Message message) {
                this.handler.setChannel(this.handler.getGuild().getTextChannelById(args.get(1)));
            }
        });

        this.commands.add(new Command(handler, "setprefix", false, false, false,
                false, "sets the chracters commands have to start with.", "*prefix*") {
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

        this.commands.add(new Command(handler, "seek", true, true, true,
                true, "seeks to the desired possition or skips forward the given amount of time.", "*[sign][[hours:]minutes:]seconds*") {
            @Override
            protected void run(List<String> args, Message message) {
                CommandManager.this.parseSeek(args, message);
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
        handler.getBuilder().setUnknownCommand(message);
        return false;
    }

    public List<Command> getCommands() {
        return this.commands;
    }

    private void parseSeek(List<String> args, Message message) {
        if(args.size() == 1){
            if(!handler.getPlayer().isPlaying()){
                handler.sendErrorMessage("No track currently playing.");
                return;
            }

            long milliseconds=handler.getPlayer().getPosition();

            int seconds = (int) (milliseconds / 1000) % 60 ;
            int minutes = (int) ((milliseconds / (1000*60)) % 60);
            int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

            handler.sendInfoMessage("Current Track Position: "+String.format("%02d", hours)+":"+String.format("%02d", minutes)+":"+String.format("%02d", seconds));
            return;
        }

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

        System.out.println(arg);

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
