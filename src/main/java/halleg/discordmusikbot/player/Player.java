package halleg.discordmusikbot.player;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import halleg.discordmusikbot.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.managers.AudioManager;

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

	public Player(GuildHandler handler, AudioPlayerManager manager) {
		this.handler = handler;
		this.audioManager = handler.getGuild().getAudioManager();
		this.manager = manager;
		this.player = this.manager.createPlayer();
		this.listener = new EventListener(handler);
		this.player.addListener(this.listener);
		this.sender = new SendHandler(this.player);
		this.queue = new LinkedList<QueueElement>();
		this.timer = new Timer(DISCONNECT_TIME, this);
	}

	public void playSearch(String query, Member member) {
		play(youtubeSearch(query), member, false);
	}

	private String youtubeSearch(String query) {
		try {
			this.handler.log("searching youtube for: " + query);
			String escape = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8");
			URL url = new URL(escape);
			Document doc = Jsoup.connect(escape).get();
			for (Element e : doc.getElementsByTag("a")) {
				String href = e.attr("href");
				if (href.startsWith("/watch?v=")) {
					this.handler.log("found link: " + href);
					return "https://www.youtube.com" + href;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void clearQueue() {
		this.player.stopTrack();
		for (QueueElement queueElement : this.queue) {
			this.handler.getBuilder().setPlayed(queueElement.getMessage());
		}

		if (this.currentTrack != null) {
			this.handler.getBuilder().setPlayed(this.currentTrack.getMessage());
			this.currentTrack = null;
		}
	}

	public void removeElement(Message message, Member member) {

		if (this.currentTrack != null && this.currentTrack.getMessage().getIdLong() == message.getIdLong()) {
			nextTrack();
		}

		for (QueueElement queueElement : this.queue) {
			if (queueElement.getMessage().getIdLong() == message.getIdLong()) {
				this.handler.getBuilder().setRemoved(message, member);
				this.queue.remove(queueElement);
				break;
			}
		}
	}

	public void play(String source, Member member) {
		play(source, member, true);
	}

	private void play(String source, Member member, boolean retry) {
		this.handler.log("trying to load " + source);
		try {
			this.audioManager.openAudioConnection(member.getVoiceState().getChannel());
		} catch (IllegalArgumentException e) {
			this.handler.sendErrorMessage("Cant find Voicechannel!");
			return;
		}

		this.audioManager.setSendingHandler(this.sender);

		if (source.startsWith("https://www.youtube.com") || source.startsWith("www.youtube.com")
				|| source.startsWith("youtube.com")) {
			source = source.split("&")[0];
		}

		this.manager.loadItem(source, new LoadHandler(this.handler, member, source, retry));
	}

	public void leave() {
		this.audioManager.closeAudioConnection();
		clearQueue();
	}

	public void queue(final Track track) {
		MessageEmbed m = this.handler.getBuilder().buildNewQueueMessage(track);
		this.handler.log("queueing song");
		this.handler.sendQueueMessage(track, new Consumer<Message>() {
			public void accept(Message m) {
				Player.this.handler.log("added song to queue");
				Player.this.queue.add(new QueueElement(track, m));
				if (Player.this.player.getPlayingTrack() == null) {
					nextTrack();
				} else {
					Player.this.handler.getBuilder().setQueue(m);
				}
			}
		});
	}

	public void nextTrack() {
		setPaused(false);

		if (this.currentTrack != null) {
			this.handler.getBuilder().setPlayed(this.currentTrack.getMessage());
		}
		if (!this.queue.isEmpty()) {

			this.currentTrack = this.queue.get(0);
			this.player.playTrack(this.currentTrack.getTrack().getTrack());
			this.handler.getBuilder().setPlaying(this.currentTrack.getMessage());
			this.queue.remove(0);
		} else {
			this.player.stopTrack();
		}
	}
	public void voiceUpdate() {
		if(audioManager.isConnected()){
			if(audioManager.getConnectedChannel().getMembers().size() > 1){
				handler.log("Timer stopped.");
				timer.stop();
			} if(audioManager.getConnectedChannel().getMembers().size() == 1){
				handler.log("Disconnecting in "+DISCONNECT_TIME/1000+"s...");
				timer.start();
			}
		}
	}

	public void setPaused(boolean b) {
		this.player.setPaused(b);
	}

	@Override
	public void onTimerEnd() {
		leave();
	}
}
