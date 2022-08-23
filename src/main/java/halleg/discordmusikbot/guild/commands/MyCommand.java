package halleg.discordmusikbot.guild.commands;

import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.MessageFactory;
import halleg.discordmusikbot.guild.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public enum MyCommand {
    
    QUEUE("adds a song to the queue. Alternatively you can write the " +
            "source directly in the specefied channel.",
            new OptionData(OptionType.STRING, "source", "source", true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {

            try {
                handler.getPlayer().join(event.getMember().getVoiceState().getChannel());
            } catch (InsufficientPermissionException e) {
                handler.handleMissingPermission(e);
                return null;
            }

            String search = event.getOption("source").getAsString();

            search = search.trim();

            handler.getLoader().search(search, handler.getPlayer(), event.getMember(), null);
            return handler.getBuilder().successReply("Searching for " + MessageFactory.inlineCodeBlock(search));
        }
    },

        PLAY( "plays the playlist song specefied by the number",
            new OptionData(OptionType.INTEGER, "songnr", "songnr", true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            QueueElement ele = handler.getPlayer().getCurrentElement();

            int i;
            try {
                i = event.getOption("songnr").getAsInt();
            } catch (NumberFormatException e) {
                return handler.getBuilder().errorReply("Argument has to be a Number.");
            }
            if (ele == null) {
                return handler.getBuilder().errorReply("Im not playing anything currently.");
            }
            try {

                ele.runPlay(i);
            } catch (Exception e) {
                return handler.getBuilder().errorReply(e.getMessage());
            }
            return handler.getBuilder().successReply("Now playing track " + i);
        }
    },

        JOIN( "the bot will join your voicechannel.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            if (event.getMember().getVoiceState().getChannel() == null) {
                return handler.getBuilder().errorReply("You are not in a Voice channel");
            }
            try {
                handler.getPlayer().join(event.getMember().getVoiceState().getChannel());
            } catch (InsufficientPermissionException e) {
                handler.handleMissingPermission(e);
                return handler.getBuilder().successReply(GuildHandler.LOADING_FAILED_EMOJI);
            }

            return handler.getBuilder().successReply("Joined channel");
        }
    },

       SETCHANNEL( "sets the channel for this bot.",
            new OptionData(OptionType.CHANNEL, "channelid", "channelid", false).setChannelTypes(ChannelType.TEXT)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            OptionMapping id = event.getOption("channelid");
            TextChannel channel = null;
            if (id == null) {
                channel = (TextChannel) event.getChannel();
            } else {
                channel = id.getAsTextChannel();
            }

            if (channel == null) {
                return handler.getBuilder().errorReply("invlaid channel");
            }
            handler.setChannel(channel);

            return handler.getBuilder().successReply("Set preffered channel to " + channel.getAsMention());
        }
    },

       PAUSE( "pauses the player.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            if (handler.getPlayer().isPaused()) {
                return handler.getBuilder().errorReply("Player is already paused");
            }
            handler.getPlayer().setPaused(true);
            return handler.getBuilder().successReply("Paused Player");
        }
    },

       RESUME( "resumes the player.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            if (!handler.getPlayer().isPaused()) {
                return handler.getBuilder().errorReply("Player is not paused");
            }
            handler.getPlayer().setPaused(false);
            return handler.getBuilder().successReply("Resumed Player");
        }
    },

        SKIP( "skips the current track.", new OptionData(OptionType.INTEGER,
                "amount", "amount", false)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            OptionMapping amount = event.getOption("amount");
            if (amount == null) {
                handler.getPlayer().nextTrack();
                return handler.getBuilder().successReply("Skipped track");
            }

            int nr = amount.getAsInt();
            if (nr < 1) {
                return handler.getBuilder().errorReply("amount cant be less than One.");
            }
            if (nr > handler.getPlayer().queueSize()) {
                return handler.getBuilder().errorReply("amount cant be more than queue length" +
                        ".");
            }
            handler.getPlayer().jump(nr);
            return handler.getBuilder().successReply("Skipped " + nr + " tracks");
        }
    },

       NExT( "plays the next track in the playlist.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            QueueElement ele = handler.getPlayer().getCurrentElement();
            if (ele == null) {
                return handler.getBuilder().errorReply("Im not playing anything currently.");
            }
            if (!ele.isPlaylist()) {
                return handler.getBuilder().errorReply("Current element is not a playlist.");
            }

            ele.onNext();
            return handler.getBuilder().successReply("Playing next track");
        }
    },

       PREV( "plays the previous track in the playlist.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            QueueElement ele = handler.getPlayer().getCurrentElement();
            if (ele == null) {
                return handler.getBuilder().errorReply("Im not playing anything currently.");
            }
            if (!ele.isPlaylist()) {
                return handler.getBuilder().errorReply("Current element is not a playlist.");
            }

            ele.onPrevious();
            return handler.getBuilder().successReply("Playing previous track");
        }
    },

       BACK( "Restarts the track.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            QueueElement ele = handler.getPlayer().getCurrentElement();
            if (ele == null) {
                return handler.getBuilder().errorReply("Im not playing anything currently.");
            }
            if (!ele.isPlaylist()) {
                return handler.getBuilder().errorReply("Current element is not a playlist.");
            }

            ele.onBack();
            return handler.getBuilder().successReply("Restarted track.");
        }
    },

        SHUFFLE( "Shuffles / unshuffles the queue.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            QueueElement ele = handler.getPlayer().getCurrentElement();
            if (ele == null) {
                return handler.getBuilder().errorReply("Im not playing anything currently.");
            }
            if (!ele.isPlaylist()) {
                return handler.getBuilder().errorReply("Current element is not a playlist.");
            }

            ele.onShuffle();
            if (ele.isShuffle()) {
                return handler.getBuilder().successReply("Shuffled the playlist.");
            } else {
                return handler.getBuilder().successReply("Unshuffled the playlist.");
            }
        }
    },

        SEEK("seeks to the desired possition or skips forward the given" +
            " " +
            "amount of time.", new OptionData(OptionType.STRING, "time", "[sign][[hours" +
                                              ":]minutes:]seconds", true)) {

            public static final String SEEK_TIPPS = "Incorrect Syntax, seek examples:\n'+10' - skips forward 10sec\n'+1:2:30'" +
                    " - skips forward 1h 2min 30sec\n'-20' - skips backward 20sec\n'5:30' - skips to 5min 30sec";

        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            return parseSeek(event.getOption("time").getAsString(), handler);
        }
            private Message parseSeek(String arg, GuildHandler handler) {

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
                    return handler.getBuilder().errorReply(SEEK_TIPPS);
                } else if (times.length > 3) {
                    return handler.getBuilder().errorReply(SEEK_TIPPS);
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
                    return handler.getBuilder().errorReply(SEEK_TIPPS);
                }

                if (seconds < 0 || minutes < 0 || hours < 0) {
                    return handler.getBuilder().errorReply(SEEK_TIPPS);
                }

                long time = seconds * 1000 + minutes * 60000 + hours * 3600000;
                try {
                    if (sign == null) {
                        handler.getPlayer().seekTo(time);
                        return handler.getBuilder().successReply("Sought to " + hours + "h " + minutes + "m " + seconds + "s");
                    } else if (sign.equals("+")) {
                        handler.getPlayer().seekAdd(time);
                        return handler.getBuilder().successReply("Sought forward " + hours + "h " + minutes + "m " + seconds + "s");
                    } else if (sign.equals("-")) {
                        handler.getPlayer().seekAdd(-time);
                        return handler.getBuilder().successReply("Sought backward " + hours + "h " + minutes + "m " + seconds + "s");
                    }
                } catch (Exception e) {
                    return handler.getBuilder().errorReply(e.getMessage());
                }
                return null;
            }
    },

        TIME( "displays the time of the current Song.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            if (!handler.getPlayer().isPlaying()) {
                return handler.getBuilder().errorReply("No track currently playing");
            }

            long milliseconds = handler.getPlayer().getPosition();

            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

            return handler.getBuilder().successReply("Current Track Position:" +
                    " " + String.format("%02d",
                    hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
        }
    },

       LEAVE( "the bot will leave any voicechannel and completly clear " +
            "its " +
            "Queue.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            handler.getPlayer().leave();
            handler.getPlayer().clearQueue();
            return handler.getBuilder().successReply("Left channel");
        }
    },

       CLEAR( "clears the queue and the currently playling track.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            handler.getPlayer().clearQueue();
            return handler.getBuilder().successReply("Cleared queue");
        }
    },

       TREE( "displays all local files.") {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            return handler.getBuilder().successReply("```" + handler.getFileManager().showTree() +
                    "```");
        }
    },

       COPY( "copies a file or folder.", new OptionData(OptionType.STRING,
                "from", "from", true), new

    OptionData(OptionType.STRING, "to", "to", true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {

            return handler.getFileManager().copyFile(event.getOption("from").getAsString(),
                    event.getOption(
                            "to").getAsString());
        }
    },

        MOVE( "moves a file or folder. can also be used for renaming.",
            new OptionData(OptionType.STRING, "from", "from", true), new

    OptionData(OptionType.STRING, "to", "to"
            , true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {

            return handler.getFileManager().moveFile(event.getOption("from").getAsString(),
                    event.getOption(
                            "to").getAsString());
        }
    },

       DELETE( "deletes a file or folder.",
            new OptionData(OptionType.STRING, "directory", "directory", true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            return handler.getFileManager().deleteFile(event.getOption(
                    "directory").getAsString());
        }
    },

       DELETE_ALL( "deletes a directory and all of its contents.",
            new OptionData(OptionType.STRING, "directory", "directory", true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            return handler.getFileManager().deleteDirectoryRecursively(event.getOption("directory").getAsString());
        }
    },

       LIST( "lists the files of the folder.",
            new OptionData(OptionType.STRING, "directory", "directory", true)) {
        @Override
        protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
            return handler.getFileManager().listFiles(event.getOption(
                    "directory").getAsString());
        }
    },

           HELP( "Displays a help message"
                         ) {
               @Override
               protected Message run(SlashCommandInteractionEvent event, GuildHandler handler) {
                   handler.queue(event.getChannel().sendMessage(handler.getBuilder().buildCommandHelpMessage()));
                   handler.queue(event.getChannel().sendMessage(handler.getBuilder().buildButtonHelpMessage()));
                   return handler.getBuilder().successReply("");
               }
    };


    private String description;
    private OptionData[] options;

     MyCommand(String description, OptionData... options) {
        this.description = description;
        this.options = options;
    }

    protected abstract Message run(SlashCommandInteractionEvent event, GuildHandler handler);

    public boolean check(SlashCommandInteractionEvent event, GuildHandler handler) {
        if (!event.getInteraction().getName().equalsIgnoreCase(getCommand())) {
            return false;
        }

        handler.log("executing command: " + getCommand());
        Message m = run(event, handler);
        handler.queueAndDeleteLater(event.reply(m));


        return true;

    }

    public OptionData[] getOptions() {
        return this.options;
    }

    public String getCommand() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getDescription() {
        return this.description;
    }
}
