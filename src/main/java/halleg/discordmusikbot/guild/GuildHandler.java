package halleg.discordmusikbot.guild;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import halleg.discordmusikbot.MusicBot;
import halleg.discordmusikbot.guild.buttons.ButtonManager;
import halleg.discordmusikbot.guild.commands.CommandManager;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GuildHandler {

    public static final String REMOVE_EMOJI = "❌";
    public static final String RESUME_PAUSE_EMOJI = "⏯";
    public static final String REPEAT_EMOJI = "🔁";
    public static final String SKIP_EMOJI = "⏩";
    public static final String BACK_EMOJI = "⏪";
    public static final String REMOVE_ALL_EMOJI = "❎";
    public static final String SHUFFLE_EMOJI = "\uD83D\uDD00";
    public static final String LOADING_EMOJI = "\uD83D\uDD0D";
    public static final String LOADING_FAILED_EMOJI = "⚡";
    public static final String UNKNOWN_COMMAND = "❓";
    public static final String CONFIRMED = "✅";
    public static final String BUSY = "\uD83D\uDD34";
    public static final String NOT_BUSY = "\uD83D\uDFE2";
    public static final String RIP = "\uD83D\uDC80";

    public static final int PLAYLIST_PREVIEW_MAX = 3;
    public static final int PRELOAD_MAX = 5;
    public static final int RETRY_AMOUNT = 5;
    private static final long DELETE_DELAY = 60;

    private Guild guild;
    private TextChannel output;
    private String prefix;

    private QueuePlayer player;
    private MusicBot bot;
    private MessageBuilder builder;
    private CommandManager commands;
    private ButtonManager buttons;
    private TrackLoader loader;

    public GuildHandler(MusicBot musicbot, Guild g) {
        this(musicbot, new GuildConfig(g.getTextChannels().get(0).getIdLong(), ".", new HashMap<>()), g);
    }

    public GuildHandler(MusicBot musicbot, GuildConfig config, Guild g) {
        this.guild = g;
        this.prefix = config.getPrefix();
        this.output = this.guild.getTextChannelById(config.getChannelId());

        this.bot = musicbot;
        this.builder = new MessageBuilder(this);
        this.player = new QueuePlayer(this);
        this.commands = new CommandManager(this);
        this.buttons = new ButtonManager(this);
        this.loader = new TrackLoader(this, musicbot.getPreloader());

        if (this.output == null) {
            setChannel(this.guild.getTextChannels().get(0));
        } else {
            //clearLastMessages(this.output);
        }
        saveConfig();
        log("initialized! outputchannel: " + this.output.getName() + " prefix: " + this.prefix);
    }

    public void setChannel(TextChannel channel) {
        this.output = channel;
        log("set channel to: " + channel.getName());
        sendInfoMessage("This is now the prefered channel.");
        saveConfig();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        sendInfoMessage("Commands for this bot now have to start with `" + this.prefix + "`.");
        saveConfig();
    }

    public void saveConfig() {
        this.bot.saveGuildHandler(this);
    }

    public void handleMessage(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(this.prefix)) {
            this.commands.handleCommand(event.getMessage());
        } else {
            QueuePlayer player = getPlayer(event.getMember().getVoiceState().getChannel());
            if (player == null) {
                return;
            }

            if (event.getChannel() != this.output) {
                return;
            }

            player.join(event.getMember().getVoiceState().getChannel());
            this.builder.setLoading(event.getMessage());
            this.loader.search(event.getMessage().getContentRaw(), player, event.getMember(), event.getMessage());
        }
    }

    public void handleReaction(MessageReaction react, Message message, Member member) {
        GuildHandler.this.buttons.handleReaction(message, react, member);
    }

    public void voiceUpdate() {
        this.player.voiceUpdate();
    }

    public void sendErrorMessage(String error) {
        queue(this.builder.buildNewErrorMessage(error), new Consumer<>() {
            @Override
            public void accept(Message message) {
                deleteLater(message);
            }
        });
    }

    public void sendInfoMessage(String message) {
        queue(this.builder.buildInfoMessage(message), new Consumer<>() {
            @Override
            public void accept(Message message) {
                deleteLater(message);
            }
        });
    }

    public void sendHelpMessage(MessageChannel channel) {
        queue(channel, this.builder.buildHelpMessage());
    }

    public void sendRepeatMessage(String link, Consumer<Message> c) {
        queue(this.output, this.builder.buildRepeatMessage(link), c);
    }

    public void delete(Message message) {
        try {
            message.delete().queue();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public void deleteLater(Message message) {
        try {
            message.delete().queueAfter(DELETE_DELAY, TimeUnit.SECONDS);
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public void queue(Message message) {
        queue(this.output, message, null);
    }

    public void queue(MessageChannel channel, Message message) {
        queue(channel, message, null);
    }

    public void queue(Message message, Consumer<Message> consumer) {
        queue(this.output, message, consumer);
    }

    public void queue(MessageChannel channel, Message message, Consumer<Message> consumer) {
        try {
            channel.sendMessage(message).queue(consumer);
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public Message complete(Message message) {
        return complete(this.output, message);
    }

    public Message complete(MessageChannel channel, Message message) {
        try {
            channel.sendMessage(message).complete();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
        return null;
    }

    public void removeReaction(Message message, MessageReaction.ReactionEmote emote, Member member) {
        try {
            message.removeReaction(emote.getEmoji(), member.getUser()).queue();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public void log(String string) {
        System.out.println("[" + this.guild.getName() + "] (" + Thread.currentThread().getId() + ") " + string);
    }

    public Guild getGuild() {
        return this.guild;
    }

    public QueuePlayer getPlayer(VoiceChannel channel) {
        if (channel == null || (this.player.getConnectedChannel() != null && channel.getIdLong() != this.player.getConnectedChannel().getIdLong())) {
            return null;
        }
        return this.player;
    }

    public TextChannel getChannel() {
        return this.output;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public MessageBuilder getBuilder() {
        return this.builder;
    }

    public CommandManager getCommands() {
        return this.commands;
    }

    public ButtonManager getButtons() {
        return this.buttons;
    }

    public AudioPlayerManager getManager() {
        return this.bot.getManager();
    }

    public TrackLoader getLoader() {
        return this.loader;
    }

    public void handleMissingPermission(InsufficientPermissionException e) {
        log("insuficciant permissions. missing " + e.getMessage() + " in " + e.getChannelType().name() + " " + e.getChannel(getGuild().getJDA()).getName());
        sendErrorMessage("insuficciant permissions. missing " + e.getMessage() + " in " + e.getChannelType().name() + " " + e.getChannel(getGuild().getJDA()).getName());
    }
}
