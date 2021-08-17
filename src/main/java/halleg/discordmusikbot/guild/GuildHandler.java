package halleg.discordmusikbot.guild;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import halleg.discordmusikbot.MusicBot;
import halleg.discordmusikbot.guild.buttons.ButtonManager;
import halleg.discordmusikbot.guild.commands.CommandManager;
import halleg.discordmusikbot.guild.player.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.HashMap;
import java.util.Map;
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
    private Role role;
    private Map<Long, Long> linkedBots;

    private Player player;
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
        //this.role = this.guild.getRoleById(config.getRoleId());
        this.linkedBots = config.getLinkedBots();

        this.bot = musicbot;
        this.builder = new MessageBuilder(this);
        this.player = new Player(this);
        this.commands = new CommandManager(this);
        this.buttons = new ButtonManager(this);
        this.loader = new TrackLoader(this, musicbot.getPreloader());

        if (this.output == null) {
            setChannel(this.guild.getTextChannels().get(0));
        } else {
            clearLastMessages(this.output);
        }
        saveConfig();
        log("initialized! outputchannel: " + this.output.getName() + " prefix: " + this.prefix);
        setupCheck();
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

    private void setupCheck() {
        EmbedBuilder primary = new EmbedBuilder();
        primary.setTitle("Primary Voice Channel");
        primaryTextchannelPermissionCheck(primary);
        for (long l : this.linkedBots.keySet()) {
            linkedTextchannelPermissionCheck(primary, l, this.guild.getTextChannelById(this.linkedBots.get(l)));
        }
        EmbedBuilder voice = new EmbedBuilder();
        voice.setTitle("Visible Voice Channel");
        voiceChannelPermissionCheck(voice);
        EmbedBuilder text = new EmbedBuilder();
        voice.setTitle("Visible Text Channel");
        textChannelPermissionCheck(text);

        this.output.sendMessageEmbeds(primary.build(), voice.build(), text.build()).queue();
    }

    public void primaryTextchannelPermissionCheck(EmbedBuilder eb) {
        Permission[] text = {
                Permission.VIEW_CHANNEL,
                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_MANAGE
        };

        addPermissionsField(eb, text, this.output, "output channel");
    }

    public void linkedTextchannelPermissionCheck(EmbedBuilder eb, long bot, TextChannel channel) {
        Permission[] text = {
                Permission.VIEW_CHANNEL,
                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_MANAGE
        };

        addPermissionsField(eb, text, channel, "linked channel for\n`" + bot + "`");
    }

    public void voiceChannelPermissionCheck(EmbedBuilder eb) {
        String name = "Visible voicechannel";
        String connect = "can connect";
        String speak = "can speak";

        for (VoiceChannel l : this.guild.getVoiceChannels()) {

            Member self = l.getGuild().getSelfMember();
            if (name == null) {
                name = l.getName();
                connect = getStatusEmote(self.hasPermission(Permission.VOICE_CONNECT));
                speak = getStatusEmote(self.hasPermission(Permission.VOICE_SPEAK));
            } else {
                eb.addField(name, l.getName(), true);
                eb.addField(connect, getStatusEmote(self.hasPermission(Permission.VOICE_CONNECT)), true);
                eb.addField(speak, getStatusEmote(self.hasPermission(Permission.VOICE_SPEAK)), true);
                name = null;
                connect = null;
                speak = null;
            }
        }

        if (name != null) {
            {
                eb.addField(name, "", true);
                eb.addField(connect, "", true);
                eb.addField(speak, "", true);
            }
        }
    }

    public void textChannelPermissionCheck(EmbedBuilder eb) {
        String name = "Visible textchannel";
        String connect = "can message";
        String speak = "can canmanage";

        for (TextChannel l : this.guild.getTextChannels()) {

            Member self = l.getGuild().getSelfMember();
            if (name == null) {
                name = l.getName();
                connect = getStatusEmote(self.hasPermission(Permission.MESSAGE_WRITE));
                speak = getStatusEmote(self.hasPermission(Permission.MESSAGE_MANAGE));
            } else {
                eb.addField(name, l.getName(), true);
                eb.addField(connect, getStatusEmote(self.hasPermission(Permission.VOICE_CONNECT)), true);
                eb.addField(speak, getStatusEmote(self.hasPermission(Permission.VOICE_SPEAK)), true);
                name = null;
                connect = null;
                speak = null;
            }
        }

        if (name != null) {
            {
                eb.addField(name, "", true);
                eb.addField(connect, "", true);
                eb.addField(speak, "", true);
            }
        }
    }

    private String getStatusEmote(boolean b) {
        if (b) {
            return CONFIRMED;
        }
        return REMOVE_EMOJI;
    }

    private void addPermissionsField(EmbedBuilder eb, Permission[] text, GuildChannel channel, String title) {
        Member self = channel.getGuild().getSelfMember();
        String perms = "";
        String status = "";
        for (Permission p : text) {
            if (!self.hasPermission(channel, p)) {
                log("permission: " + p.getName() + " in channel " + this.output.getName());

                perms += p.getName() + ": " + "\n";
                status += REMOVE_EMOJI + "\n";
            } else {
                perms += p.getName() + ": " + "\n";
                status += CONFIRMED + "\n";
            }
        }

        eb.addField(title, channel.getAsMention(), true);
        eb.addField("Permissions", perms, true);
        eb.addField("Status", status, true);
    }

    private void clearLastMessages(TextChannel channel) {
        /*
        if (!PermissionUtil.checkPermission(channel, this.guild.getSelfMember(), Permission.MESSAGE_HISTORY)) {
            log("not permissione to view history, cant clean up!");
            return;
        }
        log("cleaning up...");
        channel.getHistoryBefore(channel.getLatestMessageId(), 20).queue(new Consumer<MessageHistory>() {
            @Override
            public void accept(MessageHistory t) {
                for (Message message : t.getRetrievedHistory()) {

                    if (message.getAuthor().getIdLong() != message.getJDA().getSelfUser().getIdLong()) {
                        return;
                    }

                    for (MessageReaction react : message.getReactions()) {
                        if (react.isSelf()) {
                            GuildHandler.this.builder.setPlayed(message);
                        }
                    }
                }

            }
        });

        if (channel.hasLatestMessage()) {
            channel.retrieveMessageById(channel.getLatestMessageId()).queue(new Consumer<Message>() {
                @Override
                public void accept(Message message) {
                    for (MessageReaction react : message.getReactions()) {
                        if (react.isSelf()) {
                            GuildHandler.this.builder.setPlayed(message);
                        }
                    }
                }
            });
        }
        */
    }

    public void handleMessage(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(this.prefix)) {
            this.commands.handleCommand(event.getMessage());
        } else {

            Player player = getPlayer(event.getMember().getVoiceState().getChannel());
            if (player == null) {
                return;
            }


            if (event.getChannel() != this.output) {
                if (this.linkedBots.values().contains(event.getChannel().getIdLong())) {
                    sendLostMessage(event.getMember(), event.getChannel());
                } else {
                    return;
                }
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

    public void delete(Message message) {
        try {
            message.delete().queue();
        } catch (InsufficientPermissionException e) {
            log("insuficciant permissions to delete message!");
            return;
        }
    }

    public void sendErrorMessage(String error) {
        queue(this.output, this.builder.buildNewErrorMessage(error), new Consumer<>() {
            @Override
            public void accept(Message message) {
                message.delete().queueAfter(DELETE_DELAY, TimeUnit.SECONDS);
            }
        });
    }

    public void sendInfoMessage(String message) {
        queue(this.output, this.builder.buildInfoMessage(message), new Consumer<>() {
            @Override
            public void accept(Message message) {
                message.delete().queueAfter(DELETE_DELAY, TimeUnit.SECONDS);
            }
        });
    }

    public void assignRole(VoiceChannel c) {
        for (Member m : c.getMembers()) {
            if (!m.getRoles().contains(this.role)) {
                this.guild.addRoleToMember(m, this.role).queue();
            }
        }
    }

    public void revokeRole(VoiceChannel c) {
        for (Member m : this.guild.getMembersWithRoles(this.role)) {
            this.guild.removeRoleFromMember(m, this.role);
        }
    }

    public void setBusy(boolean busy) {
        
    }

    public void sendHelpMessage(MessageChannel channel) {
        complete(channel, this.builder.buildHelpMessage());
    }

    public void sendRepeatMessage(String link, Consumer<Message> c) {
        queue(this.output, this.builder.buildRepeatMessage(link), c);
    }

    public Message complete(Message message) {
        return complete(this.output, message);
    }

    private Message complete(MessageChannel channel, Message message) {
        MessageAction act = send(channel, message);
        if (act != null) {
            return act.complete();
        }
        return null;
    }

    public void queue(Message message, Consumer<Message> consumer) {
        queue(this.output, message, consumer);
    }

    private void queue(MessageChannel channel, Message message, Consumer<Message> consuber) {
        MessageAction act = send(channel, message);
        if (act != null) {
            act.queue(consuber);
        }
    }

    public void deleteLater(Message message) {
        message.delete().queueAfter(DELETE_DELAY, TimeUnit.SECONDS);
    }

    private MessageAction send(MessageChannel channel, Message message) {
        try {
            return channel.sendMessage(message);
        } catch (InsufficientPermissionException e) {
            log("insuficciant permissions to send Message!");
        } catch (NullPointerException e) {
            log("output not initialized!");
        }

        return null;
    }

    public void log(String string) {
        System.out.println("[" + this.guild.getName() + "] (" + Thread.currentThread().getId() + ") " + string);
    }

    public boolean reactionPermissionCheck() {
        boolean ret = PermissionUtil.checkPermission(this.output, this.guild.getSelfMember(),
                Permission.MESSAGE_ADD_REACTION);
        if (!ret) {
            log("missing reaction manage permission, continuing anyways...");
        }
        return ret;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public Player getPlayer(VoiceChannel channel) {
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

    public Role getRole() {
        return this.role;
    }

    public boolean isLinkedBot(User u) {
        return this.linkedBots.get(u.getIdLong()) != null;
    }

    public void addLikedBot(Member m, TextChannel t) {
        this.linkedBots.put(m.getUser().getIdLong(), t.getIdLong());
        saveConfig();
    }

    public Map<Long, Long> getLinkedBots() {
        return this.linkedBots;
    }

    public void sendLostMessage(Member member, MessageChannel channel) {
        queue(channel, this.builder.buildInfoMessage(member.getAsMention() + " you seem to be lost. This is where you need to go: " + this.output.getAsMention()), new Consumer<>() {
            @Override
            public void accept(Message message) {
                message.delete().queueAfter(DELETE_DELAY, TimeUnit.SECONDS);
            }
        });
    }
}
