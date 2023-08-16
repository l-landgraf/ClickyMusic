package halleg.discordmusikbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.TrackLoader;
import halleg.discordmusikbot.guild.config.GuildConfig;
import halleg.discordmusikbot.guild.config.GuildConfigBuilder;
import halleg.discordmusikbot.guild.config.GuildConfigException;
import halleg.discordmusikbot.guild.local.MyLocalAudioSourceManager;
import halleg.discordmusikbot.guild.spotify.SpotifyAudioSourceManager;
import halleg.discordmusikbot.guild.youtube.MyYoutubeAudioSourceManager;
import halleg.discordmusikbot.guild.youtube.YoutubeQueryAudioSourceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class MusicBot extends ListenerAdapter {
    private JDA jda;
    private Map<Long, GuildHandler> map;
    private SpotifyAudioSourceManager preloader;
    private YoutubeAudioSourceManager ytManager;
    private ObjectMapper mapper;
    private HttpClient httpClient;
    private File musicFolder;

    public MusicBot(JDA jda, File musicFolder) {
        this.jda = jda;
        this.musicFolder = musicFolder;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.httpClient = HttpClient.newBuilder().build();
        this.preloader = new SpotifyAudioSourceManager(this.ytManager);
        this.ytManager = new MyYoutubeAudioSourceManager();
        this.map = new HashMap<>();

        initGuilds();
    }

    private AudioPlayerManager buildAudioPlayerManager(Guild guild) {
        AudioPlayerManager manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
        manager.registerSourceManager(this.ytManager);
        manager.registerSourceManager(this.preloader);
        manager.registerSourceManager(new MyLocalAudioSourceManager(new File(this.musicFolder, guild.getId())));
        manager.registerSourceManager(new YoutubeQueryAudioSourceManager(this.ytManager));
        return manager;
    }

    private void initGuilds() {
        for (Guild g : this.jda.getGuilds()) {
            File file = new File(getFilename(g.getIdLong()));
            GuildHandler handler = null;

            System.out.println("initializing guild " + g.getName() + " (" + g.getIdLong() + ")");
            try {
                handler = new GuildHandler(this, loadConfig(file).build(g), buildAudioPlayerManager(g), this.mapper,
                        this.httpClient);
            } catch (Exception ee) {
                System.out.println("failed to load, using default");
                try {
                    handler = new GuildHandler(this, new GuildConfigBuilder().build(g), buildAudioPlayerManager(g),
                            this.mapper, this.httpClient);
                } catch (GuildConfigException e) {
                    System.out.println("Error: " + e.getMessage());
                    continue;
                }
                saveGuildHandler(handler);
            }
            this.map.put(g.getIdLong(), handler);
        }
    }


    public void saveGuildHandler(GuildHandler handler) {
        try {
            File file = new File(getFilename(handler.getGuild().getIdLong()));
            this.mapper.writeValue(file, handler.getConfig());
            System.out.println("saved config file " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GuildConfigBuilder loadConfig(File file) throws IOException, FileNotFoundException {
        System.out.println("loading file " + file.getAbsolutePath());
        return this.mapper.readValue(file, GuildConfigBuilder.class);

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        this.map.get(event.getGuild().getIdLong()).handleMessage(event);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        this.map.get(event.getGuild().getIdLong()).handleCommand(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        this.map.get(event.getGuild().getIdLong()).handleButton(event);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannel channel = event.getChannelJoined();

        if (channel == null) {
            channel = event.getChannelLeft();
        }

        if (channel != null) {
            this.map.get(channel.getGuild().getIdLong()).voiceUpdate();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("joined new server " + event.getGuild().getIdLong());
        try {
            GuildHandler guildHandler = new GuildHandler(this, new GuildConfigBuilder().build(event.getGuild()),
                    buildAudioPlayerManager(event.getGuild()), this.mapper, this.httpClient);
            this.map.put(event.getGuild().getIdLong(), guildHandler);
            saveGuildHandler(guildHandler);
        } catch (GuildConfigException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        System.out.println("left server " + event.getGuild().getIdLong());
        this.map.remove(event.getGuild().getIdLong());
        File file = new File("./" + getFilename(event.getGuild().getIdLong()));
        file.delete();
    }

    public String getFilename(long l) {
        return l + ".config";
    }

    public TrackLoader.PlaylistPreloadManager getPreloader() {
        return this.preloader;
    }


    public File getMusikFolder() {
        return this.musicFolder;
    }
}
