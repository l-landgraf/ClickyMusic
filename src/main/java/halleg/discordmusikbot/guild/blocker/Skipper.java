package halleg.discordmusikbot.guild.blocker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Skipper {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;


    public Skipper(HttpClient httpClient, ObjectMapper objectMapper){
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public SkipSegment[] loadSegments(String videoid) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URI("https://sponsor.ajay.app/api/skipSegments?videoID="+videoid);

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(uri)
                                         .GET()
                                         .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = objectMapper.readTree(response.body());
        System.out.println(json.get("segment"));
        return null;
    }
}
