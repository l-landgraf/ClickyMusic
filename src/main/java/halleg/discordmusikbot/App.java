package halleg.discordmusikbot;

import halleg.discordmusikbot.guild.spotify.SpotifyApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class App extends ListenerAdapter {

    public static void main(String[] args) throws Exception {

        // https://discordapp.com/api/oauth2/authorize?client_id=565293748309983252&permissions=36777024&scope=bot

        if (args.length < 4) {
            System.out.println(
                    "Required arguments: Discord-Bot-Secret, Spotify-Client-Id, Spotify-Client-Secret, Music-Folder");
            System.exit(1);
        }

        File file = new File(args[3]);
        if (!file.exists() || !file.isDirectory()) {
            System.out.println(file.getAbsolutePath() + " does not exist or is not a directory.");
            System.exit(1);
        }

        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        intents.add(GatewayIntent.GUILD_VOICE_STATES);
        intents.add(GatewayIntent.MESSAGE_CONTENT);
        JDA jda = JDABuilder.create(intents).setToken(args[0]).build().awaitReady();


        SpotifyApi.initialize(args[1], args[2]);

        jda.addEventListener(new MusicBot(jda, file));
        jda.addEventListener(new DisconnectListener());
    }
}
