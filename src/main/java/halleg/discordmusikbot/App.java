package halleg.discordmusikbot;

import java.util.ArrayList;
import java.util.Collection;

import halleg.discordmusikbot.guild.spotify.SpotifyApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App extends ListenerAdapter {

	public static void main(String[] args) throws Exception {

		// https://discordapp.com/api/oauth2/authorize?client_id=565293748309983252&permissions=36777024&scope=bot

		if (args.length < 3) {
			System.out.println("Requeired arguments: Discord-Bot-Secret, Spotify-Client-Id, Spotify-Client-Secret");
			System.exit(1);
		}

		for (String s : args) {
			System.out.println(s);
		}

		Collection<GatewayIntent> intents = new ArrayList<>();
		// intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		intents.add(GatewayIntent.GUILD_VOICE_STATES);
		JDA jda = JDABuilder.create(intents).setToken(args[0]).build().awaitReady();
		SpotifyApi.initialize(args[1], args[2]);
		jda.addEventListener(new MusicBot(jda));
	}
}
