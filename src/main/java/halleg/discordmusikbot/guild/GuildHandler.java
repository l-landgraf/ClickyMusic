package halleg.discordmusikbot.guild;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import halleg.discordmusikbot.MusicBot;
import halleg.discordmusikbot.guild.buttons.ButtonManager;
import halleg.discordmusikbot.guild.commands.CommandManager;
import halleg.discordmusikbot.guild.config.GuildConfig;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

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
    public static final String SAVED = "\uD83D\uDCC2";

    public static final String BUSY = "\uD83D\uDD34";
    public static final String NOT_BUSY = "\uD83D\uDFE2";
    public static final String RIP = "\uD83D\uDC80";

    public static final int PLAYLIST_PREVIEW_MAX = 3;
    public static final int PRELOAD_MAX = 5;
    public static final int RETRY_AMOUNT = 5;
    public static final long DELETE_DELAY = 60;

    private GuildConfig config;

    private QueuePlayer player;
    private MusicBot bot;
    private MessageFactory builder;
    private CommandManager commands;
    private ButtonManager buttons;
    private TrackLoader loader;
    private FileManager fileManager;
    private AudioPlayerManager audioPlayerManager;

    public GuildHandler(MusicBot musicbot, GuildConfig config, AudioPlayerManager audioPlayerManager) {
        this.config = config;
        this.audioPlayerManager = audioPlayerManager;
        this.bot = musicbot;
        this.builder = new MessageFactory(this);
        this.player = new QueuePlayer(this, audioPlayerManager.createPlayer());
        this.commands = new CommandManager(this);
        this.buttons = new ButtonManager(this);

        this.loader = new TrackLoader(this, musicbot.getPreloader());
        this.fileManager = new FileManager(this.bot.getMusikFolder(), this);
        log("initialized! outputchannel: " + config.getOutputChannel().getName() + " prefix: " + config.getPrefix());
    }

    public void setChannel(TextChannel channel) {
        this.config.setOutputChannel(channel);
        log("set channel to: " + channel.getName());
        sendInfoMessage("This is now the prefered channel.");
        saveConfig();
    }

    public void setPrefix(String prefix) {
        this.config.setPrefix(prefix);
        sendInfoMessage("Commands for this bot now have to start with `" + this.config.getPrefix() + "`.");
        saveConfig();
    }

    public void saveConfig() {
        this.bot.saveGuildHandler(this);
    }

    public void handleMessage(MessageReceivedEvent event) {
        if (!event.getMessage().getAttachments().isEmpty() && event.getChannel().getIdLong() == this.config.getOutputChannelId()) {
            handleAttachments(event);
            return;
        }
        if (!isCorrectChannel(event.getMember().getVoiceState().getChannel())) {
            return;
        }

        if (event.getChannel() != this.config.getOutputChannel()) {
            return;
        }

        try {
            this.player.join(event.getMember().getVoiceState().getChannel());
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
            return;
        }

        this.builder.setLoading(event.getMessage());
        this.loader.search(event.getMessage().getContentRaw(), this.player, event.getMember(), event.getMessage());
    }

    public void handleCommand(SlashCommandInteractionEvent event) {
        this.commands.handleCommand(event);
    }

    private void handleAttachments(MessageReceivedEvent event) {
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (!attachment.getFileExtension().equals("mp3")) {
                continue;
            }
            if (this.fileManager.downloadAttachment(attachment, event.getMessage().getContentRaw())) {
                addReaction(event.getMessage(), SAVED);
            }
        }
        return;
    }

    public void handleReaction(MessageReaction react, Message message, Member member) {
        this.buttons.handleReaction(message, react, member);
    }

    public void voiceUpdate() {
        this.player.voiceUpdate();
    }

    public void sendErrorMessage(String error) {
        queue(this.builder.buildNewErrorMessage(error), m -> deleteLater(m));
    }

    public void sendInfoMessage(String message) {
        queue(this.builder.buildInfoMessage(message), m -> deleteLater(m));
    }

    public void sendHelpMessage(MessageChannel channel) {
        queue(channel, this.builder.buildHelpMessage());
    }

    public void sendRepeatMessage(String link, Consumer<Message> c) {
        queue(this.builder.buildRepeatMessage(link), c);
    }

    public void delete(Message message) {
        try {
            message.delete().queue();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }


    public void queueAndDeleteLater(ReplyCallbackAction reply) {
        try {
            reply.queue(ih -> ih.deleteOriginal().queueAfter(DELETE_DELAY, TimeUnit.SECONDS));
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public void queueAndDeleteLater(Message message) {
        try {
            queue(message, m -> deleteLater(m));
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
        queue(this.config.getOutputChannel(), message, null);
    }

    public void queue(MessageChannel channel, Message message) {
        queue(channel, message, null);
    }

    public void queue(Message message, Consumer<Message> consumer) {
        queue(this.config.getOutputChannel(), message, consumer);
    }

    public void queue(MessageChannel channel, Message message, Consumer<Message> consumer) {
        try {
            channel.sendMessage(message).queue(consumer);
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public Message complete(Message message) {
        return complete(this.config.getOutputChannel(), message);
    }

    public Message complete(MessageChannel channel, Message message) {
        try {
            return channel.sendMessage(message).complete();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
            return null;
        }
    }

    public void addReaction(Message message, String emote) {
        try {
            message.addReaction(emote).queue();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public void removeReaction(Message message, MessageReaction.ReactionEmote emote, Member member) {
        try {
            message.removeReaction(emote.getEmoji(), member.getUser()).queue();
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public void log(String string) {
        System.out.println("[" + this.config.getGuild().getName() + "] (" + Thread.currentThread().getId() + ") " + string);
    }

    public Guild getGuild() {
        return this.config.getGuild();
    }

    public QueuePlayer getPlayer() {
        return this.player;
    }

    public MessageChannel getChannel() {
        return this.config.getOutputChannel();
    }

    public String getPrefix() {
        return this.config.getPrefix();
    }

    public MessageFactory getBuilder() {
        return this.builder;
    }

    public CommandManager getCommands() {
        return this.commands;
    }

    public ButtonManager getButtons() {
        return this.buttons;
    }

    public AudioPlayerManager getManager() {
        return this.audioPlayerManager;
    }

    public TrackLoader getLoader() {
        return this.loader;
    }

    public GuildConfig getConfig() {
        return this.config;
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public boolean isCorrectChannel(AudioChannel channel) {
        return channel != null && (this.player.getConnectedChannel() == null || channel.getIdLong() == this.player.getConnectedChannel().getIdLong());
    }

    public void handleMissingPermission(InsufficientPermissionException e) {
        String message =
                e.getMessage() + " in " + e.getChannelType().name() + "-CHANNEL \"" + e.getChannel(getGuild().getJDA()).getName() + "\"";
        log(message);
        sendErrorMessage(message);
    }
}
