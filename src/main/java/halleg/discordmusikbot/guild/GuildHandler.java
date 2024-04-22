package halleg.discordmusikbot.guild;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import halleg.discordmusikbot.MusicBot;
import halleg.discordmusikbot.guild.blocker.Skipper;
import halleg.discordmusikbot.guild.buttons.ButtonManager;
import halleg.discordmusikbot.guild.commands.CommandManager;
import halleg.discordmusikbot.guild.config.GuildConfig;
import halleg.discordmusikbot.guild.player.QueuePlayer;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.net.http.HttpClient;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GuildHandler {
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
    private Skipper skipper;

    public GuildHandler(MusicBot musicbot, GuildConfig config, AudioPlayerManager audioPlayerManager,
                        ObjectMapper objectMapper, HttpClient httpClient) {
        this.config = config;
        this.audioPlayerManager = audioPlayerManager;
        this.bot = musicbot;
        this.builder = new MessageFactory(this);
        this.player = new QueuePlayer(this, audioPlayerManager.createPlayer());
        this.commands = new CommandManager(this);
        this.buttons = new ButtonManager(this);
        this.skipper = new Skipper(httpClient, objectMapper);

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

        if (!isFreeAudioChannel(event.getMember().getVoiceState().getChannel())) {
            return;
        }

        if (event.getChannel() != this.config.getOutputChannel()) {
            return;
        }

        if (!this.player.join(event.getMember().getVoiceState().getChannel())) {
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

    public void handleButton(ButtonInteractionEvent event) {
        this.buttons.handleEvent(event, this.player);
    }

    public void voiceUpdate() {
        this.player.voiceUpdate();
    }

    public <T> void queueLater(RestAction<Void> delete) {
        try {
            delete.queueAfter(DELETE_DELAY, TimeUnit.SECONDS);
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
        }
    }

    public <T> boolean queue(RestAction<T> action) {
        return queue(action, null);
    }

    public <T> boolean queue(RestAction action, Consumer<T> consumer) {
        try {
            action.queue(consumer, throwable -> {
                log(throwable.toString());
            });
            return true;
        } catch (InsufficientPermissionException e) {
            handleMissingPermission(e);
            return false;
        }
    }

    public <T> T complete(RestAction<T> action) {
        return action.complete();
    }

    public void sendInfoMessage(String message) {
        queue(this.config.getOutputChannel().sendMessage(this.builder.buildInfoMessage(message)));
    }

    public void sendErrorMessage(String message) {
        queue(this.config.getOutputChannel().sendMessage(this.builder.buildErrorMessage(message)));
    }


    public boolean addReaction(Message message, String reaction) {
        Emoji e = Emoji.fromUnicode(reaction);
        try {
            message.addReaction(e).queue();
            return true;
        } catch (InsufficientPermissionException ex) {
            handleMissingPermission(ex);
            return false;
        }
    }

    public void log(String string) {
        System.out.println(System.currentTimeMillis() + " [" + this.config.getGuild().getName() + "] " + string);
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

    public boolean isSameAudioChannel(AudioChannel channel) {
        if (channel == null) {
            return false;
        }
        return channel == this.player.getConnectedChannel();
    }

    public boolean isFreeAudioChannel(AudioChannel channel) {
        if (channel == null) {
            return false;
        }

        if (this.player.getConnectedChannel() == null) {
            return true;
        }

        return channel == this.player.getConnectedChannel();
    }

    public void handleMissingPermission(InsufficientPermissionException e) {
        String message =
                e.getMessage() + " in " + e.getChannelType().name() + "-CHANNEL \"" + e.getChannel(getGuild().getJDA()).getName() + "\"";
        log(message);
        sendErrorMessage(message);
    }
}
