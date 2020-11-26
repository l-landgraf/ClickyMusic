package halleg.discordmusikbot.guild.local;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

import java.io.File;

public class MyLocalAudioSourceManager extends LocalAudioSourceManager {
	private File musicFodler;

	public MyLocalAudioSourceManager(File musicFolder) {
		this.musicFodler = musicFolder;
	}

	@Override
	public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
		File file = new File(this.musicFodler.getPath(), reference.identifier);
		String filePath = file.getAbsolutePath();
		AudioReference newRef = new AudioReference(filePath, reference.title, reference.containerDescriptor);
		return super.loadItem(manager, newRef);
	}
}
