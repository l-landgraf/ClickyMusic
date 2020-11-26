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
		File f = new File(this.musicFodler.getPath(), reference.identifier);
		String newIdent = f.getAbsolutePath();
		System.out.println(newIdent);
		AudioReference newRef = new AudioReference(newIdent, reference.title, reference.containerDescriptor);
		AudioItem a = super.loadItem(manager, newRef);
		System.out.println(a);
		return a;
	}
}
