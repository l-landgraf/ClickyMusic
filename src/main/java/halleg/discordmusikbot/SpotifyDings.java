package halleg.discordmusikbot;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;

import java.io.IOException;

public class SpotifyDings {

    private static SpotifyApi spotifyApi = null;

    public static void initialize(String clientId, String clientSecret) {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();

        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static Playlist loadPlaylist(String s) {
        System.out.println("Trying to load Playlist: " + s);

        GetPlaylistRequest getPlaylistRequest = spotifyApi
                .getPlaylist(s)
                .build();

        try {
            Playlist playlist = getPlaylistRequest.execute();
            return playlist;

        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static Album loadAlbum(String s) {
        System.out.println("Trying to load Playlist: " + s);

        GetAlbumRequest getAlbumRequest = spotifyApi
                .getAlbum(s)
                .build();
        try {
            Album album = getAlbumRequest.execute();
            return album;

        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
}
