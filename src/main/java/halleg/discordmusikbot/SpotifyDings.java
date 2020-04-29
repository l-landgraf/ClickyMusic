package halleg.discordmusikbot;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;

import java.io.IOException;
import java.util.Calendar;

public class SpotifyDings {

    private static SpotifyApi spotifyApi = null;
    private static String clientSecret;
    private static String clientId;
    private static long experationDate = 0;

    public static void initialize(String clientId, String clientSecret) {
        SpotifyDings.clientId = clientId;
        SpotifyDings.clientSecret = clientSecret;
        getAccess();
    }

    public static void getAccess() {
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

            experationDate = clientCredentials.getExpiresIn() * 1000 + System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(experationDate);
            System.out.println("New Spotify Experation Date: " + calendar.getTime().toString());
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void checkAccess() {
        if (experationDate < System.currentTimeMillis() - 1000l) {
            getAccess();
        }
    }

    public static Playlist loadPlaylist(String s) {
        checkAccess();
        System.out.println("Trying to load Playlist: " + s);

        GetPlaylistRequest getPlaylistRequest = spotifyApi
                .getPlaylist(s)
                .build();

        try {
            Playlist playlist = getPlaylistRequest.execute();
            return playlist;
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Spotify Error: " + e.getMessage());
        }
        return null;
    }

    public static PlaylistTrack[] loadPlaylistTracks(String s, int max) {
        checkAccess();
        System.out.println("Trying to load Playlist Tracks: " + s);

        int offset = 0;
        PlaylistTrack[] tracks = new PlaylistTrack[max];
        while (offset < max) {
            GetPlaylistsTracksRequest getPlaylistRequest = spotifyApi
                    .getPlaylistsTracks(s).offset(offset)
                    .build();
            try {
                Paging<PlaylistTrack> res = getPlaylistRequest.execute();

                for (int i = 0; i < res.getItems().length; i++) {
                    tracks[i + offset] = res.getItems()[i];
                }
                offset += res.getItems().length;

            } catch (IOException | SpotifyWebApiException e) {
                System.out.println("Spotify Error: " + e.getMessage());
                return null;
            }
        }
        return tracks;
    }

    public static Album loadAlbum(String s) {
        checkAccess();
        System.out.println("Trying to load Album: " + s);

        GetAlbumRequest getAlbumRequest = spotifyApi
                .getAlbum(s)
                .build();
        try {
            Album album = getAlbumRequest.execute();
            return album;

        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Spotify Error: " + e.getMessage());
        }
        return null;
    }
}
