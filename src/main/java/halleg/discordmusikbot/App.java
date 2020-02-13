package halleg.discordmusikbot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class App extends ListenerAdapter {
	public static void main(String[] args) throws Exception {

		// https://discordapp.com/api/oauth2/authorize?client_id=565293748309983252&permissions=36777024&scope=bot

		JDA jda = new JDABuilder(AccountType.BOT).setToken(args[0]).setStatus(OnlineStatus.DO_NOT_DISTURB).build();

		try {
			Thread.sleep(5000l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jda.getPresence().setStatus(OnlineStatus.ONLINE);

		jda.addEventListener(new MusicBot(jda));
	}
}
