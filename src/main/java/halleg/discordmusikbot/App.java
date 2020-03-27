package halleg.discordmusikbot;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.google.api.services.youtube.YouTube.Builder;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class App extends ListenerAdapter {
	private static final String clientId = "a896e424d8714b73b3903fe55b99cd31";
	private static final String clientSecret = "9fe0cb5fd8c744259d4fbacabae206bc";

	public static void main(String[] args) throws Exception {

		// https://discordapp.com/api/oauth2/authorize?client_id=565293748309983252&permissions=36777024&scope=bot

		JDA jda = new JDABuilder(AccountType.BOT).setToken(args[0]).setStatus(OnlineStatus.DO_NOT_DISTURB).build().awaitReady();

		jda.getPresence().setStatus(OnlineStatus.ONLINE);

		jda.addEventListener(new MusicBot(jda));
	}
}
