package halleg.discordmusikbot;

import halleg.discordmusikbot.guild.player.spotify.SpotifyApi;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class App extends ListenerAdapter {

    public static void main(String[] args) throws Exception {

        // https://discordapp.com/api/oauth2/authorize?client_id=565293748309983252&permissions=36777024&scope=bot


        if (args.length < 3) {
            System.out.println("Requeired arguments: Discord-Bot-Secret, Spotify-Client-Id, Spotify-Client-Secret");
            System.exit(1);
        }


        JDA jda = new JDABuilder(AccountType.BOT).setToken(args[0]).build().awaitReady();
        SpotifyApi.initialize(args[1], args[2]);
        jda.addEventListener(new MusicBot(jda));
    }
}
