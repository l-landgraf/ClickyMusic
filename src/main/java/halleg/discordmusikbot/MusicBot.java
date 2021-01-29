package halleg.discordmusikbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import halleg.discordmusikbot.guild.GuildConfig;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.guild.TrackLoader;
import halleg.discordmusikbot.guild.local.MyLocalAudioSourceManager;
import halleg.discordmusikbot.guild.spotify.SpotifyAudioSourceManager;
import halleg.discordmusikbot.guild.youtube.MyYoutubeAudioSourceManager;
import halleg.discordmusikbot.guild.youtube.YoutubeQuerryAudioSourceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MusicBot extends ListenerAdapter {
	private JDA jda;
	private Map<Long, GuildHandler> map;
	private AudioPlayerManager manager;
	private SpotifyAudioSourceManager preloader;

	public MusicBot(JDA jda, File musicFolder) {
		this.jda = jda;
		this.manager = new DefaultAudioPlayerManager();
		YoutubeAudioSourceManager ytManager = new MyYoutubeAudioSourceManager();
		this.manager.registerSourceManager(ytManager);
		this.preloader = new SpotifyAudioSourceManager(ytManager);
		this.manager.registerSourceManager(this.preloader);
		this.manager.registerSourceManager(new MyLocalAudioSourceManager(musicFolder));
		this.manager.registerSourceManager(new YoutubeQuerryAudioSourceManager(ytManager));
		this.map = new HashMap<>();
		loadConfigs();
	}

	private void loadConfigs() {
		for (Guild g : this.jda.getGuilds()) {
			System.out.println("loading config for " + g.getIdLong());
			File file = new File("./" + getFilename(g.getIdLong()));
			if (!file.exists()) {
				System.out.println("no file found, using default settings");
				GuildHandler guildHandler = new GuildHandler(this, g);
				this.map.put(g.getIdLong(), guildHandler);
			} else {
				System.out.println("loading file...");
				try {
					GuildConfig config = loadConfig(file);
					this.map.put(config.getGuildid(), new GuildHandler(this, g, config.getChannelid(), config.getPrefix()));
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public GuildConfig loadConfig(File file) throws IOException, ClassNotFoundException {
		FileInputStream in = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(in);
		GuildConfig config = (GuildConfig) (ois.readObject());
		ois.close();
		System.out.println("successfully loaded");

		return config;
	}

	public void saveGuildHandler(GuildHandler handler) {
		try {
			FileOutputStream out = new FileOutputStream(getFilename(handler.getGuild().getIdLong()));
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(new GuildConfig(handler));
			oos.flush();
			oos.close();
			System.out.println("saved config file " + getFilename(handler.getGuild().getIdLong()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		if (event.getAuthor().isBot()) {
			return;
		}

		this.map.get(event.getGuild().getIdLong()).handleMessage(event);

	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		handleReaction(event.getMember(), event.getChannel(), event.getMessageIdLong(), event.getReaction());
	}

	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		//handleReaction(event.getMember(), event.getChannel(), event.getMessageIdLong(), event.getReaction());
	}

	@Override
	public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
		VoiceChannel channel = event.getChannelJoined();

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
		GuildHandler guildHandler = new GuildHandler(this, event.getGuild());
		this.map.put(event.getGuild().getIdLong(), guildHandler);
		saveGuildHandler(guildHandler);
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		System.out.println("left server " + event.getGuild().getIdLong());
		this.map.remove(event.getGuild().getIdLong());
		File file = new File("./" + getFilename(event.getGuild().getIdLong()));
		file.delete();
	}

	private void handleReaction(Member member, MessageChannel channel, long messageid,
								MessageReaction react) {
		if (member.getUser().isBot()) {
			return;
		}

		channel.retrieveMessageById(messageid).queue(new Consumer<>() {
			@Override
			public void accept(Message message) {
				MusicBot.this.map.get(message.getGuild().getIdLong()).handleReaction(react, message, member);
			}
		});
	}

	public String getFilename(long l) {
		return l + ".config";
	}

	public AudioPlayerManager getManager() {
		return this.manager;
	}

	public TrackLoader.PlaylistPreloadManager getPreloader() {
		return this.preloader;
	}
}
