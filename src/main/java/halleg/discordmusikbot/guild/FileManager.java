package halleg.discordmusikbot.guild;

import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.List;
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
        StringBuilder buffer = new StringBuilder(50);
        tree(buffer, "", "", this.musicFolder);
        this.handler.queueAndDeleteLater(new net.dv8tion.jda.api.MessageBuilder("```" + buffer + "```").build());
    }

    private void tree(StringBuilder buffer, String prefix, String childrenPrefix, File file) {
        buffer.append(prefix);
        buffer.append(file.getName());
        buffer.append('\n');
        File[] arr = file.listFiles();
        if (arr != null) {
            List<File> children = List.of(file.listFiles());
            for (Iterator<File> i = children.iterator(); i.hasNext(); ) {
                File next = i.next();
                if (i.hasNext()) {
                    tree(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ", next);
                } else {
                    tree(buffer, childrenPrefix + "└── ", childrenPrefix + "    ", next);
                }
            }
        }
    }

    public void listFiles(String path) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return;
        }

        if (!folder.exists() || !folder.isDirectory()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(folder) + "\" does not exist or is not a directory");
            return;
        }

        String[] directories = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });
        this.handler.queueAndDeleteLater(this.handler.getBuilder().buildListMessage("Directories", directories));


        String[] files = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !new File(dir, name).isDirectory();
            }
        });
        this.handler.queueAndDeleteLater(this.handler.getBuilder().buildListMessage("Files", files));
    }

    public void copyFile(String path, String newPath) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return;
        }

        if (!folder.exists()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(folder) + "\" does not exist.");
            return;
        }

        File newFolder = null;
        try {
            newFolder = getSecureSubFile(this.musicFolder, newPath);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + newPath + "\"");
            return;
        }

        if (newFolder.exists()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(newFolder) + "\" does already exist.");
            return;
        }

        if (folder.isDirectory()) {
            try {
                FileUtils.copyDirectory(folder, newFolder);
            } catch (IOException e) {
                this.handler.sendErrorMessage("failed to copy " + getRelativePath(folder) + " to " + getRelativePath(newFolder));
                e.printStackTrace();
                return;
            }
        } else {
            try {
                Files.copy(folder.toPath(), newFolder.toPath());
            } catch (IOException e) {
                this.handler.sendErrorMessage("failed to copy " + getRelativePath(folder) + " to " + getRelativePath(newFolder));
                e.printStackTrace();
                return;
            }
        }

        this.handler.sendInfoMessage("copied " + getRelativePath(folder) + " to " + getRelativePath(newFolder));
    }

    public void moveFile(String path, String newPath) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return;
        }

        if (!folder.exists()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(folder) + "\" does not exist.");
            return;
        }

        File newFolder = null;
        try {
            newFolder = getSecureSubFile(this.musicFolder, newPath);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + newPath + "\"");
            return;
        }

        if (newFolder.exists()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(newFolder) + "\" does already exist.");
            return;
        }

        folder.renameTo(newFolder);
        this.handler.sendInfoMessage("moved " + getRelativePath(folder) + " to " + getRelativePath(newFolder));
    }

    public void deleteFile(String path) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return;
        }

        if (!folder.exists()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(folder) + "\" does not exist.");
            return;
        }

        if (!folder.delete()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(folder) + "\" is not empty.");
            return;
        } else {
            this.handler.sendInfoMessage("\"" + getRelativePath(folder) + "\" has been deleted.");
        }
    }

    public void deleteDirectoryRecursively(String path) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return;
        }

        if (!folder.exists() || !folder.isDirectory()) {
            this.handler.sendErrorMessage("\"" + getRelativePath(folder) + "\" does not exist or is not a directory");
            return;
        }

        deleteRecursively(folder);
        this.handler.sendInfoMessage("\"" + path + "\" has been deleted.");
    }

    private void deleteRecursively(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                deleteRecursively(f);
            }
        }
        file.delete();

    }

    public boolean downloadAttachment(Message.Attachment attachment, String path) {
        File targetDirectory = null;
        try {
            targetDirectory = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            this.handler.sendErrorMessage("Invalid directory \"" + path + "\"");
            return false;
        }
        targetDirectory.mkdirs();
        File file = new File(targetDirectory, attachment.getFileName());
        if (file.exists()) {
            this.handler.sendErrorMessage("File " + getRelativePath(file) + " already exists");
            return false;
        }

        this.handler.log("Downloading file \"" + file.getPath() + "\"");
        CompletableFuture<File> future = attachment.downloadToFile(file.getAbsolutePath());
        future.exceptionally(error -> {
            error.printStackTrace();
            return null;
        });
        this.handler.sendInfoMessage("saved to\"" + getRelativePath(file) + "\"");
        return true;
    }

    public File getSecureSubFile(File parent, String child) throws IOException {
        File sub = new File(parent, child);
        if (sub.getCanonicalPath().startsWith(parent.getCanonicalPath() + File.separator)) {
            return sub;
        } else if (sub.getCanonicalPath().equals(parent.getCanonicalPath())) {
            return sub;
        }

        throw new NoSuchFileException(child);
    }

    public String getRelativePath(File child) {
        return new File(this.musicFolder.getAbsolutePath()).toPath().relativize(new File(child.getAbsolutePath()).toPath()).toString();
    }
}
