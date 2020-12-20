package halleg.discordmusikbot;

import com.fasterxml.jackson.databind.ObjectMapper;
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
	private ObjectMapper mapper;

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
		try {
			File dir = new File(".");
			File[] filesList = dir.listFiles();
			for (File file : filesList) {
				System.out.println("checking file: " + file.getName());
				if (file.getName().matches("\\d+\\.config")) {
					System.out.println("loading file..");
					FileInputStream in = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(in);
					GuildConfig config = (GuildConfig) (ois.readObject());

					this.map.put(config.getGuildid(), new GuildHandler(this, this.jda.getGuildById(config.getGuildid()),
							config.getChannelid(), config.getPrefix()));
					ois.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfig(GuildHandler handler) {
		try {
			File file  = new File(handler.getGuild().getIdLong() + ".config");
			mapper.writeValue(file,new GuildConfig(handler));
			System.out.println("saved config file " + handler.getGuild().getIdLong() + ".config");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		if (event.getAuthor().isBot()) {
			return;
		}

		getHandler(event.getGuild().getIdLong()).handleMessage(event);

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
			getHandler(channel.getGuild().getIdLong()).voiceUpdate();
		}
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		getHandler(event.getGuild().getIdLong());
	}

	private void handleReaction(Member member, MessageChannel channel, long messageid,
								MessageReaction react) {
		if (member.getUser().isBot()) {
			return;
		}

		channel.retrieveMessageById(messageid).queue(new Consumer<>() {
			@Override
			public void accept(Message message) {
				getHandler(message.getGuild().getIdLong()).handleReaction(react, message, member);
			}
		});
	}

	private GuildHandler getHandler(long id) {
		GuildHandler handler = MusicBot.this.map.get(id);
		if (handler == null) {
			handler = new GuildHandler(MusicBot.this, this.jda.getGuildById(id), 0l, ".");
			MusicBot.this.map.put(id, handler);
		}

		return handler;
	}

	public AudioPlayerManager getManager() {
		return this.manager;
	}

	public TrackLoader.PlaylistPreloadManager getPreloader() {
		return this.preloader;
	}
}
