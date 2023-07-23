package halleg.discordmusikbot.guild;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
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
    private static final long MAX_SIZE = (long) 1E9;
    private File musicFolder;
    private GuildHandler handler;

    public FileManager(File musicFolder, GuildHandler handler) {
        this.handler = handler;

        this.musicFolder = new File(musicFolder, String.valueOf(handler.getGuild().getIdLong()));
        if (!this.musicFolder.exists()) {
            this.musicFolder.mkdir();
        }
    }

    public String showTree() {
        StringBuilder buffer = new StringBuilder(50);
        tree(buffer, "", "", this.musicFolder);
        return buffer.toString();
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

    public MessageCreateData listFiles(String path) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            return this.handler.getBuilder().buildErrorMessage("Invalid directory \"" + path + "\"");
        }

        if (!folder.exists() || !folder.isDirectory()) {
            return this.handler.getBuilder().buildErrorMessage("\"" + getRelativePath(folder) + "\" does not exist" +
                    " or is not a directory");
        }

        String[] directories = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });

        String[] files = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !new File(dir, name).isDirectory();
            }
        });
        return this.handler.getBuilder().buildListMessage(directories, files);
    }

    public String copyFile(String path, String newPath) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            return "Invalid directory " + MessageFactory.inlineCodeBlock(path);
        }

        if (!folder.exists()) {
            return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " does not exist.";
        }

        File newFolder = null;
        try {
            newFolder = getSecureSubFile(this.musicFolder, newPath);
        } catch (IOException e) {
            return "Invalid directory " + MessageFactory.inlineCodeBlock(newPath);
        }

        if (newFolder.exists()) {
            return "\"" + MessageFactory.inlineCodeBlock(getRelativePath(newFolder)) + "\" does " +
                    "already exist.";
        }

        if (folder.isDirectory()) {
            if (!checkFolderSize(folderSize(folder))) {
                return "Not enough space available to create copy";
            }

            try {
                FileUtils.copyDirectory(folder, newFolder);
            } catch (IOException e) {

                return "failed to copy " + MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " to " + getRelativePath(newFolder);
            }
        } else {
            if (!checkFolderSize(folder.length())) {
                return "Not enough space available to create copy";
            }

            try {
                Files.copy(folder.toPath(), newFolder.toPath());
            } catch (IOException e) {
                return "failed to copy " + MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " to " + getRelativePath(newFolder);
            }
        }

        return "copied " + MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " to " + getRelativePath(newFolder);
    }

    public String moveFile(String path, String newPath) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            return "Invalid directory " + MessageFactory.inlineCodeBlock(path);
        }

        if (!folder.exists()) {
            return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " does not exist.";
        }

        File newFolder = null;
        try {
            newFolder = getSecureSubFile(this.musicFolder, newPath);
        } catch (IOException e) {
            return "Invalid directory " + MessageFactory.inlineCodeBlock(newPath);
        }

        if (newFolder.exists()) {
            return MessageFactory.inlineCodeBlock(getRelativePath(newFolder)) + " does already " +
                    "exist.";
        }

        folder.renameTo(newFolder);
        return "moved " + MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " to " + MessageFactory.inlineCodeBlock(getRelativePath(newFolder));
    }

    public String deleteFile(String path) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            return "Invalid directory " + MessageFactory.inlineCodeBlock(path);
        }

        if (!folder.exists()) {
            return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " does not exist.";
        }

        if (!folder.delete()) {
            return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " is not empty.";
        } else {
            return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " has been deleted.";
        }
    }

    public String deleteDirectoryRecursively(String path) {
        File folder = null;
        try {
            folder = getSecureSubFile(this.musicFolder, path);
        } catch (IOException e) {
            return "Invalid directory " + MessageFactory.inlineCodeBlock(path);
        }

        if (!folder.exists() || !folder.isDirectory()) {
            return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " does not exist or " +
                    "is not a directory";
        }

        deleteRecursively(folder);
        return MessageFactory.inlineCodeBlock(getRelativePath(folder)) + " has been deleted.";
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

        if (!checkFolderSize(attachment.getSize())) {
            this.handler.sendErrorMessage("Not enough space available to download Attachment");
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

    private boolean checkFolderSize(long newFile) {
        long size = folderSize(this.musicFolder) + newFile;
        this.handler.log("current size: " + size);
        return size < MAX_SIZE;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }
}
