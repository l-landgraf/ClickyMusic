package halleg.discordmusikbot.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import halleg.discordmusikbot.guild.GuildHandler;
import halleg.discordmusikbot.player.loader.LoadHandler;
import halleg.discordmusikbot.player.loader.PlaylistLoadHandler;
import halleg.discordmusikbot.player.loader.PlaylistLoadSynchonizer;
import halleg.discordmusikbot.player.queue.QueueElement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public class Player implements Timer.TimerListener {
    private static final long DISCONNECT_TIME = 10000l;

    private List<QueueElement> queue;
    private QueueElement currentTrack;

    private AudioPlayerManager manager;
    private SendHandler sender;
    private EventListener listener;

    private AudioPlayer player;
    private GuildHandler handler;
    private AudioManager audioManager;

    private Timer timer;

    private SpotifyLinkHandler spotifyLinkHandler;

    public Player(GuildHandler handler, AudioPlayerManager manager) {
        this.handler = handler;
        this.audioManager = handler.getGuild().getAudioManager();
        this.manager = manager;
        this.player = this.manager.createPlayer();
        this.listener = new EventListener(handler);
        this.player.addListener(this.listener);
        this.sender = new SendHandler(this.player);
        this.queue = new LinkedList<>();
        this.timer = new Timer(DISCONNECT_TIME, this);
        this.audioManager.setSendingHandler(this.sender);
        this.spotifyLinkHandler = new SpotifyLinkHandler(this.handler);
    }

    public void loadAndQueueSearch(String query, Member member) {
        loadAndQueue(youtubeSearch(query), member, false);
    }

    private synchronized String youtubeSearch(String query) {
        try {
            this.handler.log("searching youtube for: \"" + query + "\"");
            String escape = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8");
            Document doc = Jsoup.connect(escape).get();
            for (Element e : doc.getElementsByTag("a")) {
                String href = e.attr("href");
                if (href.startsWith("/watch?v=")) {
                    this.handler.log("found link: " + href + " for \"" + query + "\"");
                    return "https://www.youtube.com" + href;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.handler.log("found nothing");
        return null;
    }

    public void clearQueue() {
        this.player.stopTrack();
        for (QueueElement queueElement : this.queue) {
            queueElement.onPlayed();
        }
        this.queue.clear();

        nextTrack();
    }

    public void loadAndQueueAndJoin(String source, Member member) {
        join(member.getVoiceState().getChannel());
        loadAndQueue(source, member, true);
    }

    public void loadAndQueueSpotify(String source, Member member, PlaylistLoadSynchonizer sync, int nr) {
        String ytLink = youtubeSearch(source);
        PlaylistLoadHandler loader = new PlaylistLoadHandler(sync, source, nr);
        this.manager.loadItem(ytLink, loader);
    }

    private void loadAndQueue(String source, Member member, boolean retry) {
        if (source.startsWith("https://www.youtube.com") || source.startsWith("www.youtube.com")
                || source.startsWith("youtube.com")) {
            source = source.split("&")[0];
        }

        if (this.spotifyLinkHandler.handleLink(source, member)) {
            return;
        }

        this.manager.loadItem(source, new LoadHandler(this.handler, member, source, retry));
    }

    public void disconnect() {
        this.audioManager.closeAudioConnection();
    }

    public void leave() {
        disconnect();
        clearQueue();
    }

    public void queueComplete(QueueElement element) {
        try {
            MessageEmbed m = element.buildMessage();
            Message message = this.handler.complete(m);
            element.setMessage(message);
            Player.this.queue.add(element);
            if (this.currentTrack == null) {
                nextTrack();
            } else {
                element.onQueued();
            }
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
    }

    public void trackEnded() {
        this.currentTrack.onEnded();
    }

    public void nextTrack() {
        if (this.currentTrack != null) {
            this.currentTrack.onPlayed();
            this.currentTrack = null;
        }

        QueueElement next = null;
        if (!this.queue.isEmpty()) {
            next = this.queue.get(0);
        }

        if (next == null) {
            this.player.stopTrack();
        } else {
            this.queue.remove(next);
            this.currentTrack = next;
            this.currentTrack.onPlaying();
        }

    }

    public void playTrack(AudioTrack track) {
        this.player.playTrack(track);
    }

    public void join(VoiceChannel c) {
        if (c == null) {
            return;
        }

        if (getConnectedChannel() != null &&
                c != getConnectedChannel()) {
            return;
        }
        connect(c);
    }

    public void connect(VoiceChannel c) {
        setPaused(false);
        this.audioManager.openAudioConnection(c);
    }

    public void removeElement(QueueElement element) {
        this.queue.remove(element);
    }

    public QueueElement findElement(long id) {
        if (this.currentTrack != null && this.currentTrack.getMessage().getIdLong() == id) {
            return this.currentTrack;
        }

        for (QueueElement queueElement : this.queue) {
            if (queueElement.getMessage().getIdLong() == id) {
                return queueElement;
            }
        }
        return null;
    }

    public QueueElement findElement(AudioTrack track) {
        /*if (this.currentTrack.getTrack() == track) {
            return this.currentTrack;
        }

        for (QueueElement queueElement : this.queue) {
            if (queueElement.getTrack() == track) {
                return queueElement;
            }
        }*/
        return null;
    }

    public void voiceUpdate() {
        if (this.audioManager.isConnected()) {
            if (this.audioManager.getConnectedChannel().getMembers().size() > 1) {
                this.handler.log("Timer stopped.");
                this.timer.stop();
            }
            if (this.audioManager.getConnectedChannel().getMembers().size() == 1) {
                this.handler.log("Disconnecting in " + DISCONNECT_TIME / 1000 + "s...");
                this.timer.start();
            }
        }
    }

    public void setPaused(boolean b) {
        this.player.setPaused(b);
    }

    public boolean isPaused() {
        return this.player.isPaused();
    }

    public VoiceChannel getConnectedChannel() {
        return this.audioManager.getConnectedChannel();
    }

    @Override
    public void onTimerEnd() {
        leave();
    }

    public void togglePaused() {
        setPaused(!isPaused());
    }
}
