package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.MessageFactory;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandManager {
    public static final String SEEK_TIPPS = "Incorrect Syntax, seek examples:\n'+10' - skips forward 10sec\n'+1:2:30'" +
            " - skips forward 1h 2min 30sec\n'-20' - skips backward 20sec\n'5:30' - skips to 5min 30sec";
    private GuildHandler handler;
    private List<Command> commands;

    public CommandManager(GuildHandler handler) {
        this.handler = handler;
        this.commands = new ArrayList<>();

        this.commands.add(new Command(handler, "queue", "adds a song to the queue. Alternatively you can write the " +
                "source directly in the specefied channel.",
                new OptionData(OptionType.STRING, "source", "source", true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {

                try {
                    player.join(event.getMember().getVoiceState().getChannel());
                } catch (InsufficientPermissionException e) {
                    this.getHandler().handleMissingPermission(e);
                    return null;
                }

                String search = event.getOption("source").getAsString();

                search = search.trim();

                this.getHandler().getLoader().search(search, player, event.getMember(), null);
                return this.getHandler().getBuilder().successReply("Searching for " + MessageFactory.inlineCodeBlock(search));
            }
        });

        this.commands.add(new Command(handler, "play", "plays the playlist song specefied by the number",
                new OptionData(OptionType.INTEGER, "songnr", "songnr", true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                QueueElement ele = player.getCurrentElement();

                int i;
                try {
                    i = event.getOption("songnr").getAsInt();
                } catch (NumberFormatException e) {
                    return this.getHandler().getBuilder().errorReply("Argument has to be a Number.");
                }
                if (ele == null) {
                    return this.getHandler().getBuilder().errorReply("Im not playing anything currently.");
                }
                try {

                    ele.runPlay(i);
                } catch (Exception e) {
                    return this.getHandler().getBuilder().errorReply(e.getMessage());
                }
                return this.getHandler().getBuilder().successReply("Now playing track " + i);
            }
        });

        this.commands.add(new Command(handler, "join", "the bot will join your voicechannel.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                if (event.getMember().getVoiceState().getChannel() == null) {
                    return getHandler().getBuilder().errorReply("You are not in a Voice channel");
                }
                try {
                    player.join(event.getMember().getVoiceState().getChannel());
                } catch (InsufficientPermissionException e) {
                    this.getHandler().handleMissingPermission(e);
                    return this.getHandler().getBuilder().successReply(GuildHandler.LOADING_FAILED_EMOJI);
                }

                return this.getHandler().getBuilder().successReply("Joined channel");
            }
        });

        this.commands.add(new Command(handler, "setchannel", "sets the channel for this bot.",
                new OptionData(OptionType.CHANNEL, "channelid", "channelid", false).setChannelTypes(ChannelType.TEXT)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                OptionMapping id = event.getOption("channelid");
                TextChannel channel = null;
                if (id == null) {
                    channel = (TextChannel) event.getChannel();
                } else {
                    channel = id.getAsTextChannel();
                }

                if (channel == null) {
                    return this.getHandler().getBuilder().errorReply("invlaid channel");
                }
                this.getHandler().setChannel(channel);

                return this.getHandler().getBuilder().successReply("Set preffered channel to " + channel.getAsMention());
            }
        });

        this.commands.add(new Command(handler, "pause", "pauses the player.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                if (player.isPaused()) {
                    return this.getHandler().getBuilder().errorReply("Player is already paused");
                }
                player.setPaused(true);
                return this.getHandler().getBuilder().successReply("Paused Player");
            }
        });

        this.commands.add(new Command(handler, "resume", "resumes the player.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                if (!player.isPaused()) {
                    return this.getHandler().getBuilder().errorReply("Player is not paused");
                }
                player.setPaused(false);
                return this.getHandler().getBuilder().successReply("Resumed Player");
            }
        });

        this.commands.add(new Command(handler, "skip", "skips the current track.", new OptionData(OptionType.INTEGER,
                "amount", "amount", false)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                OptionMapping amount = event.getOption("amount");
                if (amount == null) {
                    player.nextTrack();
                    return this.getHandler().getBuilder().successReply("Skipped track");
                }

                int nr = amount.getAsInt();
                if (nr < 1) {
                    return this.getHandler().getBuilder().errorReply("amount cant be less than One.");
                }
                if (nr > player.queueSize()) {
                    return this.getHandler().getBuilder().errorReply("amount cant be more than queue length" +
                            ".");
                }
                player.jump(nr);
                return this.getHandler().getBuilder().successReply("Skipped " + nr + " tracks");
            }
        });

        this.commands.add(new Command(handler, "seek", "seeks to the desired possition or skips forward the given " +
                "amount of time.", new OptionData(OptionType.STRING, "time", "[sign][[hours:]minutes:]seconds", true)) {

            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                return CommandManager.this.parseSeek(event.getOption("time").getAsString());
            }
        });

        this.commands.add(new Command(handler, "time", "displays the time of the current Song.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                if (!player.isPlaying()) {
                    return getHandler().getBuilder().errorReply("No track currently playing");
                }

                long milliseconds = player.getPosition();

                int seconds = (int) (milliseconds / 1000) % 60;
                int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
                int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

                return this.getHandler().getBuilder().successReply("Current Track Position: " + String.format("%02d",
                        hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }
        });

        this.commands.add(new Command(handler, "leave", "the bot will leave any voicechannel and completly clear its " +
                "Queue.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                player.leave();
                player.clearQueue();
                return this.getHandler().getBuilder().successReply("Left channel");
            }
        });

        this.commands.add(new Command(handler, "clear", "clears the queue and the currently playling track.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                player.clearQueue();
                return this.getHandler().getBuilder().successReply("Cleared queue");
            }
        });

        this.commands.add(new Command(handler, "tree", "displays all local files.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                return this.getHandler().getBuilder().successReply("```" + getHandler().getFileManager().showTree() + "```");
            }
        });

        this.commands.add(new Command(handler, "copy", "copies a file or folder.", new OptionData(OptionType.STRING,
                "from", "from", true), new OptionData(OptionType.STRING, "to", "to", true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {

                return this.getHandler().getFileManager().copyFile(event.getOption("from").getAsString(),
                        event.getOption(
                                "to").getAsString());
            }
        });

        this.commands.add(new Command(handler, "move", "moves a file or folder. can also be used for renaming.",
                new OptionData(OptionType.STRING, "from", "from", true), new OptionData(OptionType.STRING, "to", "to"
                , true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {

                return this.getHandler().getFileManager().moveFile(event.getOption("from").getAsString(),
                        event.getOption(
                                "to").getAsString());
            }
        });

        this.commands.add(new Command(handler, "delete", "deletes a file or folder.",
                new OptionData(OptionType.STRING, "directory", "directory", true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                return this.getHandler().getFileManager().deleteFile(event.getOption("directory").getAsString());
            }
        });

        this.commands.add(new Command(handler, "deleteall", "deletes a directory and all of its contents.",
                new OptionData(OptionType.STRING, "directory", "directory", true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                return this.getHandler().getFileManager().deleteDirectoryRecursively(event.getOption("directory").getAsString());
            }
        });

        this.commands.add(new Command(handler, "list", "lists the files of the folder.",
                new OptionData(OptionType.STRING, "directory", "directory", true)) {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                return this.getHandler().getFileManager().listFiles(event.getOption("directory").getAsString());
            }
        });

        this.commands.add(new Command(handler, "ripclicky", "terminates the bot and hopefully restarts it.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                //message.addReaction(GuildHandler.RIP).queue();
                return this.getHandler().getBuilder().errorReply("");
            }
        });

        this.commands.add(new Command(handler, "help", "displays a help message.") {
            @Override
            protected Message run(SlashCommandInteractionEvent event, QueuePlayer player) {
                Message m = new MessageBuilder("test").build();
                event.getChannel().sendMessage(m).setActionRows(ActionRow.of(Button.primary(
                        "test", "test"))).queue(me -> me.editMessage(m).setActionRow(Button.secondary("t2", "t2")).queue());
                return this.getHandler().getBuilder().errorReply("");
            }
        });

        CommandListUpdateAction list = handler.getGuild().updateCommands();
        for (Command c : this.commands) {
            CommandDataImpl data = new CommandDataImpl(c.getCommand(), c.getDescription());
            data.addOptions(c.getOptions());
            list.addCommands(data);
        }
        list.queue();
    }

    public boolean handleCommand(SlashCommandInteractionEvent event) {

        for (Command com : this.commands) {
            if (com.check(event)) {
                return true;
            }
        }
        return false;
    }

    public List<Command> getCommands() {
        return this.commands;
    }

    private Message parseSeek(String arg) {

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
            return this.handler.getBuilder().errorReply(SEEK_TIPPS);
        } else if (times.length > 3) {
            return this.handler.getBuilder().errorReply(SEEK_TIPPS);
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
            return this.handler.getBuilder().errorReply(SEEK_TIPPS);
        }

        if (seconds < 0 || minutes < 0 || hours < 0) {
            return this.handler.getBuilder().errorReply(SEEK_TIPPS);
        }

        long time = seconds * 1000 + minutes * 60000 + hours * 3600000;
        try {
            if (sign == null) {
                this.handler.getPlayer().seekTo(time);
                return this.handler.getBuilder().successReply("Sought to " + hours + "h " + minutes + "m " + seconds + "s");
            } else if (sign.equals("+")) {
                this.handler.getPlayer().seekAdd(time);
                return this.handler.getBuilder().successReply("Sought forward " + hours + "h " + minutes + "m " + seconds + "s");
            } else if (sign.equals("-")) {
                this.handler.getPlayer().seekAdd(-time);
                return this.handler.getBuilder().successReply("Sought backward " + hours + "h " + minutes + "m " + seconds + "s");
            }
        } catch (Exception e) {
            return this.handler.getBuilder().errorReply(e.getMessage());
        }
        return null;
    }
}
