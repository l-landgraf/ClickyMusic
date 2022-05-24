package halleg.discordmusikbot.guild.local;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyLocalAudioSourceManager extends LocalAudioSourceManager {
    private File musicFodler;

    public MyLocalAudioSourceManager(File musicFolder) {
        this.musicFodler = musicFolder;
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        File file = new File(this.musicFodler, reference.identifier);
        Path filePath = null;
        Path folderPath = null;
        try {
            filePath = Paths.get(file.getCanonicalPath()).toAbsolutePath();
            folderPath = Paths.get(this.musicFodler.getCanonicalPath()).toAbsolutePath();
        } catch (IOException | InvalidPathException e) {
            return null;
        }
        if (!filePath.startsWith(folderPath)) {
            return null;
        }
        String stringPath = file.toURI().getPath();
        AudioReference newRef = new AudioReference(stringPath, folderPath.relativize(filePath).toString(),
                reference.containerDescriptor);
        AudioItem item = super.loadItem(manager, newRef);
        return item;
    }
}
