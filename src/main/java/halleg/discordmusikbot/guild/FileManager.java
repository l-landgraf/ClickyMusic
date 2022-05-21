package halleg.discordmusikbot.guild;

import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.CompletableFuture;

public class FileManager {
    private File musicFolder;
    private GuildHandler handler;

    public FileManager(File musicFolder, GuildHandler handler) {
        this.handler = handler;

        this.musicFolder = new File(musicFolder, String.valueOf(handler.getGuild().getIdLong()));
        if (!this.musicFolder.exists()) {
            this.musicFolder.mkdir();
        }
    }

    public void showTree() {

    }

    public void listFiles() {

    }

    public void moveFile() {

    }

    public void deleteFile() {

    }

    public void downloadAttachment(Message.Attachment attachment, String path) {
        File targetDirectory = null;
        try {
            targetDirectory = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return;
        }
        targetDirectory.mkdirs();
        File file = new File(targetDirectory, attachment.getFileName());
        if (file.exists()) {
            this.handler.sendErrorMessage("File " + path + File.separator + attachment.getFileName() + " already exists");
            return;
        }

        this.handler.log("Downloading file \"" + file.getPath() + "\"");
        CompletableFuture<File> future = attachment.downloadToFile(file.getAbsolutePath());
        future.exceptionally(error -> {
            error.printStackTrace();
            return null;
        });
    }

    private File getSecureSubFile(File parent, String child) throws IOException {
        File sub = new File(parent, child);
        System.out.println(sub.getCanonicalPath());
        if (sub.getCanonicalPath().startsWith(parent.getCanonicalPath() + File.separator)) {
            return sub;
        } else if (sub.getCanonicalPath().equals(parent.getCanonicalPath())) {
            return sub;
        }

        throw new NoSuchFileException(child);
    }
}
